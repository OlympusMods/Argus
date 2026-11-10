package com.github.olympus.argus.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Constructor;
import net.minecraft.client.KeyMapping;

public final class KeyFactory {
    private static final int UNBOUND = InputConstants.UNKNOWN.getValue();
    private KeyFactory() {
    }

    public static KeyMapping.Category registerCategory() {
        return KeyMapping.Category.register(ArgusClient.id("main"));
    }

    public static KeyBindings create(KeyMapping.Category category) {
        return new KeyBindings(key("zoom", 67, 6, category),
                key("fine", 88, 27, category),
                key("reset", UNBOUND, UNBOUND, category),
                key("freelook", 342, 226, category),
                key("recenter", UNBOUND, UNBOUND, category),
                key("menu", UNBOUND, UNBOUND, category));
    }

    private static KeyMapping key(String name, int glfw, int sdl, KeyMapping.Category category) {
        String translationKey = "key.argus." + name;
        int code = KeyInput.SDL ? sdl : glfw;
        try {
            Constructor<KeyMapping> withoutType =
                    findConstructor(String.class, int.class, KeyMapping.Category.class);
            if (withoutType != null) {
                return withoutType.newInstance(translationKey, code, category);
            }
            Constructor<KeyMapping> withType = findConstructor(
                    String.class, InputConstants.Type.class, int.class, KeyMapping.Category.class);
            if (withType != null) {
                return withType.newInstance(translationKey, KeyInput.keyboardType(), code, category);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create the key mapping " + translationKey, exception);
        }
        throw new IllegalStateException("This Minecraft version has no supported KeyMapping constructor");
    }

    private static Constructor<KeyMapping> findConstructor(Class<?>... parameters) {
        try {
            return KeyMapping.class.getConstructor(parameters);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
