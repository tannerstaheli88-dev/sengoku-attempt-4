package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.YukiOnnaModel;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Yuki Onna (Snow Woman) entity.
 * Renders with custom model and texture.
 */
public class YukiOnnaRenderer extends MobRenderer<YukiOnnaEntity, YukiOnnaModel> {
    
    private static final ResourceLocation YUKI_ONNA_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/yuki_onna.png");

    private static final ResourceLocation YUKI_ONNA_AGGRO_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/yuki_onna_aggro.png");
    
    public YukiOnnaRenderer(EntityRendererProvider.Context context) {
        super(context, new YukiOnnaModel(context.bakeLayer(YukiOnnaModel.LAYER_LOCATION)), 0.5F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(YukiOnnaEntity entity) {
        return entity.isAggressive() || entity.getTarget() != null
            ? YUKI_ONNA_AGGRO_TEXTURE
            : YUKI_ONNA_TEXTURE;
    }
}