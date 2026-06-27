package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonTailModel;
import com.shioh.sengoku.entity.DragonTailEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonTailRenderer extends LivingEntityRenderer<DragonTailEntity, DragonTailModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonTailRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonTailModel(context.bakeLayer(DragonTailModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    protected void scale(DragonTailEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonTailEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonTailEntity entity) {
        return false;
    }
}
