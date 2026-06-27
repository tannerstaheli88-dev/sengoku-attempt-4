package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonPartModel;
import com.shioh.sengoku.entity.DragonPartEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonPartRenderer extends LivingEntityRenderer<DragonPartEntity, DragonPartModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonPartRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonPartModel(context.bakeLayer(DragonPartModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    protected void scale(DragonPartEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonPartEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonPartEntity entity) {
        return false;
    }
}
