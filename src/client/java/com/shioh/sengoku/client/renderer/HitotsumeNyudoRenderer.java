package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.HitotsumeNyudoModel;
import com.shioh.sengoku.entity.HitotsumeNyudoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HitotsumeNyudoRenderer extends MobRenderer<HitotsumeNyudoEntity, HitotsumeNyudoModel> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/hitotsume_nyudo.png");
    
    public HitotsumeNyudoRenderer(EntityRendererProvider.Context context) {
        super(context, new HitotsumeNyudoModel(context.bakeLayer(HitotsumeNyudoModel.LAYER_LOCATION)), 1.0F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(HitotsumeNyudoEntity entity) {
        return TEXTURE;
    }
}
