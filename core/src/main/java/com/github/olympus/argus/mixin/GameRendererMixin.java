package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void argus$hand(CallbackInfo callback) {
        if (Argus.hideHand()) {
            callback.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void argus$bob(CallbackInfo callback) {
        if (Argus.steadyView()) {
            callback.cancel();
        }
    }
}
