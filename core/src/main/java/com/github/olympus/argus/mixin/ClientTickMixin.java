package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ClientTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void argus$tick(CallbackInfo callback) {
        Argus.tick((Minecraft) (Object) this);
    }
}
