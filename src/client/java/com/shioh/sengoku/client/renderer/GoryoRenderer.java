package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.entity.GoryoEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Renderer for Goryo entity - vengeful ghost samurai.
 * Uses translucent rendering similar to slime outer layer.
 */
public class GoryoRenderer extends IllagerRenderer<GoryoEntity> {
    
    private static final ResourceLocation GORYO_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/goryo.png");
    private static final ResourceLocation GORYO_ELITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/goryo_elite.png");
    private static final double SENGOKU_ELITE_HEALTH_THRESHOLD = 49.9D;
    
    public GoryoRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        // Add translucent outer layer like slime
        this.addLayer(new GoryoTranslucentLayer(this, context.getModelSet()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(GoryoEntity entity) {
        if (entity.getAttributeValue(Attributes.MAX_HEALTH) >= SENGOKU_ELITE_HEALTH_THRESHOLD) {
            return GORYO_ELITE_TEXTURE;
        }
        return GORYO_TEXTURE;
    }
    
    @Override
    public void render(GoryoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
    
    @Override
    protected RenderType getRenderType(GoryoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        // Force translucent render type for the Goryo base layer so the
        // entity's main texture is rendered with ghostly transparency.
        ResourceLocation texture = this.getTextureLocation(entity);
        return RenderType.entityTranslucent(texture);
    }
}
