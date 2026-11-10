package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    private boolean argus$aligning;

    @Inject(method = "setup", at = @At("HEAD"))
    private void argus$begin(CallbackInfo callback) {
        argus$aligning = Argus.cameraDetached();
    }

    @Inject(method = "setup", at = @At("RETURN"))
    private void argus$end(CallbackInfo callback) {
        argus$aligning = false;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float argus$yaw(float yaw) {
        return argus$aligning ? Argus.cameraYaw() : yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float argus$pitch(float pitch) {
        return argus$aligning ? Argus.cameraPitch() : pitch;
    }

    @Inject(method = "setRotation", at = @At("RETURN"))
    private void argus$rotated(CallbackInfo callback) {
        argus$aligning = false;
    }

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), argsOnly = true)
    private double argus$distance(double distance) {
        return (double) Argus.cameraDistance(distance);
    }
}
