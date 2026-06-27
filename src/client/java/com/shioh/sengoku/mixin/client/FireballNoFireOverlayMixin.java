package com.shioh.sengoku.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the fire overlay from fireballs and adds soul fire particles in the Nether.
 * This prevents the fire_0/fire_1 textures from rendering on fireballs and replaces them with particles.
 */
@Mixin(EntityRenderDispatcher.class)
public class FireballNoFireOverlayMixin {
    
    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void sengoku$removeFireOverlayForFireball(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity, Quaternionf quaternionf, CallbackInfo ci) {
        // Cancel fire overlay rendering for Fireballs and spawn flame particles
        if (entity instanceof Fireball fireball && fireball.level() instanceof ClientLevel clientLevel) {
            ci.cancel();
            
            // Choose particle type based on dimension (soul fire in Nether, regular fire elsewhere)
            boolean isNether = fireball.level().dimension() == Level.NETHER;
            
            // Spawn particles around the fireball
            for (int i = 0; i < 3; ++i) {
                double xOffset = fireball.getX() + (fireball.getRandom().nextDouble() - 0.5) * 0.5;
                double yOffset = fireball.getY() + fireball.getRandom().nextDouble() * 0.5;
                double zOffset = fireball.getZ() + (fireball.getRandom().nextDouble() - 0.5) * 0.5;
                
                clientLevel.addParticle(
                    isNether ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
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
