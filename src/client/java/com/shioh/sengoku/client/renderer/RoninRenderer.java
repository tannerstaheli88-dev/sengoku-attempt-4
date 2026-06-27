package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.RoninEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.shioh.sengoku.client.renderer.RoninPlayerAnimLayer;

/**
 * Renderer for Ronin entity.
 * Ronin are exclusively Outlaws - no clan variants.
 */
public class RoninRenderer extends IllagerRenderer<RoninEntity> {
    
    // Ronin are exclusively Outlaws - no clan variants needed
    private static final ResourceLocation RONIN_BASE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/ronin.png");
    private static final ResourceLocation RONIN_ELITE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/ronin_elite.png");
    private static final double SENGOKU_ELITE_HEALTH_THRESHOLD = 59.9D;
    
    public RoninRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new RoninPlayerAnimLayer(this));
    }
    
    @Override
    public ResourceLocation getTextureLocation(RoninEntity entity) {
        if (entity.getAttributeValue(Attributes.MAX_HEALTH) >= SENGOKU_ELITE_HEALTH_THRESHOLD) {
            return RONIN_ELITE;
        }
        return RONIN_BASE;
    }

    @Override
    public void render(RoninEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
