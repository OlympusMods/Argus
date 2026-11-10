package com.github.olympus.argus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.client.Minecraft;

public final class KeyInput {
    private static final int GLFW_ESCAPE = 256;
    private static final int SDL_ESCAPE = 41;

    public static final boolean SDL = keyboardTypeNamed("KEYBOARD") != null;

    private static final InputConstants.Type KEYBOARD = SDL ? keyboardTypeNamed("KEYBOARD") : keyboardTypeNamed("KEYSYM");
    private static final MethodHandle IS_DOWN = findIsDown();

    private KeyInput() {
    }

    public static InputConstants.Type keyboardType() {
        if (KEYBOARD == null) {
            throw new IllegalStateException("InputConstants.Type has no keyboard type");
        }
        return KEYBOARD;
    }

    public static int escape() {
        return SDL ? SDL_ESCAPE : GLFW_ESCAPE;
    }

    public static boolean down(Minecraft minecraft, int key) {
        try {
            if (SDL) {
                return (boolean) IS_DOWN.invokeExact(key);
            }
            return (boolean) IS_DOWN.invokeExact(minecraft.getWindow(), key);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Could not read key " + key, throwable);
        }
    }

    public static String name(int key) {
        return keyboardType().getOrCreate(key).getDisplayName().getString();
    }

    private static InputConstants.Type keyboardTypeNamed(String name) {
        for (InputConstants.Type type : InputConstants.Type.class.getEnumConstants()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    private static MethodHandle findIsDown() {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodType type = SDL
                ? MethodType.methodType(boolean.class, int.class)
                : MethodType.methodType(boolean.class, Window.class, int.class);
        try {
            return lookup.findStatic(InputConstants.class, "isKeyDown", type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("This Minecraft version has no supported InputConstants.isKeyDown", exception);
        }
    }
}
