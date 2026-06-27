package com.shioh.sengoku.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Swaps the first-person fire overlay to soul fire textures when in Nether
 * and the player is in lava.
 */
@Mixin(LevelRenderer.class)
public abstract class SoulFireFireOverlayMixin {

    @ModifyArg(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"), index = 0)
    private ResourceLocation sengoku$swapOverlay(ResourceLocation id) {
        if (id == null) return id;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return id;
        boolean inNether = mc.level.dimension() == Level.NETHER;
        if (!(inNether && mc.player.isInLava())) return id;
        String path = id.getPath();
        if ("block/fire_0".equals(path)) {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/soul_fire_0");
        }
        if ("block/fire_1".equals(path)) {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/soul_fire_1");
        }
        return id;
    }
}
