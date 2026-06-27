package com.shioh.sengoku.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Swaps entity flame quads to use soul fire textures for all entities
 * when in the Nether (and optionally Deep Dark later).
 */
@Mixin(net.minecraft.client.renderer.entity.EntityRenderer.class)
public abstract class SoulFireEntityFlameMixin {

    // Redirect the sprite resource id used by EntityRenderer.renderFlame from fire_0/fire_1 to soul_fire_0/_1
    @ModifyArg(method = "renderFlame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"), index = 0)
    private ResourceLocation sengoku$swapFlameSprite(ResourceLocation id) {
        if (id == null) return id;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return id;
        boolean inNether = mc.level.dimension() == Level.NETHER;
        if (!inNether) return id;
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
