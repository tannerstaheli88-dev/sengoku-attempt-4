package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.UmiBozuEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class UmiBozuModel extends EntityModel<UmiBozuEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "umi_bozu"), "main");

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tail;

    public UmiBozuModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Head: roughly 3x the kojin head
        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-12.0F, -24.0F, -12.0F, 24, 24, 24),
            PartPose.offset(0.0F, -6.0F, 0.0F));

        // Headwear: ornate, ceremonial hat
        root.addOrReplaceChild("headwear", CubeListBuilder.create()
            .texOffs(0, 72).addBox(-10.0F, -26.0F, -10.0F, 20, 4, 20),
            PartPose.offset(0.0F, -6.0F, 0.0F));

        // Body: wide, looming torso
        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 48).addBox(-12.0F, 0.0F, -6.0F, 24, 32, 12),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Arms: long and heavy, hang low
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(96, 0).addBox(-2.0F, -4.0F, -4.0F, 10, 36, 8),
            PartPose.offset(14.0F, 4.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(96, 0).mirror().addBox(-8.0F, -4.0F, -4.0F, 10, 36, 8),
            PartPose.offset(-14.0F, 4.0F, 0.0F));

        // Tail: massive, submerged lower half — no legs, just a pillar of darkness
        root.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(0, 96).addBox(-10.0F, 0.0F, -6.0F, 20, 40, 12),
            PartPose.offset(0.0F, 30.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(UmiBozuEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Slow head tracking
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.6F;
        this.head.xRot = headPitch * ((float)Math.PI / 180F) * 0.4F;
        this.headwear.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.6F;
        this.headwear.xRot = headPitch * ((float)Math.PI / 180F) * 0.4F;

        // Arms sway slowly and independently — unsettling, not humanlike
        this.leftArm.xRot  = (float)Math.sin(ageInTicks * 0.04F) * 0.15F;
        this.leftArm.zRot  = (float)Math.cos(ageInTicks * 0.03F) * 0.08F - 0.1F;
        this.rightArm.xRot = (float)Math.sin(ageInTicks * 0.04F + Math.PI) * 0.15F;
        this.rightArm.zRot = (float)Math.cos(ageInTicks * 0.03F + Math.PI) * 0.08F + 0.1F;

        // Tail bobs subtly — like something massive drifting in deep water
        this.tail.xRot = (float)Math.sin(ageInTicks * 0.03F) * 0.05F;
        this.tail.yRot = (float)Math.sin(ageInTicks * 0.025F) * 0.04F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}