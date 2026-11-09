#!/usr/bin/env python3
import argparse
import json
import re
import sys
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SKIP_DIRS = {'.git', '.gradle', 'build', 'run', 'run-data'}
TEXT_SUFFIXES = {'.java', '.gradle', '.properties', '.json', '.toml', '.md', '.txt', '.mcmeta', '.removed'}
EXTRA_FILES = ['publish.gradle', 'CHANGELOG.md']
WRAPPER_FILES = ['gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties']
LOADER_KEYS = {
    'fabric': ['loader_version', 'loom_version', 'fabric_api_version'],
    'forge': ['forge_version'],
    'neoforge': ['neo_version'],
}
COMMON_KEYS = ['minecraft_version', 'mod_java', 'mod_label', 'publish_minecraft_versions']

problems = []


def problem(target, message):
    problems.append(f'{target}: {message}')


def read_properties(path):
    props = {}
    for line in path.read_text(encoding='utf-8').splitlines():
        line = line.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        key, value = line.split('=', 1)
        props[key.strip()] = value.strip()
    return props


def read_versions():
    return [v.strip() for v in (ROOT / 'versions.txt').read_text(encoding='utf-8').splitlines() if v.strip()]


def merge(version, loader, order):
    layers = [ROOT / 'core', ROOT / 'loaders' / loader]
    for v in order:
        layers.append(ROOT / 'versions' / v / 'common')
        layers.append(ROOT / 'versions' / v / loader)
        if v == version:
            break
    files = {name: ROOT / name for name in EXTRA_FILES}
    for layer in layers:
        if not layer.is_dir():
            continue
        for found in sorted(layer.rglob('*')):
            if not found.is_file():
                continue
            rel = found.relative_to(layer).as_posix()
            if rel.endswith('.removed'):
                files.pop(rel[: -len('.removed')], None)
            else:
                files[rel] = found
    return files


def check_target(version, loader, jdk, order):
    target = f'{version}/{loader}'
    files = merge(version, loader, order)

    for rel in ['build.gradle', 'gradle.properties', 'settings.gradle', 'publish.gradle', 'CHANGELOG.md'] + WRAPPER_FILES:
        if rel not in files:
            problem(target, f'missing {rel}')

    props = {}
    if 'gradle.properties' in files:
        props = read_properties(files['gradle.properties'])
    for key in COMMON_KEYS + LOADER_KEYS[loader]:
        if key not in props:
            problem(target, f'gradle.properties has no {key}')

    java = props.get('mod_java')
    if java and java.isdigit():
        if int(java) > jdk:
            problem(target, f'mod_java {java} is newer than the JDK {jdk} that runs the build')
    published = [s.strip() for s in props.get('publish_minecraft_versions', '').split(',') if s.strip()]
    if not published:
        problem(target, 'publish_minecraft_versions is empty')
    elif props.get('minecraft_version') not in published:
        problem(target, f'minecraft_version {props.get("minecraft_version")} is not in publish_minecraft_versions')

    mixin_name = next((rel for rel in files if rel.endswith('.mixins.json')), None)
    if mixin_name is None:
        problem(target, 'no mixin config')
    else:
        try:
            mixins = json.loads(files[mixin_name].read_text(encoding='utf-8'))
            level = re.fullmatch(r'JAVA_(\d+)', mixins.get('compatibilityLevel', ''))
            if not level:
                problem(target, 'mixin compatibilityLevel is not JAVA_<n>')
            elif java and java.isdigit() and int(level.group(1)) > int(java):
                problem(target, f'mixin compatibilityLevel {level.group(0)} is newer than mod_java {java}')
        except json.JSONDecodeError as error:
            problem(target, f'{mixin_name} is not valid JSON: {error}')

    sources = {rel: path for rel, path in files.items() if rel.endswith('.java') and '/src/main/java/' in '/' + rel}
    texts = {rel: path.read_text(encoding='utf-8') for rel, path in sources.items()}

    if loader == 'fabric':
        meta = files.get('src/main/resources/fabric.mod.json')
        if meta is None:
            problem(target, 'missing fabric.mod.json')
        else:
            try:
                data = json.loads(meta.read_text(encoding='utf-8'))
            except json.JSONDecodeError as error:
                problem(target, f'fabric.mod.json is not valid JSON: {error}')
                data = {}
            for key in ('id', 'name', 'version', 'entrypoints', 'mixins', 'depends'):
                if key not in data:
                    problem(target, f'fabric.mod.json has no {key}')
            for entry in data.get('entrypoints', {}).get('client', []):
                expected = 'src/main/java/' + entry.replace('.', '/') + '.java'
                if expected not in files:
                    problem(target, f'entrypoint {entry} has no source file')
            if mixin_name and Path(mixin_name).name not in data.get('mixins', []):
                problem(target, 'fabric.mod.json does not list the mixin config')
            declared = re.search(r'>=(\d+)', data.get('depends', {}).get('java', ''))
            if declared and java and declared.group(1) != java:
                problem(target, f'fabric.mod.json needs Java {declared.group(1)} but mod_java is {java}')
        if not any('implements ClientModInitializer' in t for t in texts.values()):
            problem(target, 'no ClientModInitializer entry point')
    else:
        candidates = ['src/main/resources/META-INF/mods.toml'] if loader == 'forge' else [
            'src/main/templates/META-INF/mods.toml', 'src/main/templates/META-INF/neoforge.mods.toml']
        present = [c for c in candidates if c in files]
        if len(present) != 1:
            problem(target, f'expected exactly one mods metadata file, found {present}')
        for rel in present:
            raw = files[rel].read_text(encoding='utf-8')
            try:
                tomllib.loads(re.sub(r'\$\{[^}]+\}', 'x', raw))
            except tomllib.TOMLDecodeError as error:
                problem(target, f'{rel} is not valid TOML: {error}')
            if mixin_name and Path(mixin_name).name not in raw:
                problem(target, f'{rel} does not reference the mixin config')
        if not any('@Mod(' in t for t in texts.values()):
            problem(target, 'no @Mod entry point')

    if not any(Path(rel).name == 'ArgusConfig.java' for rel in sources):
        problem(target, 'no shared common class from core')

    return props


def forbidden_scan(forbid):
    for path in sorted(ROOT.rglob('*')):
        if not path.is_file() or any(part in SKIP_DIRS for part in path.relative_to(ROOT).parts):
            continue
        if path.resolve() == Path(__file__).resolve() or path.suffix not in TEXT_SUFFIXES and path.name != 'LICENSE':
            continue
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            continue
        for word in forbid:
            if word.lower() in text.lower() or word.lower() in path.name.lower():
                problem(path.relative_to(ROOT).as_posix(), f'contains forbidden text "{word}"')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--forbid', action='append', default=[], help='text that must not appear anywhere')
    args = parser.parse_args()

    order = read_versions()
    count = 0
    for version in order:
        vp = ROOT / 'versions' / version / 'version.properties'
        if not vp.exists():
            problem(version, 'missing version.properties')
            continue
        props = read_properties(vp)
        for loader in [s.strip() for s in props.get('loaders', '').split(',') if s.strip()]:
            if loader not in LOADER_KEYS:
                problem(version, f'unknown loader {loader}')
                continue
            jdk = props.get(f'jdk.{loader}')
            if not jdk or not jdk.isdigit():
                problem(version, f'version.properties has no jdk.{loader}')
                continue
            check_target(version, loader, int(jdk), order)
            count += 1

    changelog = (ROOT / 'CHANGELOG.md').read_text(encoding='utf-8')
    mod_version = read_properties(ROOT / 'gradle.properties').get('mod_version', '')
    if f'## {mod_version}' not in changelog.splitlines():
        problem('CHANGELOG.md', f'no "## {mod_version}" entry for mod_version')

    if args.forbid:
        forbidden_scan(args.forbid)

    if problems:
        print('\n'.join(problems))
        print(f'{len(problems)} problem(s) in {count} target(s)')
        sys.exit(1)
    print(f'ok: {count} targets across {len(order)} version ranges')


if __name__ == '__main__':
    main()
