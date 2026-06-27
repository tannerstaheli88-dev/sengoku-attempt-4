package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.projectile.SpectralArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes spectral arrows fly much faster and farther like bullets
 */
@Mixin(SpectralArrow.class)
public class SpectralArrowVelocityMixin {
    
    private static final double DEFLECTION_THRESHOLD = 4.0; // If velocity drops below this, arrow was deflected
    private boolean wasDeflected = false;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void increaseBulletSpeed(CallbackInfo ci) {
        SpectralArrow arrow = (SpectralArrow)(Object)this;
        AbstractArrowAccessor accessor = (AbstractArrowAccessor)arrow;
        
        // On the first tick, multiply the velocity to make it go much faster
        if (arrow.tickCount == 0) {
            // Multiply velocity by 8x to make it fly like a bullet
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(8.0));
            
            // Remove gravity for bullet-like trajectory
            arrow.setNoGravity(true);
            
            // Reset life counter to give it maximum range
            accessor.setLife(0);
        }
        
        // Check if arrow was deflected (by shield, etc.)
        double currentSpeed = arrow.getDeltaMovement().length();
        
        // If arrow suddenly has low velocity after being fast, it was deflected
        if (arrow.tickCount > 1 && currentSpeed < DEFLECTION_THRESHOLD && !wasDeflected && !accessor.isInGround()) {
            // Arrow was deflected! Restore gravity and boost speed
            arrow.setNoGravity(false);
            wasDeflected = true;
            
            // Restore some speed to the deflected arrow (keep it dangerous)
            if (currentSpeed > 0.01) {
                arrow.setDeltaMovement(arrow.getDeltaMovement().normalize().scale(1.5));
            }
        }
        
        // Prevent arrows from despawning while in flight, but allow stuck arrows to despawn
        if (!accessor.isInGround() && arrow.getDeltaMovement().lengthSqr() > 0.01 && !wasDeflected) {
            // Only reset life counter if arrow is actually moving and not deflected
            // This prevents stuck/floating arrows from staying forever
            accessor.setLife(0);
        }
        
        // Safety check: if arrow is not in ground and has zero velocity for too long, let it despawn
        if (!accessor.isInGround() && arrow.getDeltaMovement().lengthSqr() < 0.001 && accessor.getLife() > 100) {
            // Arrow is stuck floating with no velocity, allow it to despawn naturally
            // Don't reset the life counter so it can despawn on its own
        }
    }
}
