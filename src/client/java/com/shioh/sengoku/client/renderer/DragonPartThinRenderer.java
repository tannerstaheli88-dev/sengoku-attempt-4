package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonPartThinModel;
import com.shioh.sengoku.entity.DragonPartThinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonPartThinRenderer extends LivingEntityRenderer<DragonPartThinEntity, DragonPartThinModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonPartThinRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonPartThinModel(context.bakeLayer(DragonPartThinModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    protected void scale(DragonPartThinEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.35F, 1.35F, 1.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonPartThinEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonPartThinEntity entity) {
        return false;
    }
}