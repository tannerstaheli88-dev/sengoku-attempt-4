package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.AkugyoEntity;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.client.model.AkugyoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import com.shioh.sengoku.entity.AkugyoEntity;

/**
 * Renders Akugyo using the custom editable `AkugyoModel` only.
 */
public class AkugyoRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/akugyo.png");
    private static final ResourceLocation TEXTURE_AGGRO = sengokuFabric.asId("textures/entity/akugyo_aggro.png");

    public AkugyoRenderer(EntityRendererProvider.Context context) {
        // Increase shadow radius to reflect larger visual size
        super(context, new AkugyoModel(context.bakeLayer(AkugyoModel.LAYER_LOCATION)), 2.5F);
        // Ensure no other layers/models (such as vanilla guardian overlays) remain attached
        try {
            this.layers.clear();
        } catch (Throwable ignored) {}
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        try {
            if (entity instanceof AkugyoEntity ak) {
                if (ak.getTarget() != null && !ak.isReturningToCenter()) {
                    return TEXTURE_AGGRO;
                }
            }
        } catch (Throwable ignored) {}
        return TEXTURE;
    }

    @Override
    protected void scale(net.minecraft.world.entity.LivingEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTickTime) {
        // Make Akugyo significantly larger than the vanilla Elder Guardian
        float scale = 2.6F; // 260% size
        poseStack.scale(scale, scale, scale);
    }
}
