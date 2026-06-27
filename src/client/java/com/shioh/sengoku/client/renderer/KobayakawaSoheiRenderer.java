package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.KobayakawaSoheiEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Kobayakawa Sohei.
 * Uses Evoker model - EMF will override with custom .jem if present.
 */
public class KobayakawaSoheiRenderer extends IllagerRenderer<KobayakawaSoheiEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/kobayakawa_sohei.png");
    
    public KobayakawaSoheiRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(KobayakawaSoheiEntity entity) {
        return TEXTURE;
    }
}
