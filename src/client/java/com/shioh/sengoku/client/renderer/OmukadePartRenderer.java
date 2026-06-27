package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.OmukadePartModel;
import com.shioh.sengoku.entity.OmukadePartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for OmukadePart entity using the spider model.
 * Displays the body segments that follow the main Omukade.
 */
public class OmukadePartRenderer extends LivingEntityRenderer<OmukadePartEntity, OmukadePartModel> {
    private static final ResourceLocation OMUKADE_PART_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/omukade_part.png");
    private static final float PART_SCALE = 1.45F;
    
    public OmukadePartRenderer(EntityRendererProvider.Context context) {
        super(context, new OmukadePartModel(context.bakeLayer(OmukadePartModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    protected void scale(OmukadePartEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(PART_SCALE, PART_SCALE, PART_SCALE);
    }
    
    @Override
    public ResourceLocation getTextureLocation(OmukadePartEntity entity) {
        return OMUKADE_PART_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(OmukadePartEntity entity) {
        return false;
    }
}
