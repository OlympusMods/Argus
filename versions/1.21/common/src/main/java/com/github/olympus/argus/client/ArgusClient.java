package com.github.olympus.argus.client;

import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArgusClient {
    public static final String MOD_ID = "argus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Path configDirectory = Path.of("config");
    private static KeyBindings keys;

    private ArgusClient() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void configure(Path directory) {
        configDirectory = directory;
    }

    public static Path configDirectory() {
        return configDirectory;
    }

    public static synchronized KeyBindings keys() {
        if (keys == null) {
            keys = KeyFactory.create();
        }
        return keys;
    }

    public static synchronized void installKeys(KeyBindings bindings) {
        keys = bindings;
    }

    public static void tick(Minecraft minecraft) {
        Argus.tick(minecraft);
    }
}
