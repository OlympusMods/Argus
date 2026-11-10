package com.github.olympus.argus;

import com.github.olympus.argus.client.ArgusMenu;
import com.github.olympus.argus.client.ArgusClient;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber(modid = Argus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeClient {
    private ForgeClient() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ArgusClient.configure(FMLPaths.CONFIGDIR.get());
        ArgusMenu.register();
        for (KeyMapping key : ArgusClient.keys().all()) {
            event.register(key);
        }
    }
}
