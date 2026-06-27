package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonPartEndModel;
import com.shioh.sengoku.entity.DragonPartEndEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonPartEndRenderer extends LivingEntityRenderer<DragonPartEndEntity, DragonPartEndModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonPartEndRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonPartEndModel(context.bakeLayer(DragonPartEndModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    protected void scale(DragonPartEndEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonPartEndEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonPartEndEntity entity) {
        return false;
    }
}
