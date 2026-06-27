package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.OniBruteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * Custom OniBrute model created from scratch but structured like the vanilla zombie model.
 * Parts: head, headwear, body, left_arm, right_arm, left_leg, right_leg
 */
public class OniBruteModel extends EntityModel<OniBruteEntity> implements ArmedModel {
        public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "oni_brute"), "main");

    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public OniBruteModel(ModelPart root) {
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

        // Head (slightly larger than vanilla zombie)
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.5F, 9, 9, 9), PartPose.offset(0.0F, -5.0F, 0.0F));

        // Headwear overlay
        root.addOrReplaceChild("headwear", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.5F, -8.0F, -4.5F, 9, 9, 9, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -5.0F, 0.0F));

        // Body (taller / wider)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-5.0F, 0.0F, -3.0F, 10, 18, 6), PartPose.offset(0.0F, -5.0F, 0.0F));

        // Arms
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(44, 16).addBox(-1.5F, -2.0F, -2.0F, 4, 18, 4), PartPose.offset(5.0F, -4.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(44, 16).mirror().addBox(-2.5F, -2.0F, -2.0F, 4, 18, 4), PartPose.offset(-5.0F, -4.0F, 0.0F));

        // Legs
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4), PartPose.offset(2.0F, 6.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4), PartPose.offset(-2.0F, 6.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

        @Override
        public void setupAnim(OniBruteEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        float speedScale = 0.6F; // slower, heavier movement
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * speedScale;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * speedScale;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * speedScale;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * speedScale;

        // subtle breathing / idle
        this.body.xRot = Mth.sin(ageInTicks * 0.02F) * 0.02F;
    }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
                headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                head.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                body.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay);
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
