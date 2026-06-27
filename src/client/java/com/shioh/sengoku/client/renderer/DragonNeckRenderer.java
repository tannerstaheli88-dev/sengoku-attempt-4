package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonNeckModel;
import com.shioh.sengoku.entity.DragonNeckEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonNeckRenderer extends LivingEntityRenderer<DragonNeckEntity, DragonNeckModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonNeckRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonNeckModel(context.bakeLayer(DragonNeckModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    protected void scale(DragonNeckEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonNeckEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonNeckEntity entity) {
        return false;
    }
}
