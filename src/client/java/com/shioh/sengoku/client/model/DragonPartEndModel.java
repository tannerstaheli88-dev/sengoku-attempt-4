package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.DragonPartEndEntity;
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
 * Slightly tapered end cap model for the dragon tail chain.
 */
public class DragonPartEndModel extends HierarchicalModel<DragonPartEndEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "dragon_part_end"),
        "main"
    );

    private final ModelPart root;

    public DragonPartEndModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.5F, -5.0F, -28.0F, 5.0F, 10.0F, 30.0F),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DragonPartEndEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = entity.getXRot() * ((float) Math.PI / 180F);
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
    }
}
