package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void argus$hud(CallbackInfo callback) {
        if (Argus.hideHud()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void argus$crosshair(CallbackInfo callback) {
        if (Argus.hideCrosshair()) {
            callback.cancel();
        }
    }
}
