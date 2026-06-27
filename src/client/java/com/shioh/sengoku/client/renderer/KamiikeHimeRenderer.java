package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.KamiikeHimeEntity;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Renders Kamiike Hime using the vanilla Guardian model with a custom texture.
 */
public class KamiikeHimeRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/kamiike_hime.png");

    public KamiikeHimeRenderer(EntityRendererProvider.Context context) {
        super(context, new GuardianModel(context.bakeLayer(ModelLayers.GUARDIAN)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        return TEXTURE;
    }
}
