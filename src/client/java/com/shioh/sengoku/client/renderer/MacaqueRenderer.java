package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.MacaqueModel;
import com.shioh.sengoku.entity.MacaqueEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MacaqueRenderer extends MobRenderer<MacaqueEntity, MacaqueModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/macaque.png");

    public MacaqueRenderer(EntityRendererProvider.Context context) {
        super(context, new MacaqueModel(context.bakeLayer(MacaqueModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(MacaqueEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MacaqueEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            if (entity.isBaby()) {
                final float babyScale = 0.6F;
                poseStack.scale(babyScale, babyScale, babyScale);
                poseStack.translate(0.0D, 0.12D * (1.0D - babyScale), 0.0D);
            }
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
