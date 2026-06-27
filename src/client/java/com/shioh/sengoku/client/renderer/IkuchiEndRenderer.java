package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.IkuchiEndModel;
import com.shioh.sengoku.entity.IkuchiEndEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class IkuchiEndRenderer extends LivingEntityRenderer<IkuchiEndEntity, IkuchiEndModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/ikuchi_end.png");

    public IkuchiEndRenderer(EntityRendererProvider.Context context) {
        super(context, new IkuchiEndModel(context.bakeLayer(IkuchiEndModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    protected void scale(IkuchiEndEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(3.1F, 3.1F, 3.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(IkuchiEndEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(IkuchiEndEntity entity) {
        return false;
    }
}
