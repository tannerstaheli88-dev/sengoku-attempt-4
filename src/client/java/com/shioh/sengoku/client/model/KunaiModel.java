package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.KunaiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Kunai entity model - Created with Blockbench 5.0.2
 */
public class KunaiModel extends EntityModel<KunaiEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "kunai"), "main");
    
    private final ModelPart cross_1;
    private final ModelPart cross_2;

    public KunaiModel(ModelPart root) {
        this.cross_1 = root.getChild("cross_1");
        this.cross_2 = root.getChild("cross_2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Cross 1 - First blade cross section
        partdefinition.addOrReplaceChild("cross_1", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-12.0F, -2.0F, 0.0F, 16.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), 
            PartPose.offsetAndRotation(0.0F, 20.5F, -4.0F, -2.3562F, 1.5708F, 0.0F));

        // Cross 2 - Second blade cross section (rotated 45 degrees)
        partdefinition.addOrReplaceChild("cross_2", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-12.0F, -2.0F, 0.0F, 16.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), 
            PartPose.offsetAndRotation(0.0F, 20.5F, -4.0F, -0.7854F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(KunaiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // No animation needed for projectile
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        cross_1.render(poseStack, buffer, packedLight, packedOverlay, color);
        cross_2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
