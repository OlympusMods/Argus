package com.github.olympus.argus.mixin;

import com.github.olympus.argus.client.Argus;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTurnMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void argus$turn(double yaw, double pitch, CallbackInfo callback) {
        if (Argus.onTurn((Entity) (Object) this, yaw, pitch)) {
            callback.cancel();
        }
    }
}
