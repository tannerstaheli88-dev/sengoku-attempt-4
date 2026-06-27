package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.OmukadePartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class OmukadePartModel extends HierarchicalModel<OmukadePartEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "omukade_part"),
        "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;
    private final ModelPart leg8;

    public OmukadePartModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.leg7 = root.getChild("leg7");
        this.leg8 = root.getChild("leg8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        parts.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, 15.0F, -3.0F)
        );
        parts.addOrReplaceChild(
            "body0",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offset(0.0F, 15.0F, 0.0F)
        );
        parts.addOrReplaceChild(
            "body1",
            CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F),
            PartPose.offset(0.0F, 15.0F, 9.0F)
        );

        parts.addOrReplaceChild(
            "leg1",
            CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(-4.0F, 15.0F, 2.0F)
        );
        parts.addOrReplaceChild(
            "leg2",
            CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(4.0F, 15.0F, 2.0F)
        );
        parts.addOrReplaceChild(
            "leg3",
            CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(-4.0F, 15.0F, 1.0F)
        );
        parts.addOrReplaceChild(
            "leg4",
            CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(4.0F, 15.0F, 1.0F)
        );
        parts.addOrReplaceChild(
            "leg5",
            CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(-4.0F, 15.0F, 0.0F)
        );
        parts.addOrReplaceChild(
            "leg6",
            CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(4.0F, 15.0F, 0.0F)
        );
        parts.addOrReplaceChild(
            "leg7",
            CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(-4.0F, 15.0F, -1.0F)
        );
        parts.addOrReplaceChild(
            "leg8",
            CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
            PartPose.offset(4.0F, 15.0F, -1.0F)
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(OmukadePartEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = 0.0F;
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
        this.head.yRot = 0.0F;
        this.head.xRot = 0.0F;

        float baseSpread = ((float) Math.PI / 4F);
        this.leg1.zRot = -baseSpread;
        this.leg2.zRot = baseSpread;
        this.leg3.zRot = -baseSpread * 0.74F;
        this.leg4.zRot = baseSpread * 0.74F;
        this.leg5.zRot = -baseSpread * 0.74F;
        this.leg6.zRot = baseSpread * 0.74F;
        this.leg7.zRot = -baseSpread;
        this.leg8.zRot = baseSpread;

        float ySpread = 0.3926991F;
        this.leg1.yRot = ySpread * 2.0F;
        this.leg2.yRot = -ySpread * 2.0F;
        this.leg3.yRot = ySpread;
        this.leg4.yRot = -ySpread;
        this.leg5.yRot = -ySpread;
        this.leg6.yRot = ySpread;
        this.leg7.yRot = -ySpread * 2.0F;
        this.leg8.yRot = ySpread * 2.0F;

        float swing0 = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount;
        float swing1 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float swing2 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float swing3 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI * 1.5F)) * 0.4F) * limbSwingAmount;
        float lift0 = Math.abs(Mth.sin(limbSwing * 0.6662F) * 0.4F) * limbSwingAmount;
        float lift1 = Math.abs(Mth.sin(limbSwing * 0.6662F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float lift2 = Math.abs(Mth.sin(limbSwing * 0.6662F + ((float) Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float lift3 = Math.abs(Mth.sin(limbSwing * 0.6662F + ((float) Math.PI * 1.5F)) * 0.4F) * limbSwingAmount;

        this.leg1.yRot += swing0;
        this.leg2.yRot -= swing0;
        this.leg3.yRot += swing1;
        this.leg4.yRot -= swing1;
        this.leg5.yRot += swing2;
        this.leg6.yRot -= swing2;
        this.leg7.yRot += swing3;
        this.leg8.yRot -= swing3;

        this.leg1.zRot += lift0;
        this.leg2.zRot -= lift0;
        this.leg3.zRot += lift1;
        this.leg4.zRot -= lift1;
        this.leg5.zRot += lift2;
        this.leg6.zRot -= lift2;
        this.leg7.zRot += lift3;
        this.leg8.zRot -= lift3;
    }
}