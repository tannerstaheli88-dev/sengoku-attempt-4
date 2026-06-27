package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.BanditEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
// two-handed block layer removed; using mixin-based pose instead

public class BanditRenderer extends LivingEntityRenderer<BanditEntity, IllagerModel<BanditEntity>> {
    // Bandits are exclusively Outlaws - no clan variants needed
    private static final ResourceLocation BANDIT_BASE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/bandit.png");

    public BanditRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        
        // Add item holding layer so bandits can hold weapons
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        // Two-handed blocking handled by client mixin
    }

    @Override
    public ResourceLocation getTextureLocation(BanditEntity entity) {
        // Bandits are ALWAYS Outlaws - no clan variants
        return BANDIT_BASE;
    }

    @Override
    protected boolean shouldShowName(BanditEntity entity) {
        // Only show name tag if the entity has a custom name or is being looked at closely
        return entity.hasCustomName() && this.entityRenderDispatcher.crosshairPickEntity == entity;
    }
}