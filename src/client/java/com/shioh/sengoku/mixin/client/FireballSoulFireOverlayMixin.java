package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Swaps the fire overlay texture to soul fire for fireballs in the Nether.
 * This changes the fire_0/fire_1 textures to soul_fire_0/soul_fire_1 for fireballs.
 */
@Mixin(ThrownItemRenderer.class)
public class FireballSoulFireOverlayMixin {
    
    @ModifyArg(
        method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        ),
        index = 0
    )
    private ResourceLocation sengoku$swapFireballFireTexture(ResourceLocation texture, Object entity) {
        // Check if the entity being rendered is a Fireball in the Nether
        if (entity instanceof Fireball fireball && fireball.level().dimension() == Level.NETHER) {
            String path = texture.getPath();
            if ("block/fire_0".equals(path)) {
                return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "block/soul_fire_0");
            } else if ("block/fire_1".equals(path)) {
                return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "block/soul_fire_1");
            }
        }
        return texture;
    }
}
