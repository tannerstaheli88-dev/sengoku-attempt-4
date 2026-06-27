package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.DragonNeckEntity;
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
 * Narrower neck segment model connecting the dragon head to the body chain.
 */
public class DragonNeckModel extends HierarchicalModel<DragonNeckEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "dragon_neck"),
        "main"
    );

    private final ModelPart root;

    public DragonNeckModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        // Narrower than body: 8 wide, 7 tall, 14 long
        root.addOrReplaceChild(
            "neck",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -4.0F, 16.0F, 16.0F, 48.0F),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DragonNeckEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = entity.getXRot() * ((float) Math.PI / 180F);
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
    }
}
