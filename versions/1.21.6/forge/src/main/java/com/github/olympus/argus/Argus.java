package com.github.olympus.argus;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Argus.MOD_ID)
public class Argus {
    public static final String MOD_ID = "argus";

    public Argus() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.register();
        }
    }
}
