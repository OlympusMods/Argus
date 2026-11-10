package com.github.olympus.argus.client;

import com.github.olympus.argus.client.ui.ArgusUi;
import com.github.olympus.proteus.client.ProteusScreen;
import com.github.olympus.proteus.client.ui.Hub;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ArgusMenu {
    private ArgusMenu() {
    }

    public static void register() {
        Hub.register(ArgusClient.MOD_ID, () -> Component.translatable("argus.screen.title").getString(), "spyglass",
                ArgusUi::new);
    }

    public static Screen create(Screen parent) {
        return ProteusScreen.open(parent, ArgusClient.MOD_ID);
    }
}
