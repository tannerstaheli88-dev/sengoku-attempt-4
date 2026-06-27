package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.projectile.SpectralArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes crit particles from spectral arrows for cleaner gun-like visuals
 */
@Mixin(SpectralArrow.class)
public abstract class SpectralArrowNoCritParticlesMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void disableCritParticles(CallbackInfo ci) {
        SpectralArrow arrow = (SpectralArrow)(Object)this;
        
        // Disable critical arrow flag to prevent particle effects
        if (arrow.isCritArrow()) {
            arrow.setCritArrow(false);
        }
    }
}
