package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class ProjectionMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void argus$projection(CameraRenderState state, float partialTick, CallbackInfo callback) {
        double yaw = Argus.residualYaw();
        double pitch = Argus.residualPitch();
        if (yaw != 0.0 || pitch != 0.0) {
            Matrix4f matrix = state.projectionMatrix;
            matrix.translateLocal((float) (-Math.tan(Math.toRadians(yaw)) * matrix.m00()),
                    (float) (Math.tan(Math.toRadians(pitch)) * matrix.m11()), 0.0f);
        }
    }
}
