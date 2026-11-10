package com.github.olympus.argus;

import com.github.olympus.argus.client.ArgusMenu;
import com.github.olympus.argus.client.ArgusClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public final class ArgusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArgusClient.configure(FabricLoader.getInstance().getConfigDir());
        ArgusMenu.register();
        for (KeyMapping key : ArgusClient.keys().all()) {
            KeyBindingHelper.registerKeyBinding(key);
        }
    }
}
