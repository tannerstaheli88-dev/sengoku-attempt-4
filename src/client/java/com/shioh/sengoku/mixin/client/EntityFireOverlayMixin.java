package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Swaps the "on fire" texture overlay for entities to use soul fire textures in the Nether.
 * This affects the fire_0.png and fire_1.png textures that render on burning entities.
 */
@Mixin(EntityRenderer.class)
public class EntityFireOverlayMixin {

    @ModifyArg(
        method = "renderFlame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        ),
        index = 0
    )
    private ResourceLocation sengoku$swapEntityFireTexture(ResourceLocation texture, Entity entity) {
        // Check if entity is in the Nether
        if (entity.level() != null && entity.level().dimension() == Level.NETHER) {
            String path = texture.getPath();
            // Swap fire_0 and fire_1 to soul_fire variants
            if ("block/fire_0".equals(path)) {
                return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "block/soul_fire_0");
            } else if ("block/fire_1".equals(path)) {
                return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "block/soul_fire_1");
            }
        }
        return texture;
    }
}
