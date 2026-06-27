package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.OmukadeEndModel;
import com.shioh.sengoku.entity.OmukadeEndEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for OmukadeEndEntity.
 * Uses Omukade part geometry but a dedicated texture namespace for the tail segment.
 */
public class OmukadeEndRenderer extends LivingEntityRenderer<OmukadeEndEntity, OmukadeEndModel> {
    private static final ResourceLocation OMUKADE_END_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/omukade_end.png");
    private static final float END_SCALE = 1.45F;

    public OmukadeEndRenderer(EntityRendererProvider.Context context) {
        super(context, new OmukadeEndModel(context.bakeLayer(OmukadeEndModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    protected void scale(OmukadeEndEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(END_SCALE, END_SCALE, END_SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(OmukadeEndEntity entity) {
        return OMUKADE_END_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(OmukadeEndEntity entity) {
        return false;
    }
}
