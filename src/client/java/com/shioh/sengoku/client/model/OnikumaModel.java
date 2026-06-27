package com.shioh.sengoku.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.OnikumaEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Simplified custom model for Onikuma based on provided OptiFine CEM JEM.
 * Geometry approximates head (with ears & horns), body, arms, legs, loincloth.
 * Animations reuse basic biped mechanics with slight ear sway.
 */
public class OnikumaModel extends EntityModel<OnikumaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "onikuma"), "main");

    private final ModelPart head;
    private final ModelPart headwear; // includes horns & hair
    private final ModelPart body;
    private final ModelPart loinclothFront;
    private final ModelPart loinclothBack;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart horns;

    public OnikumaModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.loinclothFront = body.getChild("loincloth_front");
        this.loinclothBack = body.getChild("loincloth_back");
        this.leftEar = headwear.getChild("left_ear");
        this.rightEar = headwear.getChild("right_ear");
        this.horns = headwear.getChild("horns");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), PartPose.offset(0, -4, 0));

        PartDefinition headwear = root.addOrReplaceChild("headwear", CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.25F)), PartPose.offset(0, -4, 0));

        PartDefinition horns = headwear.addOrReplaceChild("horns", CubeListBuilder.create()
            .texOffs(0, 38).addBox(-5.0F, -10.0F, -1.0F, 10, 6, 0), PartPose.offset(0, -2.0F, 0));
        headwear.addOrReplaceChild("left_ear", CubeListBuilder.create()
            .texOffs(40, 38).addBox(-7.0F, -6.0F, -1.5F, 3, 5, 3), PartPose.offset(0, 0, 0));
        headwear.addOrReplaceChild("right_ear", CubeListBuilder.create()
            .texOffs(40, 38).mirror().addBox(4.0F, -6.0F, -1.5F, 3, 5, 3), PartPose.offset(0, 0, 0));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4, -4, -2, 8, 12, 4)
            .texOffs(16, 32).addBox(-4.5F, -5F, -2.5F, 9, 13, 5, new CubeDeformation(0.25F)), PartPose.offset(0, -2, 0));

        body.addOrReplaceChild("loincloth_front", CubeListBuilder.create()
            .texOffs(0, 48).addBox(-4.5F, -8.0F, -0.1F, 9, 10, 0), PartPose.offset(0, 9.5F, -2.3F));
        body.addOrReplaceChild("loincloth_back", CubeListBuilder.create()
            .texOffs(18, 48).addBox(-4.5F, -8.0F, 0.1F, 9, 10, 0), PartPose.offset(0, 9.5F, 2.3F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(48, 16).addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, -2, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(48, 16).mirror().addBox(-3, -2, -2, 4, 12, 4), PartPose.offset(-5, -2, 0));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 8, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(0, 32).mirror().addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 8, 0));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(OnikumaEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;

        // Basic biped limb swing
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Ear idle sway
        float sway = Mth.sin(ageInTicks * 0.1F) * 0.15F;
        this.leftEar.zRot = sway;
        this.rightEar.zRot = -sway;
        this.horns.xRot = 0.2F + Mth.sin(ageInTicks * 0.05F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
