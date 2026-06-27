package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.GakiEntity;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Renders Gaki using the vanilla Husk/Zombie model with a custom texture.
 */
public class GakiRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/gaki.png");

    public GakiRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel(context.bakeLayer(ModelLayers.HUSK)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}
