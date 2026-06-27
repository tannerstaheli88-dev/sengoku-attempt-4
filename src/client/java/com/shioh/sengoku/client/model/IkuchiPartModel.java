package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.IkuchiPartEntity;
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

public class IkuchiPartModel extends HierarchicalModel<IkuchiPartEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "ikuchi_part"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;

    public IkuchiPartModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        // Cylindrical body segment — slightly narrower than the head
        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.5F, -5.0F, 7, 7, 10),
            PartPose.offset(0.0F, 17.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(IkuchiPartEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        // Segments don't independently animate — chain logic handles orientation
        this.root.xRot = 0.0F;
        this.root.yRot = 0.0F;
        this.root.zRot = 0.0F;
    }
}
