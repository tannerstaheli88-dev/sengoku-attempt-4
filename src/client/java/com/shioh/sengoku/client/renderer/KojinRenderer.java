package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.KojinEntity;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.client.model.KojinModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Renders Kojin using the vanilla Drowned/Zombie model with a custom texture.
 */
public class KojinRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/kojin.png");

    public KojinRenderer(EntityRendererProvider.Context context) {
        super(context, new KojinModel(context.bakeLayer(KojinModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}
