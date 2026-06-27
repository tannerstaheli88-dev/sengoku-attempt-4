package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VindicatorRenderer.class)
public class VindicatorRendererMixin {
    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void getTextureLocation(Vindicator vindicator, CallbackInfoReturnable<ResourceLocation> cir) {
        if (vindicator.getMaxHealth() >= 48.0F) {
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/vindicator_elite.png"));
        }
    }
}