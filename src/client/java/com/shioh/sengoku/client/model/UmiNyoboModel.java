package com.shioh.sengoku.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.UmiNyoboEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Simple humanoid model for Umi Nyobo, matching Sarugami/Onikuma style.
 */
public class UmiNyoboModel extends EntityModel<UmiNyoboEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "umi_nyobo"), "main");

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public UmiNyoboModel(ModelPart root) {
        this.head = root.getChild("head");
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition headwear = root.addOrReplaceChild("headwear", CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, -4.0F, -2.0F, 8, 12, 4)
            .texOffs(16, 32).addBox(-4.5F, -4.5F, -2.5F, 9, 13, 5, new CubeDeformation(0.2F)), PartPose.offset(0.0F, -2.0F, 0.0F));


        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(48, 16).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(5.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(48, 16).mirror().addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(-5.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(2.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(0, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(-2.0F, 8.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(UmiNyoboEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;
        this.headwear.zRot = Mth.sin(ageInTicks * 0.06F) * 0.03F;

        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        if (entity.isAggressive()) {
            this.rightArm.xRot = -1.2F + this.rightArm.xRot * 0.5F;
            this.leftArm.xRot = -1.0F + this.leftArm.xRot * 0.5F;
        }

        // Apply bow-and-arrow pose when entity is using a bow
        try {
            net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose pose = entity.getArmPose();
            if (pose == net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose.BOW_AND_ARROW && entity.isUsingItem() && entity.hasRangedWeapon()) {
                // Static aiming pose: both arms raised, slight inward yaw so hands meet
                float aimX = -1.2F; // pitch for arms raised to aim
                float inward = 0.28F; // inward yaw
                boolean mainIsRight = entity.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;

                this.rightArm.xRot = aimX;
                this.leftArm.xRot = aimX;

                if (mainIsRight) {
                    this.rightArm.yRot = -inward;
                    this.leftArm.yRot = inward;
                } else {
                    this.rightArm.yRot = inward;
                    this.leftArm.yRot = -inward;
                }
            }
        } catch (Throwable ignored) {}

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
