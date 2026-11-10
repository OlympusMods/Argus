package com.github.olympus.argus;

import com.github.olympus.argus.client.ArgusMenu;
import com.github.olympus.argus.client.ArgusClient;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(Argus.MOD_ID)
public class Argus {
    public static final String MOD_ID = "argus";

    public Argus(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(Argus::onRegisterKeys);
        }
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ArgusClient.configure(FMLPaths.CONFIGDIR.get());
        ArgusMenu.register();
        for (KeyMapping key : ArgusClient.keys().all()) {
            event.register(key);
        }
    }
}
