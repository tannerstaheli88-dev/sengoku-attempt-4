package com.shioh.sengoku.mixin.client;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to add smoke particles when spectral arrow flies,
 * simulating a tanegashima musket discharge effect.
 * 
 * NOTE: Most smoke now spawns at the shooter's position via CrossbowItemMixin.
 * This only adds a small trail effect on the projectile itself.
 */
@Mixin(SpectralArrow.class)
public class SpectralArrowSmokeMixin {
    
    @Unique
    private int sengoku$smokeTickDelay = 0;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void addSmokeTrail(CallbackInfo ci) {
        SpectralArrow arrow = (SpectralArrow)(Object)this;
        Level level = arrow.level();
        
        // Wait just 1 tick so smoke spawns closer to the player
        if (sengoku$smokeTickDelay < 1) {
            sengoku$smokeTickDelay++;
            return;
        }
        
        // Only spawn smoke particles once, after the delay
        if (sengoku$smokeTickDelay == 1 && level.isClientSide()) {
            // Check if arrow is actually moving (was just fired)
            double speed = arrow.getDeltaMovement().length();
            if (speed > 0.5) {
                sengoku$smokeTickDelay = 2; // Mark as done
                
                // Minimal trail smoke (main smoke now spawns at shooter via CrossbowItemMixin)
                for (int i = 0; i < 3; i++) {
                    level.addParticle(
                        ParticleTypes.SMOKE,
                        arrow.getX() + (level.random.nextDouble() - 0.5) * 0.1,
                        arrow.getY() + (level.random.nextDouble() - 0.5) * 0.1,
                        arrow.getZ() + (level.random.nextDouble() - 0.5) * 0.1,
                        0.0,
                        0.01,
                        0.0
                    );
                }
            }
        }
    }
}
