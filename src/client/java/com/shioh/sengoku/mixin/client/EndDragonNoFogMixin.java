package com.shioh.sengoku.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the End dragon “boss fog” by pushing fog start/end very far
 * when the player is in the End and an Ender Dragon is present.
 */
@Mixin(FogRenderer.class)
public class EndDragonNoFogMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void sengoku$disableEndDragonFog(Camera camera,
                                                    FogRenderer.FogMode fogType,
                                                    float viewDistance,
                                                    boolean thickFog,
                                                    float partialTick,
                                                    CallbackInfo ci) {
        if (camera == null) return;
        if (!(camera.getEntity().level() instanceof ClientLevel level)) return;

        ResourceKey<Level> dim = level.dimension();
        if (!dim.equals(Level.END)) return;

        // Look for an Ender Dragon anywhere nearby (large radius to be safe)
        AABB big = camera.getEntity().getBoundingBox().inflate(8192);
        boolean dragonAlive = !level.getEntitiesOfClass(EnderDragon.class, big).isEmpty();

        if (dragonAlive) {
            // Push fog far away to effectively disable it while dragon is alive
            RenderSystem.setShaderFogStart(1_000_000.0F);
            RenderSystem.setShaderFogEnd(1_000_100.0F);
            ci.cancel();
        }
    }
}
