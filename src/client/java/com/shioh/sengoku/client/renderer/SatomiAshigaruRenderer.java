package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.SatomiAshigaruEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Satomi Ashigaru.
 * Uses Pillager model - EMF will override with custom .jem if present.
 */
public class SatomiAshigaruRenderer extends IllagerRenderer<SatomiAshigaruEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/satomi_ashigaru.png");
    
    public SatomiAshigaruRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(SatomiAshigaruEntity entity) {
        return TEXTURE;
    }
}
