package com.github.olympus.argus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class Compat {
    private Compat() {
    }

    public static Screen screen(Minecraft minecraft) {
        return minecraft.screen;
    }

    public static void show(Minecraft minecraft, Screen screen) {
        minecraft.setScreen(screen);
    }
}
