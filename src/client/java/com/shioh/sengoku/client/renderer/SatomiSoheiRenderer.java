package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.SatomiSoheiEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Satomi Sohei.
 * Uses Evoker model - EMF will override with custom .jem if present.
 */
public class SatomiSoheiRenderer extends IllagerRenderer<SatomiSoheiEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/satomi_sohei.png");
    
    public SatomiSoheiRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(SatomiSoheiEntity entity) {
        return TEXTURE;
    }
}
