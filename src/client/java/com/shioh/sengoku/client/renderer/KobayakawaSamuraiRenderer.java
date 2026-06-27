package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.KobayakawaSamuraiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Renderer for Kobayakawa Samurai.
 * Uses Vindicator model - EMF will override with custom .jem if present.
 */
public class KobayakawaSamuraiRenderer extends IllagerRenderer<KobayakawaSamuraiEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/kobayakawa_samurai.png");
    private static final ResourceLocation ELITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/kobayakawa_samurai_elite.png");
    private static final double SENGOKU_ELITE_HEALTH_THRESHOLD = 47.9D;
    
    public KobayakawaSamuraiRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(KobayakawaSamuraiEntity entity) {
        if (entity.getAttributeValue(Attributes.MAX_HEALTH) >= SENGOKU_ELITE_HEALTH_THRESHOLD) {
            return ELITE_TEXTURE;
        }
        return TEXTURE;
    }

    @Override
    public void render(KobayakawaSamuraiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            if (entity.getAttributeValue(Attributes.MAX_HEALTH) >= SENGOKU_ELITE_HEALTH_THRESHOLD) {
                poseStack.scale(1.08F, 1.08F, 1.08F);
            }
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
