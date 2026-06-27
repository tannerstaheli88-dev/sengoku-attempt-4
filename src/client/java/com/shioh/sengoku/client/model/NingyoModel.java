package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.NingyoEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple Ningyo model with editable boxes for ETF CEM.
 */
public class NingyoModel extends EntityModel<NingyoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sengoku", "ningyo"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tail;

    public NingyoModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(40, 16).mirror().addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Replace legs with a single merfolk-style tail (editable in ETF CEM)
        root.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(0, 32).addBox(-3.5F, 0.0F, -1.0F, 7, 14, 4), PartPose.offset(0.0F, 10.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(NingyoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        this.leftArm.xRot = (float)Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = (float)Math.cos(limbSwing * 0.6662F + Math.PI) * 1.4F * limbSwingAmount;
        // Tail sway for merfolk locomotion
        this.tail.xRot = (float)Math.sin(ageInTicks * 0.12F) * 0.15F;
        this.tail.yRot = (float)Math.sin(ageInTicks * 0.07F) * 0.12F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
