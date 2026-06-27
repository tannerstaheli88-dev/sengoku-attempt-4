package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.OmukadeModel;
import com.shioh.sengoku.entity.OmukadeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Omukade entity using the spider model.
 * Displays the main Omukade body with spider appearance.
 */
public class OmukadeRenderer extends LivingEntityRenderer<OmukadeEntity, OmukadeModel> {
    private static final ResourceLocation OMUKADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/omukade.png");
    private static final float MAIN_SCALE = 1.75F;
    
    public OmukadeRenderer(EntityRendererProvider.Context context) {
        super(context, new OmukadeModel(context.bakeLayer(OmukadeModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    protected void scale(OmukadeEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(MAIN_SCALE, MAIN_SCALE, MAIN_SCALE);
    }
    
    @Override
    public ResourceLocation getTextureLocation(OmukadeEntity entity) {
        return OMUKADE_TEXTURE;
    }
    
    @Override
    protected boolean shouldShowName(OmukadeEntity entity) {
        return false;  // Hide name tag
    }
}
