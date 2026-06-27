package com.shioh.sengoku.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.HitotsumeNyudoEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Oversized cyclopean Hitotsume Nyudo model. Simplified: large head with single eye bulge, elongated body.
 */
public class HitotsumeNyudoModel extends EntityModel<HitotsumeNyudoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "hitotsume_nyudo"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart headwear;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    // Eye is baked directly into the head texture; no separate part.

    public HitotsumeNyudoModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.headwear = root.getChild("headwear");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        // No separate eye child; animation will use slight head roll if needed.
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-5, -9, -5, 10, 10, 10), PartPose.offset(0, -6, 0));
        // Add a subtle headwear overlay (slightly inflated) so cyclopean texture layers render.
        root.addOrReplaceChild("headwear", CubeListBuilder.create()
            .texOffs(0, 40).addBox(-5, -9, -5, 10, 10, 10, new CubeDeformation(0.2F)), PartPose.offset(0, -6, 0));
        // Eye quad integrated into head cube now: add a thin overlay face on front
        head.addOrReplaceChild("eye_overlay", CubeListBuilder.create()
            .texOffs(40, 0).addBox(-2.5F, -4.0F, -5.6F, 5, 5, 0), PartPose.offset(0, -1, 0));

        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 20).addBox(-5, -6, -3, 10, 18, 6), PartPose.offset(0, -2, 0));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(32, 20).addBox(-1, -2, -2, 4, 16, 4), PartPose.offset(6, -4, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(32, 20).mirror().addBox(-3, -2, -2, 4, 16, 4), PartPose.offset(-6, -4, 0));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(48, 20).addBox(-2, 0, -2, 4, 14, 4), PartPose.offset(2.5F, 12, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(48, 20).mirror().addBox(-2, 0, -2, 4, 14, 4), PartPose.offset(-2.5F, 12, 0));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(HitotsumeNyudoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Slow heavy limb movement
        float speedScale = 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount * speedScale;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.2F * limbSwingAmount * speedScale;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * speedScale;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * speedScale;

        // Optional subtle head roll instead of separate eye part
        this.head.zRot = Mth.sin(ageInTicks * 0.02F) * 0.02F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
