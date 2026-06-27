package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.entity.ShinobiLordEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class ShinobiLordRenderer extends IllagerRenderer<ShinobiLordEntity> {

    private static final ResourceLocation SHINOBI_LORD_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/shinobi_lord.png");
    private static final ResourceLocation SHINOBI_LORD_ENRAGED_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/shinobi_lord_enraged.png");

    public ShinobiLordRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(ShinobiLordEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShinobiLordEntity entity) {
        try {
            if (entity.isPhaseTwo()) {
                return SHINOBI_LORD_ENRAGED_TEXTURE;
            }
        } catch (Throwable ignored) {}
        return SHINOBI_LORD_TEXTURE;
    }
}