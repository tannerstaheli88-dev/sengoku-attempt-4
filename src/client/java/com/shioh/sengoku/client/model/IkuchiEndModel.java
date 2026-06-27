package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.IkuchiEndEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class IkuchiEndModel extends HierarchicalModel<IkuchiEndEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "ikuchi_end"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tailFin;

    public IkuchiEndModel(ModelPart root) {
        this.root    = root;
        this.body    = root.getChild("body");
        this.tailFin = root.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        // Tapered tail segment — smaller than body segments
        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -5.0F, 5, 5, 8),
            PartPose.offset(0.0F, 17.5F, 0.0F));

        // Tail fin — flat horizontal fluke
        parts.addOrReplaceChild("tail_fin",
            CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -0.5F, 0.0F, 8, 1, 4),
            PartPose.offset(0.0F, 17.5F, 3.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(IkuchiEndEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        // Gentle tail fin wag
        this.tailFin.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.4F;
        this.root.xRot = 0.0F;
        this.root.yRot = 0.0F;
        this.root.zRot = 0.0F;
    }
}
