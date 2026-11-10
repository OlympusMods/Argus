package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class FovMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void argus$fov(float partialTick, CallbackInfoReturnable<Float> callback) {
        float fov = callback.getReturnValue();
        float zoomed = (float) Argus.fov(fov);
        if (zoomed != fov) {
            callback.setReturnValue(zoomed);
        }
    }
}
