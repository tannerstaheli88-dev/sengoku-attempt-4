package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.UmiInuEntity;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.client.model.UmiInuModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UmiInuRenderer extends MobRenderer {

    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/umi_inu.png");
    private static final ResourceLocation TEXTURE_AGGRO = sengokuFabric.asId("textures/entity/umi_inu_aggro.png");

    public UmiInuRenderer(EntityRendererProvider.Context context) {
        super(context, new UmiInuModel(context.bakeLayer(UmiInuModel.LAYER_LOCATION)), 0.7F);
        try {
            this.layers.clear();
        } catch (Throwable ignored) {}
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        try {
            if (entity instanceof UmiInuEntity u && u.getTarget() != null) {
                return TEXTURE_AGGRO;
            }
        } catch (Throwable ignored) {}
        return TEXTURE;
    }

    @Override
    protected void scale(net.minecraft.world.entity.LivingEntity entity,
            com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }
}