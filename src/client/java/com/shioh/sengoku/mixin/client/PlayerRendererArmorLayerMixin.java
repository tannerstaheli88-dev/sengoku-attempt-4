package com.shioh.sengoku.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides Shinobi armor pieces when the wearer is invisible by skipping
 * their render calls inside the humanoid armor layer. This avoids
 * brittle reflection against player renderer internals.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class PlayerRendererArmorLayerMixin {

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void sengoku$hideAllArmorWhenPlayerInvisible(PoseStack poseStack,
                                                         MultiBufferSource buffers,
                                                         LivingEntity entity,
                                                         EquipmentSlot slot,
                                                         int packedLight,
                                                         HumanoidModel<?> model,
                                                         CallbackInfo ci) {
        if (entity == null) return;
        // Only affect players to avoid unintended mob visuals
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) return;
        if (entity.isInvisible()) {
            ci.cancel();
        }
    }
}
