# Argus

A client-side zoom and freelook camera for Minecraft.

Hold **C** to zoom and scroll to change the level. Hold **Left Alt** to look around without turning your character. Both work at the same time, and nothing is sent to the server, so it is safe on any multiplayer server.

## Zoom

- Hold to zoom, or switch to toggle. Scrolling multiplies the zoom by a step you choose, and a second key gives a finer step while held.
- 1x up to a maximum you set (1000x at most), or unlimited, which goes to 100000x. Past that the game's own geometry runs out of precision, so that is where it stops.
- The mouse slows down with the zoom so aiming still works. Aim is tracked in doubles and the part a float can't hold is put into the projection, so the view pans smoothly at high zoom instead of stepping.
- View bobbing is off while zoomed. Culling is untouched, because the game already culls with the normal FOV.
- Starting level, remember the last level (kept across restarts), smooth transitions with a speed, cinematic style mouse smoothing.
- Reset with a key or a middle click.
- Optional: hide the crosshair, the hand or the whole HUD, a vignette, and a zoom level indicator.

Scrolling only belongs to Argus while zoom is active. The rest of the time it switches the hotbar like always.

## Zooming out

In third person, or while freelooking, scrolling out past 1x pulls the camera back from you, up to a distance you set. The camera still stops at walls. In first person scrolling out stops at 1x, or widens the FOV up to a limit if you turn that on.

## Freelook

- While the key is held the mouse turns the camera and your character keeps facing where it was. Movement, attacks, block interaction and aim all use the real facing.
- Works in first and third person, where it orbits you. It can switch to third person for you.
- On release the camera swings back, snaps back, or stays where it is until you press the recenter key.
- Toggle mode, invert pitch, its own sensitivity, pitch limits, hide hand, and whether your head turns to follow the camera in third person.

## Settings and keys

Everything is in the Argus card of the [Proteus](https://github.com/OlympusMods/Proteus) menu. Open it from any Olympus mod's menu or bind the Open settings key. The same options are in `config/argus.json`.

Zoom, fine zoom step, reset zoom, freelook, recenter camera and open settings are normal keybinds under Controls, Argus.

## How it works

`core/.../core` is plain Java with no Minecraft in it: `ZoomModel`, `FreelookModel`, `Aim` and `ArgusConfig`. The tests cover those. `client/Argus` connects them to the game, and the mixins only call into it:

- `FovMixin` changes the FOV the game computed
- `CameraMixin` swaps the camera rotation while freelooking and adds the pull back distance
- `EntityTurnMixin` takes the mouse for freelook and scales it for zoom
- `MouseScrollMixin` takes the scroll wheel while zoom is active
- `ProjectionMixin` shifts the projection by the aim remainder
- `GameRendererMixin` and `HudMixin` hide the hand, the bobbing, the HUD and the crosshair

Argus needs Proteus 1.1.0 or newer, and Fabric API on Fabric. The build takes Proteus from Modrinth's Maven.

One source tree builds a jar for every Minecraft range and loader below. Shared code lives in `core`, and a Minecraft range only holds the files that changed from the range before it.

## Supported versions

| Minecraft | Folder | Fabric | Forge | NeoForge |
|-----------|--------|:------:|:-----:|:--------:|
| 1.20-1.20.2 | `1.20` | yes | yes | n/a |
| 1.20.3-1.20.4 | `1.20.3` | yes | yes | yes |
| 1.20.5-1.20.6 | `1.20.5` | yes | yes | yes |
| 1.21-1.21.1 | `1.21` | yes | yes | yes |
| 1.21.2-1.21.3 | `1.21.2` | yes | yes | yes |
| 1.21.4-1.21.5 | `1.21.4` | yes | yes | yes |
| 1.21.6-1.21.8 | `1.21.6` | yes | yes | yes |
| 1.21.9-1.21.10 | `1.21.9` | yes | yes | yes |
| 1.21.11 | `1.21.11` | yes | yes | yes |
| 26.1-26.1.2 | `26.1` | yes | yes | yes |
| 26.2 | `26.2` | yes | yes | yes |
| 26.3 | `26.3` | yes | yes | yes |

## Building

Each build lays out one Gradle project per range and loader under `build/targets/<range>/<loader>` and runs that project's own wrapper, so every target gets the Gradle it needs. Set `JAVA17_HOME`, `JAVA21_HOME` and `JAVA25_HOME` (or `-Pjava21Home=...`) to point at the JDKs. Fabric needs Java 25 to run Gradle for every range.

```sh
./gradlew :1.21.4:fabric:build
./gradlew :1.21.4:build
./gradlew build -PonlyVersions=1.21.4,1.21.6 -PonlyLoaders=fabric
./gradlew build
```

Jars are collected in `build/dist`. Set the version in `gradle.properties`. On Windows the layered sources are copied instead of linked, so edit the files under `core`, `loaders` and `versions`.

## Checks

```sh
python3 scripts/check.py
```

Merges the layers of every target like the build does and checks the metadata, mixin config, entry points, Java versions and changelog. It does not compile anything. The unit test in `loaders/fabric/src/test` runs with the Fabric builds.

## Layout

```
core/                       code and resources every range and loader shares
loaders/<loader>/           files every range of that loader shares
versions/<range>/common/    what changed in this range, for every loader
versions/<range>/<loader>/  what changed in this range, for one loader
versions/<range>/version.properties   loaders in the range and the JDK that runs each build
versions.txt                the ranges, oldest first
publish.gradle              Modrinth and CurseForge setup shared by every target
```

A target is the layers stacked in that order: `core`, `loaders/<loader>`, then every range from the oldest up to its own. A later file replaces an earlier one with the same path, and an empty `name.removed` file deletes `name`.

To add a Minecraft range, create `versions/<range>`, list it in `versions.txt`, add `version.properties` and only the files that differ (usually each loader's `gradle.properties` and metadata). Put code the compiler rejects in `versions/<range>/common` or the loader folder.

## Publishing

Set `modrinthToken` and `curseforgeToken` in `~/.gradle/gradle.properties` (or `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`), set the two project ids at the top of `publish.gradle`, and add a `## <version>` entry to `CHANGELOG.md`.

```sh
./gradlew publishMods -PdryRun=true
./gradlew publishModrinth
./gradlew publishCurseforge
```

Without a token the tasks only do a dry run, and a platform whose id is still `TEMPLATE_*` is skipped. Every upload is marked client only and lists Proteus as required.

## License

[GPL-3.0](LICENSE)
