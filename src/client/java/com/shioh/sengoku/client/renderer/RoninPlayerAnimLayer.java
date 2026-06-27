package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.entity.RoninEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

/**
 * Layer that requests PlayerAnimator to play an animation for Ronin entities.
 *
 * This layer uses reflection so the mod does not require PlayerAnimator at
 * compile time. If PlayerAnimator is not present at runtime this layer is
 * a no-op.
 */
public class RoninPlayerAnimLayer extends RenderLayer<RoninEntity, IllagerModel<RoninEntity>> {

    public RoninPlayerAnimLayer(RenderLayerParent<RoninEntity, IllagerModel<RoninEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, RoninEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        // PlayerAnimator integration disabled — this layer is intentionally a no-op.
        return;
    }
}
