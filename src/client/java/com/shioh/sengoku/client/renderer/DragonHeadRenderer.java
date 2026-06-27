package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.client.model.DragonHeadModel;
import com.shioh.sengoku.entity.DragonHeadEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonHeadRenderer extends LivingEntityRenderer<DragonHeadEntity, DragonHeadModel> {

    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");

    public DragonHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonHeadModel(context.bakeLayer(DragonHeadModel.LAYER_LOCATION)), 0.95F);
    }

    @Override
    protected void scale(DragonHeadEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.6F, 1.6F, 1.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonHeadEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(DragonHeadEntity entity) {
        return false;
    }
}
