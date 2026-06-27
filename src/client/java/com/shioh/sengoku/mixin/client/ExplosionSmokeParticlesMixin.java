package com.shioh.sengoku.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores the smoke particles that Mojang accidentally removed from TNT explosions.
 * This adds back the large smoke clouds that used to accompany explosion particles,
 * making explosions look more dramatic and realistic like they were in older versions.
 */
@Mixin(Explosion.class)
public class ExplosionSmokeParticlesMixin {

    @Shadow @Final private Level level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private float radius;

    @Inject(method = "finalizeExplosion", at = @At("TAIL"))
    private void addExplosionSmokeParticles(boolean spawnParticles, CallbackInfo ci) {
        // Only add particles on the client side and if particles are enabled
        if (!spawnParticles || !(this.level instanceof ClientLevel clientLevel)) {
            return;
        }
        
        // Increase particle counts aggressively to create dense cloud effects
        // Use a larger multiplier so explosions feel voluminous and dramatic.
        int smokeCount = (int)(this.radius * this.radius * 8);
        smokeCount = Math.min(400, Math.max(40, smokeCount)); // Clamp between 40-400 particles

        // No thinning: spawn as many as possible up to an absolute cap.
        int absoluteCap = 400;
        int spawned = 0;

        // Spawn dense large-smoke particles in a sphere around the explosion
        for (int i = 0; i < smokeCount && spawned < absoluteCap; i++) {
            // Random offset within explosion radius
            double offsetX = (clientLevel.random.nextDouble() - 0.5) * this.radius * 2.0;
            double offsetY = (clientLevel.random.nextDouble() - 0.5) * this.radius * 2.0;
            double offsetZ = (clientLevel.random.nextDouble() - 0.5) * this.radius * 2.0;

            // Velocity pointing outward from center — slightly stronger for cloud spread
            double velocityX = offsetX * 0.12;
            double velocityY = Math.abs(offsetY) * 0.12 + 0.18; // Upward lift
            double velocityZ = offsetZ * 0.12;

            clientLevel.addParticle(
                ParticleTypes.LARGE_SMOKE,
                this.x + offsetX,
                this.y + offsetY,
                this.z + offsetZ,
                velocityX,
                velocityY,
                velocityZ
            );

            spawned++;
        }
        
        // Add many cloud particles near the center for a thick explosion cloud
        int denseCloud = Math.min(absoluteCap / 2, smokeCount / 2);
        for (int i = 0; i < denseCloud; i++) {
            double offsetX = (clientLevel.random.nextDouble() - 0.5) * this.radius * 0.8;
            double offsetY = (clientLevel.random.nextDouble() - 0.5) * this.radius * 0.6;
            double offsetZ = (clientLevel.random.nextDouble() - 0.5) * this.radius * 0.8;

            double velocityX = (clientLevel.random.nextDouble() - 0.5) * 0.06;
            double velocityY = clientLevel.random.nextDouble() * 0.28 + 0.2; // stronger upward motion
            double velocityZ = (clientLevel.random.nextDouble() - 0.5) * 0.06;

            clientLevel.addParticle(
                ParticleTypes.CLOUD,
                this.x + offsetX,
                this.y + offsetY,
                this.z + offsetZ,
                velocityX,
                velocityY,
                velocityZ
            );
        }
    }
}
