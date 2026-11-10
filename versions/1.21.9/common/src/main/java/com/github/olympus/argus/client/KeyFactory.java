package com.github.olympus.argus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyFactory {
    private static final int UNBOUND = InputConstants.UNKNOWN.getValue();
    private KeyFactory() {
    }

    public static KeyMapping.Category registerCategory() {
        return KeyMapping.Category.register(ArgusClient.id("main"));
    }

    public static KeyBindings create(KeyMapping.Category category) {
        return new KeyBindings(key("zoom", GLFW.GLFW_KEY_C, category),
                key("fine", GLFW.GLFW_KEY_X, category),
                key("reset", UNBOUND, category),
                key("freelook", GLFW.GLFW_KEY_LEFT_ALT, category),
                key("recenter", UNBOUND, category),
                key("menu", UNBOUND, category));
    }

    private static KeyMapping key(String name, int code, KeyMapping.Category category) {
        return new KeyMapping("key.argus." + name, InputConstants.Type.KEYSYM, code, category);
    }
}
