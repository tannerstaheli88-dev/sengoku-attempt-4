package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.SarugamiModel;
import com.shioh.sengoku.entity.SarugamiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class SarugamiRenderer extends MobRenderer<SarugamiEntity, SarugamiModel> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/sarugami.png");
    
    public SarugamiRenderer(EntityRendererProvider.Context context) {
        super(context, new SarugamiModel(context.bakeLayer(SarugamiModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    
    @Override
    public ResourceLocation getTextureLocation(SarugamiEntity entity) {
        return TEXTURE;
    }
}
