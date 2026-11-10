package com.github.olympus.argus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyFactory {
    private static final int UNBOUND = InputConstants.UNKNOWN.getValue();
    private static final String CATEGORY = "key.categories.argus";

    private KeyFactory() {
    }

    public static KeyBindings create() {
        return new KeyBindings(key("zoom", GLFW.GLFW_KEY_C),
                key("fine", GLFW.GLFW_KEY_X),
                key("reset", UNBOUND),
                key("freelook", GLFW.GLFW_KEY_LEFT_ALT),
                key("recenter", UNBOUND),
                key("menu", UNBOUND));
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.argus." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }
}
