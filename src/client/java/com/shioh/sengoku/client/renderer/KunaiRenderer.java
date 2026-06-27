package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.shioh.sengoku.client.model.KunaiModel;
import com.shioh.sengoku.entity.KunaiEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renderer for the kunai entity using a Blockbench model.
 * 
 * To customize the model:
 * 1. Create your kunai in Blockbench (Java Entity Model)
 * 2. Export the model code
 * 3. Replace the createBodyLayer() method in KunaiModel.java with your exported code
 * 4. Update the texture path below if needed
 */
public class KunaiRenderer extends EntityRenderer<KunaiEntity> {
    private static final ResourceLocation KUNAI_TEXTURE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/projectiles/kunai.png");
    private final KunaiModel model;
    
    public KunaiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new KunaiModel(context.bakeLayer(KunaiModel.LAYER_LOCATION));
    }
    
    @Override
    public void render(KunaiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            // Rotate to match velocity direction (like an arrow)
            // First apply yaw (horizontal rotation)
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
            // Then apply pitch (vertical rotation) - inverted to match flight direction
            poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

            // Adjust position and scale
            poseStack.translate(0.0F, -1.25F, 0.0F); // Center the model
            poseStack.scale(1.0F, 1.0F, 1.0F);

            // Render the model with proper render type
            net.minecraft.client.renderer.RenderType renderType = net.minecraft.client.renderer.RenderType.entityCutoutNoCull(KUNAI_TEXTURE);
            this.model.renderToBuffer(poseStack, buffer.getBuffer(renderType), packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        } finally {
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(KunaiEntity entity) {
        return KUNAI_TEXTURE;
    }
}
