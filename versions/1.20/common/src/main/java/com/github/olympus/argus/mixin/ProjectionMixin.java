package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class ProjectionMixin {
    @Inject(method = "getProjectionMatrix", at = @At("RETURN"))
    private void argus$projection(CallbackInfoReturnable<Matrix4f> callback) {
        double yaw = Argus.residualYaw();
        double pitch = Argus.residualPitch();
        if (yaw != 0.0 || pitch != 0.0) {
            Matrix4f matrix = callback.getReturnValue();
            matrix.translateLocal((float) (-Math.tan(Math.toRadians(yaw)) * matrix.m00()),
                    (float) (Math.tan(Math.toRadians(pitch)) * matrix.m11()), 0.0f);
        }
    }
}
