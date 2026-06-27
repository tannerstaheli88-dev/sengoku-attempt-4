package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.GoryoEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Translucent outer layer for Goryo entity, similar to slime's outer layer.
 * Provides a ghostly, ethereal appearance.
 */
public class GoryoTranslucentLayer extends RenderLayer<GoryoEntity, IllagerModel<GoryoEntity>> {
    
    private static final ResourceLocation GORYO_OUTER_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/goryo_outer.png");
    private static final ResourceLocation GORYO_OUTER_ELITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/goryo_outer_elite.png");
    private final IllagerModel<GoryoEntity> model;
    
    public GoryoTranslucentLayer(RenderLayerParent<GoryoEntity, IllagerModel<GoryoEntity>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new IllagerModel<>(modelSet.bakeLayer(ModelLayers.VINDICATOR));
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GoryoEntity entity, 
                      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, 
                      float netHeadYaw, float headPitch) {
        
        if (entity.isInvisible()) {
            return;
        }
        
        // Sync model animations with parent
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        
        // Render translucent outer layer: treat as elite when MAX_HEALTH is high or entity has elite tag
        boolean isElite = entity.getAttributeValue(Attributes.MAX_HEALTH) >= 47.9D || entity.getTags().contains("sengoku_elite");
        ResourceLocation outerTexture = isElite ? GORYO_OUTER_ELITE_TEXTURE : GORYO_OUTER_TEXTURE;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(outerTexture));
        // Pack RGBA into an ARGB int (model.renderToBuffer expects an int color)
        int alpha = (int)(0.4F * 255.0F) & 0xFF; // 40% opacity
        int red = 255 & 0xFF;
        int green = 255 & 0xFF;
        int blue = 255 & 0xFF;
        int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
            OverlayTexture.NO_OVERLAY,
            color); // 40% opacity for ghostly effect
    }
}
