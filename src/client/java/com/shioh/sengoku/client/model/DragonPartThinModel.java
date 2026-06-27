package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.DragonPartThinEntity;
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
 * Thin body segment for the narrower rear half of the dragon.
 */
public class DragonPartThinModel extends HierarchicalModel<DragonPartThinEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "dragon_part_thin"),
        "main"
    );

    private final ModelPart root;

    public DragonPartThinModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

root.addOrReplaceChild(
    "thin_segment",
    CubeListBuilder.create()
        .texOffs(214, 151)
        .addBox(-4.5F, -6.0F, -15.0F, 9.0F, 12.0F, 28.0F),
    PartPose.offset(0.0F, 20.0F, 0.0F)
);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DragonPartThinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = entity.getXRot() * ((float) Math.PI / 180F);
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
    }
}