package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.CrowEntity;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for crow entity - uses vanilla parrot model with custom crow texture.
 */
public class CrowRenderer extends MobRenderer {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/crow.png");
    
    public CrowRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}
