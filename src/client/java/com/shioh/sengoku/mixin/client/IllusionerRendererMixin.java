package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Illusioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IllusionerRenderer.class)
public class IllusionerRendererMixin {
    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void getTextureLocation(Illusioner illusioner, CallbackInfoReturnable<ResourceLocation> cir) {
        if (illusioner.getMaxHealth() >= 64.0F) {
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/illusioner_elite.png"));
        }
    }
}