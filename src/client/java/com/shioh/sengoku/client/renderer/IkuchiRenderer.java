package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.IkuchiModel;
import com.shioh.sengoku.entity.IkuchiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class IkuchiRenderer extends LivingEntityRenderer<IkuchiEntity, IkuchiModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/ikuchi.png");

    public IkuchiRenderer(EntityRendererProvider.Context context) {
        super(context, new IkuchiModel(context.bakeLayer(IkuchiModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    protected void scale(IkuchiEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(3.5F, 3.5F, 3.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(IkuchiEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(IkuchiEntity entity) {
        return false;
    }
}
