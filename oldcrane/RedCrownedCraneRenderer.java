package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.client.model.RedCrownedCraneModel;
import com.shioh.sengoku.entity.RedCrownedCraneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RedCrownedCraneRenderer extends MobRenderer<RedCrownedCraneEntity, RedCrownedCraneModel> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/red_crowned_crane.png");
    private static final ResourceLocation BABY_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/red_crowned_crane_baby.png");
    
    public RedCrownedCraneRenderer(EntityRendererProvider.Context context) {
        super(context, new RedCrownedCraneModel(context.bakeLayer(RedCrownedCraneModel.LAYER_LOCATION)), 0.5F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(RedCrownedCraneEntity entity) {
        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }

    @Override
    public void render(RedCrownedCraneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            if (entity.isBaby()) {
                // Apply uniform scale for baby cranes. Adjust translate to keep feet grounded.
                final float babyScale = 0.6F; // conservative baby size (60%)
                // Scale around origin, then translate down slightly so feet remain near ground.
                poseStack.scale(babyScale, babyScale, babyScale);
                // Translate downward after scaling so the baby's feet rest on the ground.
                // Reduced magnitude so the baby doesn't sink into the ground.
                // Slightly raise the baby so feet aren't sunk — reduced downward translation.
                // For `babyScale = 0.6`, this now moves the model down by -0.25*(1-scale) = -0.1 units.
                    poseStack.translate(0.0D, 0.12D * (1.0D - babyScale), 0.0D);
            }
            else {
                // Reduce adult model size slightly (no model file changes).
                final float adultScale = 0.75F; // make adult cranes a bit smaller
                poseStack.scale(adultScale, adultScale, adultScale);
                // Slight vertical translation so feet remain roughly grounded after scaling.
                poseStack.translate(0.0D, 0.12D * (1.0D - adultScale), 0.0D);
            }
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
