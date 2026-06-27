package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.OnikumaModel;
import com.shioh.sengoku.entity.OnikumaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OnikumaRenderer extends MobRenderer<OnikumaEntity, OnikumaModel> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/onikuma.png");
    
    public OnikumaRenderer(EntityRendererProvider.Context context) {
        super(context, new OnikumaModel(context.bakeLayer(OnikumaModel.LAYER_LOCATION)), 0.7F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(OnikumaEntity entity) {
        return TEXTURE;
    }
}
