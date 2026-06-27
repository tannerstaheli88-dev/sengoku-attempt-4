package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonArmsModel;
import com.shioh.sengoku.entity.DragonArmsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonArmsRenderer extends LivingEntityRenderer<DragonArmsEntity, DragonArmsModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonArmsRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonArmsModel(context.bakeLayer(DragonArmsModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    protected void scale(DragonArmsEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonArmsEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonArmsEntity entity) {
        return false;
    }
}
