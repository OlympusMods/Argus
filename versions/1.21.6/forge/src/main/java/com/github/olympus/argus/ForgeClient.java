package com.github.olympus.argus;

import com.github.olympus.argus.client.ArgusMenu;
import com.github.olympus.argus.client.ArgusClient;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ForgeClient {
    private ForgeClient() {
    }

    static void register() {
        RegisterKeyMappingsEvent.getBus(FMLJavaModLoadingContext.get().getModBusGroup()).addListener(ForgeClient::onRegisterKeys);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ArgusClient.configure(FMLPaths.CONFIGDIR.get());
        ArgusMenu.register();
        for (KeyMapping key : ArgusClient.keys().all()) {
            event.register(key);
        }
    }
}
