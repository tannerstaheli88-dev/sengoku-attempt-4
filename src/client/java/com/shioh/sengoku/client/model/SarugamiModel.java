package com.shioh.sengoku.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.SarugamiEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Simplified Sarugami model (monkey-like yokai) with tail and ears derived from JEM.
 */
public class SarugamiModel extends EntityModel<SarugamiEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "sarugami"), "main");

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public SarugamiModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tail = body.getChild("tail");
        this.leftEar = headwear.getChild("left_ear");
        this.rightEar = headwear.getChild("right_ear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), PartPose.offset(0, -4, 0));
        // Slim headwear container (no inflated overlay cube) – just ears.
        PartDefinition headwear = root.addOrReplaceChild("headwear", CubeListBuilder.create(), PartPose.offset(0, -4, 0));
        // Refined ear boxes: reduced depth to avoid "earmuff" block appearance.
        headwear.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 48).addBox(-7.0F, -6.0F, -1.0F, 3, 5, 2), PartPose.offset(0, 0, 0));
        headwear.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(4.0F, -6.0F, -1.0F, 3, 5, 2), PartPose.offset(0, 0, 0));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4, -4, -2, 8, 12, 4), PartPose.offset(0, -2, 0));
        body.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(40, 32).addBox(-1, 0, -1, 2, 2, 11), PartPose.offset(0, 7, 2.5F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 16).addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, -2, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 16).mirror().addBox(-3, -2, -2, 4, 12, 4), PartPose.offset(-5, -2, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 8, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 8, 0));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SarugamiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;

        // Standard walk
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Tail gentle sway, faster if aggressive
        float tailBase = 0.6F;
        float speed = entity.isAggressive() ? 0.25F : 0.1F;
        this.tail.xRot = tailBase + Mth.sin(ageInTicks * speed) * 0.3F;
        this.tail.yRot = Mth.cos(ageInTicks * speed * 0.7F) * 0.25F;

        float earSway = Mth.sin(ageInTicks * 0.12F) * 0.12F;
        this.leftEar.zRot = earSway;
        this.rightEar.zRot = -earSway;
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

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart modelpart = this.getArm(arm);
        modelpart.translateAndRotate(poseStack);
    }

    protected ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }
}
