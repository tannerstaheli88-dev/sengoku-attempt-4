package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.MaikubiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Maikubi model - converted from Blockbench
 */
public class MaikubiModel extends EntityModel<MaikubiEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "maikubi"), "main");

    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;

    public MaikubiModel(ModelPart root) {
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head1",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46)
                .addBox(-4.5F, -4.5F, -4.25F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head2",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 50)
                .addBox(-4.5F, -4.5F, -4.25F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-8.0F, 4.0F, 0.0F, 0.0F, 0.0F, -0.48F));

        partdefinition.addOrReplaceChild("head3",
            CubeListBuilder.create()
                .texOffs(7, 34)
                .addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 36)
                .addBox(-4.5F, -4.5F, -4.25F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(10.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.48F));

        partdefinition.addOrReplaceChild("body1",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(3, 83)
                .addBox(-10.0F, 3.9F, 3.5F, 20.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
            CubeListBuilder.create()
                .texOffs(0, 22)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 22)
                .addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 22)
                .addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 22)
                .addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 6.9F, -0.5F));

        partdefinition.addOrReplaceChild("body3",
            CubeListBuilder.create()
                .texOffs(12, 22)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 16.9F, -0.5F));

        return LayerDefinition.create(meshdefinition, 120, 120);
    }

    @Override
    public void setupAnim(MaikubiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Heads keep their default (model) rotations; do not follow look direction.
        // Any dynamic head animation is handled by the resourcepack CEM animations.
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
