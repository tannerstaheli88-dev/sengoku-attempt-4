package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.DragonHeadEntity;
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
 * Larger head segment to serve as the visible front of the replacement dragon chain.
 */
public class DragonHeadModel extends HierarchicalModel<DragonHeadEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "dragon_head"),
        "main"
    );

    private final ModelPart root;

    public DragonHeadModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-5.0F, -8.0F, -20.0F, 10.0F, 16.0F, 20.0F),
            PartPose.offset(0.0F, 19.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DragonHeadEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().xRot = entity.getXRot() * ((float) Math.PI / 180F);
        this.root().yRot = 0.0F;
        this.root().zRot = 0.0F;
    }
}
