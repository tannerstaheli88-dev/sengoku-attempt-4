package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.AkugyoEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple Akugyo model (guardian-like silhouette) intended to be easily edited in ETF CEM.
 */
public class AkugyoModel extends EntityModel<AkugyoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "akugyo"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftFin;
    private final ModelPart rightFin;
    private final ModelPart tail;

    public AkugyoModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftFin = root.getChild("left_fin");
        this.rightFin = root.getChild("right_fin");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-5.0F, -6.0F, -5.0F, 10, 10, 10), PartPose.offset(0.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 20).addBox(-6.0F, 0.0F, -3.0F, 12, 10, 6), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_fin", CubeListBuilder.create()
            .texOffs(48, 0).addBox(0.0F, 0.0F, -1.0F, 8, 1, 6), PartPose.offset(6.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_fin", CubeListBuilder.create()
            .texOffs(48, 0).mirror().addBox(-8.0F, 0.0F, -1.0F, 8, 1, 6), PartPose.offset(-6.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(48, 8).addBox(-2.0F, 0.0F, 0.0F, 4, 8, 2), PartPose.offset(0.0F, 8.0F, 3.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AkugyoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        float swim = ageInTicks * 0.1F;
        this.leftFin.zRot = (float)Math.sin(swim) * 0.2F;
        this.rightFin.zRot = -(float)Math.sin(swim) * 0.2F;
        this.tail.xRot = (float)Math.sin(swim) * 0.15F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftFin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightFin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
