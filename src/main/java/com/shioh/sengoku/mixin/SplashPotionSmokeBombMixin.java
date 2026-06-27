package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes splash potions create dramatic smoke bomb effects with lots of particles
 * and grants invisibility to all players within the smoke bomb radius when it goes off
 */
@Mixin(ThrownPotion.class)
public abstract class SplashPotionSmokeBombMixin {
    
    @Inject(method = "onHit", at = @At("HEAD"))
    private void createDramaticSmokeEffect(HitResult hitResult, CallbackInfo ci) {
        ThrownPotion potion = (ThrownPotion)(Object)this;
        
        // Only do this on server side to avoid double particles
        if (potion.level() instanceof ServerLevel serverLevel) {
            double x = hitResult.getLocation().x;
            double y = hitResult.getLocation().y;
            double z = hitResult.getLocation().z;
            
            // Grant invisibility ONLY to the player who threw the potion (if it was a player)
            if (potion.getOwner() instanceof Player thrower) {
                // Grant invisibility for 5 seconds (100 ticks)
                MobEffectInstance invisibility = new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    100,  // 5 seconds duration
                    0,    // Level 1 (amplifier 0)
                    false, // not ambient
                    false, // no visible particles (important for stealth!)
                    true   // show icon
                );
                thrower.addEffect(invisibility);
            }
            
            // Play smoke bomb sound effect
            serverLevel.playSound(
                null, // null means all nearby players will hear it
                x, y, z,
                SoundRegistry.SMOKE_BOMB,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f  // pitch
            );
            
            // Create a massive smoke cloud - smoke bomb effect!
            // Large puff of smoke particles
            for (int i = 0; i < 150; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 4.0;
                double offsetY = serverLevel.random.nextDouble() * 3.0;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 4.0;
                
                double velocityX = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                double velocityY = serverLevel.random.nextDouble() * 0.2;
                double velocityZ = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, velocityX, velocityY, velocityZ, 0.05);
            }
            
            // Add some campfire smoke for extra density
            for (int i = 0; i < 100; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                double offsetY = serverLevel.random.nextDouble() * 2.5;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                
                double velocityY = serverLevel.random.nextDouble() * 0.15 + 0.05;
                
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0, velocityY, 0, 0.03);
            }
            
            // Add explosion particles at the center for impact
            for (int i = 0; i < 20; i++) {
                double velocityX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                double velocityY = serverLevel.random.nextDouble() * 0.3;
                double velocityZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    x, y, z,
                    1, velocityX, velocityY, velocityZ, 0.1);
            }
            
            // Add some cloud particles for extra thickness
            for (int i = 0; i < 80; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 3.5;
                double offsetY = serverLevel.random.nextDouble() * 2.0;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 3.5;
                
                double velocityX = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                double velocityY = serverLevel.random.nextDouble() * 0.1 + 0.05;
                double velocityZ = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, velocityX, velocityY, velocityZ, 0.04);
            }
        }
    }
}
