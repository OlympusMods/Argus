package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class FovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void argus$fov(Camera camera, float partialTick, boolean world, CallbackInfoReturnable<Float> callback) {
        float fov = callback.getReturnValue();
        float zoomed = (float) Argus.fov(fov);
        if (world && zoomed != fov) {
            callback.setReturnValue(zoomed);
        }
    }
}
