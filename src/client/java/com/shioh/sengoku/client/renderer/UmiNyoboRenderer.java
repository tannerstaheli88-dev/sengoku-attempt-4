package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.UmiNyoboModel;
import com.shioh.sengoku.entity.UmiNyoboEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Umi Nyobo entity.
 */
public class UmiNyoboRenderer extends MobRenderer<UmiNyoboEntity, UmiNyoboModel> {

    private static final ResourceLocation UMI_NYOBO_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/umi_nyobo.png");

    public UmiNyoboRenderer(EntityRendererProvider.Context context) {
        super(context, new UmiNyoboModel(context.bakeLayer(UmiNyoboModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.shadowRadius *= 1.25F;
    }

    @Override
    protected void scale(UmiNyoboEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(UmiNyoboEntity entity) {
        return UMI_NYOBO_TEXTURE;
    }
}
