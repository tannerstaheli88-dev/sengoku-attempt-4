package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.DragonArmsEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Arms/limb segment model — wider body with arm/wing stubs on each side.
 */
public class DragonArmsModel extends HierarchicalModel<DragonArmsEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "dragon_arms"),
        "main"
    );

    private final ModelPart root;

    public DragonArmsModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        // Central body section
        PartDefinition torso = root.addOrReplaceChild(
            "torso",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -27.0F, 16.0F, 16.0F, 24.0F),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );

        // Left arm/wing stub
        torso.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create()
                .texOffs(0, 22)
                .addBox(0.0F, -1.5F, -2.0F, 8.0F, 3.0F, 4.0F),
            PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F)
        );

        // Right arm/wing stub
        torso.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create()
                .texOffs(0, 22)
                .mirror()
                .addBox(-8.0F, -1.5F, -2.0F, 8.0F, 3.0F, 4.0F),
            PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F)
        );

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DragonArmsEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = entity.getXRot() * ((float) Math.PI / 180F);
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
    }
}
