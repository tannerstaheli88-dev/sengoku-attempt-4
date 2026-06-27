package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.NingyoModel;
import com.shioh.sengoku.entity.NingyoEntity;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Renders Ningyo using the vanilla Drowned/Zombie model with a custom texture.
 */
public class NingyoRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/ningyo.png");

    public NingyoRenderer(EntityRendererProvider.Context context) {
        super(context, new NingyoModel(context.bakeLayer(NingyoModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}
