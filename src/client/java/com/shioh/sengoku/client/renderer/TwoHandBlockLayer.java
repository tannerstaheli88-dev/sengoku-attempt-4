package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.AbstractIllager;

/**
 * Stubbed TwoHandBlockLayer: left as a no-op to avoid private-field access
 * and generic issues. The two-handed blocking pose is implemented via a
 * client mixin (`TwoHandBlockMixin`) instead.
 */
public class TwoHandBlockLayer<T extends AbstractIllager> extends RenderLayer<T, IllagerModel<T>> {

    public TwoHandBlockLayer(RenderLayerParent<T, IllagerModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource buffers, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // No-op: rendering handled by mixin
    }
}
