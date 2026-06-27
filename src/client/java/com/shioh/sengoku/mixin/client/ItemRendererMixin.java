package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.item.BasicWeaponItem;
import com.shioh.sengoku.item.SweeplessItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

  @ModifyVariable(
      method = "render",
      at = @At(value = "HEAD"),
      argsOnly = true
  )
  public BakedModel useHeldModels(
      BakedModel value,
      ItemStack itemStack,
      ItemDisplayContext displayContext,
      boolean leftHand,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int combinedLight,
      int combinedOverlay,
      BakedModel model
  ) {
    // Don’t interfere with models at all, just return vanilla/JSON-based
    return value;
  }
}
