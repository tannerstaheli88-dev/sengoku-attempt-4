package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.OniBruteEntity;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.client.model.OniBruteModel;
import net.minecraft.client.model.ZombieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Oni Brute. Reuses the vanilla zombie/husk model and a custom texture.
 */
public class OniBruteRenderer extends MobRenderer<OniBruteEntity, OniBruteModel> {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/oni_brute.png");

    public OniBruteRenderer(EntityRendererProvider.Context context) {
        // Larger shadow for significantly larger model
        super(context, new OniBruteModel(context.bakeLayer(OniBruteModel.LAYER_LOCATION)), 1.6F);
        // Render held items (stone kanabo etc.) in the Oni Brute's hands
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(OniBruteEntity entity) {
        return TEXTURE;
    }
    
    @Override
    protected void scale(OniBruteEntity entity, PoseStack poseStack, float partialTickTime) {
        // Make the model visually larger to match the hitbox increase
        float scale = 1.8F; // ~80% larger than vanilla zombie model
        poseStack.scale(scale, scale, scale);
    }
}
