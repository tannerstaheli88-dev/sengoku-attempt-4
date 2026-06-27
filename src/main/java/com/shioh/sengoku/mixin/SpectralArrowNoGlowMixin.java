package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SpectralArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the glowing effect from spectral arrows to make them work more like bullets
 */
@Mixin(SpectralArrow.class)
public abstract class SpectralArrowNoGlowMixin {
    
    @Inject(method = "doPostHurtEffects", at = @At("HEAD"), cancellable = true)
    private void removeGlowingEffect(LivingEntity living, CallbackInfo ci) {
        // Cancel the method that applies the glowing effect
        // This prevents spectral arrows from making entities glow
        ci.cancel();
    }
}
