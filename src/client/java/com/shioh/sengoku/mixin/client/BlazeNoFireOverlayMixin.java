package com.shioh.sengoku.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the fire overlay from Blazes.
 * Since we can't easily swap the texture, we just cancel the rendering for Blazes.
 * They'll still have the soul fire particles from BlazeSoulFireParticleMixin.
 */
@Mixin(EntityRenderDispatcher.class)
public class BlazeNoFireOverlayMixin {
    
    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void sengoku$removeFireOverlayForBlaze(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity, Quaternionf quaternionf, CallbackInfo ci) {
        // Cancel fire overlay rendering for Blazes
        if (entity instanceof Blaze) {
            ci.cancel();
        }
    }
}
