package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.shioh.sengoku.entity.MaikubiEntity;
import com.shioh.sengoku.client.model.MaikubiModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Maikubi entity - uses the Wither's model and similar appearance.
 */
public class MaikubiRenderer extends MobRenderer<MaikubiEntity, MaikubiModel> {
    
    private static final ResourceLocation MAIKUBI_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/maikubi.png");
    
    public MaikubiRenderer(EntityRendererProvider.Context context) {
        super(context, new MaikubiModel(context.bakeLayer(MaikubiModel.LAYER_LOCATION)), 1.0F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(MaikubiEntity entity) {
        return MAIKUBI_TEXTURE;
    }
    
    @Override
    protected int getBlockLightLevel(MaikubiEntity entity, BlockPos pos) {
        // Make Maikubi glow slightly like the Wither
        return 15;
    }
    
    @Override
    protected void scale(MaikubiEntity entity, PoseStack poseStack, float partialTickTime) {
        // Scale slightly smaller than the wither to differentiate it as a standard enemy
        float scale = 0.75F;
        poseStack.scale(scale, scale, scale);
    }

    // Prevent the model from rotating when the entity turns by forcing zero yaw
    // when delegating to the superclass render pipeline.
    @Override
    public void render(MaikubiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Call super.render with yaw forced to 0 so the model does not rotate with the entity.
        super.render(entity, 0.0F, partialTicks, poseStack, bufferSource, packedLight);
    }
}
