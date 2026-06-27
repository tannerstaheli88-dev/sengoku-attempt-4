package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.UmiBozuModel;
import com.shioh.sengoku.entity.UmiBozuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class UmiBozuRenderer extends LivingEntityRenderer<UmiBozuEntity, UmiBozuModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/umi_bozu.png");

    public UmiBozuRenderer(EntityRendererProvider.Context context) {
        super(context, new UmiBozuModel(context.bakeLayer(UmiBozuModel.LAYER_LOCATION)), 2.5F);
    }

    @Override
    protected void scale(UmiBozuEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(4.5F, 4.5F, 4.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UmiBozuEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(UmiBozuEntity entity) {
        return false;
    }
}