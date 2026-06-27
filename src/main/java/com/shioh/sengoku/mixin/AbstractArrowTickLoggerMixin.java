package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import com.shioh.sengoku.mixin.AbstractArrowAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-only debug mixin that tracks an arrow's velocity each tick and logs
 * if its direction suddenly reverses. This helps locate where the bounce/ricochet
 * behavior is introduced without injecting into high-risk methods.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowTickLoggerMixin {

    @Unique
    private Vec3 sengoku_lastVel = null;

    private AbstractArrow self() {
        return (AbstractArrow)(Object)this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$onTick(CallbackInfo ci) {
        try {
            AbstractArrow a = self();
            if (a.level().isClientSide()) return; // server-only

            Vec3 cur = a.getDeltaMovement();
            if (sengoku_lastVel != null) {
                double curLen = cur.length();
                double lastLen = sengoku_lastVel.length();
                if (curLen > 1e-6 && lastLen > 1e-6) {
                    double dot = cur.normalize().dot(sengoku_lastVel.normalize());
                    // If directions are nearly opposite, log a reversal event
                    if (dot < -0.9) {
                        // reversal detected (debug logging removed)
                    }
                }
            }

            sengoku_lastVel = cur;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
