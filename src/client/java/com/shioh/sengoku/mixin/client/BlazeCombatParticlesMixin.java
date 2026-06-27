package com.shioh.sengoku.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds soul fire particles when Blazes enter combat mode in the Nether.
 */
@Mixin(Blaze.class)
public abstract class BlazeCombatParticlesMixin {
    
    @Shadow
    private int attackAnimationTick;
    
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void sengoku$addSoulFireCombatParticles(CallbackInfo ci) {
        Blaze blaze = (Blaze) (Object) this;
        
        // Only spawn particles when in combat mode (attackAnimationTick > 0) and in the Nether
        if (this.attackAnimationTick > 0 && blaze.level() instanceof ClientLevel clientLevel && 
            blaze.level().dimension() == Level.NETHER) {
            
            // Spawn soul fire flame particles around the Blaze
            for (int i = 0; i < 2; ++i) {
                double xOffset = blaze.getX() + (blaze.getRandom().nextDouble() - 0.5) * blaze.getBbWidth();
                double yOffset = blaze.getY() + blaze.getRandom().nextDouble() * blaze.getBbHeight();
                double zOffset = blaze.getZ() + (blaze.getRandom().nextDouble() - 0.5) * blaze.getBbWidth();
                
                clientLevel.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    xOffset,
                    yOffset,
                    zOffset,
                    0.0,
                    0.0,
                    0.0
                );
            }
        }
    }
}
