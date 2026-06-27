package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.IkuchiPartModel;
import com.shioh.sengoku.entity.IkuchiPartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class IkuchiPartRenderer extends LivingEntityRenderer<IkuchiPartEntity, IkuchiPartModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/ikuchi_part.png");

    public IkuchiPartRenderer(EntityRendererProvider.Context context) {
        super(context, new IkuchiPartModel(context.bakeLayer(IkuchiPartModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    protected void scale(IkuchiPartEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(3.3F, 3.3F, 3.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(IkuchiPartEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(IkuchiPartEntity entity) {
        return false;
    }
}
