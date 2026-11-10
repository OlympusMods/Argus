package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendedGui.class)
public abstract class NeoHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void argus$hud(CallbackInfo callback) {
        if (Argus.hideHud()) {
            callback.cancel();
        }
    }
}
