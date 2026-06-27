package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.IkuchiEntity;
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

public class IkuchiModel extends HierarchicalModel<IkuchiEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "ikuchi"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart finLeft;
    private final ModelPart finRight;

    public IkuchiModel(ModelPart root) {
        this.root   = root;
        this.head     = root.getChild("head");
        this.body     = root.getChild("body");
        this.finLeft  = root.getChild("fin_left");
        this.finRight = root.getChild("fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        // Elongated eel head
        parts.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -8.0F, 6, 6, 8),
            PartPose.offset(0.0F, 17.0F, 0.0F));

        // Main body segment
        parts.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -4.0F, 0.0F, 8, 8, 10),
            PartPose.offset(0.0F, 16.0F, 0.0F));

        // Pectoral fins
        parts.addOrReplaceChild("fin_left",
            CubeListBuilder.create().texOffs(36, 0).addBox(0.0F, -1.0F, -1.0F, 6, 1, 4),
            PartPose.offset(4.0F, 17.0F, 2.0F));

        parts.addOrReplaceChild("fin_right",
            CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-6.0F, -1.0F, -1.0F, 6, 1, 4),
            PartPose.offset(-4.0F, 17.0F, 2.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(IkuchiEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        // Gentle fin undulation
        float wave = ageInTicks * 0.1F;
        this.finLeft.zRot  =  Mth.sin(wave) * 0.3F;
        this.finRight.zRot = -Mth.sin(wave) * 0.3F;
    }
}
