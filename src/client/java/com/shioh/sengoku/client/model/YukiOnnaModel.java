// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17+ for Yarn
package com.shioh.sengoku.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class YukiOnnaModel extends EntityModel<YukiOnnaEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		ResourceLocation.fromNamespaceAndPath("sengoku", "yuki_onna"), "main");
	private final ModelPart head;
	private final ModelPart headwear;
	private final ModelPart hair_right;
	private final ModelPart hair_left;
	private final ModelPart body;
	private final ModelPart jacket;
	private final ModelPart left_arm;
	private final ModelPart left_sleeve;
	private final ModelPart right_arm;
	private final ModelPart right_sleeve;
	private final ModelPart left_leg;
	private final ModelPart left_pants;
	private final ModelPart left_skirt;
	private final ModelPart right_leg;
	private final ModelPart right_pants;
	private final ModelPart right_skirt;
	public YukiOnnaModel(ModelPart root) {
		this.head = root.getChild("head");
		this.headwear = root.getChild("headwear");
		this.hair_right = root.getChild("hair_right");
		this.hair_left = root.getChild("hair_left");
		this.body = root.getChild("body");
		this.jacket = root.getChild("jacket");
		this.left_arm = root.getChild("left_arm");
		this.left_sleeve = root.getChild("left_sleeve");
		this.right_arm = root.getChild("right_arm");
		this.right_sleeve = root.getChild("right_sleeve");
		this.left_leg = root.getChild("left_leg");
		this.left_pants = root.getChild("left_pants");
		this.left_skirt = this.left_pants.getChild("left_skirt");
		this.right_leg = root.getChild("right_leg");
		this.right_pants = root.getChild("right_pants");
		this.right_skirt = this.right_pants.getChild("right_skirt");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 23).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition headwear = partdefinition.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
		.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 15.0F, 8.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition hair_right = partdefinition.addOrReplaceChild("hair_right", CubeListBuilder.create().texOffs(0, 39).addBox(-2.5F, -3.0F, 0.0F, 7.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -6.0F, 4.3F));

		PartDefinition hair_left = partdefinition.addOrReplaceChild("hair_left", CubeListBuilder.create().texOffs(14, 39).addBox(-4.5F, -3.0F, 0.0F, 7.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -6.0F, 4.3F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition jacket = partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(32, 35).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 15.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -2.0F, 0.0F));

		partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(56, 35).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, -2.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 63).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -2.0F, 0.0F));

		partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(68, 54).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, -2.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 70).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

		PartDefinition left_pants = partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(32, 70).addBox(-1.0F, -0.75F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.0F, 11.75F, 0.0F));

		PartDefinition left_skirt = left_pants.addOrReplaceChild("left_skirt", CubeListBuilder.create().texOffs(72, 17).addBox(-1.5F, 0.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(1.0F, 7.25F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 70).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition right_pants = partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(72, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition right_skirt = right_pants.addOrReplaceChild("right_skirt", CubeListBuilder.create().texOffs(72, 27).addBox(-2.5F, -0.5F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 7.5F, 0.0F));
		
		return LayerDefinition.create(meshdefinition, 128, 128);
	}
	
	@Override
	public void setupAnim(YukiOnnaEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Head rotation
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
		this.headwear.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.headwear.xRot = headPitch * ((float)Math.PI / 180F);

		// Determine current behaviour state
		boolean aggressive = entity.isAggressive() || entity.getTarget() != null; // Active attack mode
		boolean stalking = entity.isCreepy(); // Watching the player without moving
		float clampedSwing = Math.min(limbSwingAmount, 1.0F);
		boolean moving = clampedSwing > 0.05F && !aggressive; // Treat as walking movement only when not in attack mode
		float windStrength = aggressive ? 1.0F : (moving ? 0.4F : 0.1F);
		float windWave = windStrength * 0.12F;
		float time = ageInTicks * 0.2F;
		
		// === ARMS ===
		if (aggressive) {
			// AGGRESSIVE/ATTACK MODE: Fluid Y pose with gentle sway instead of a rigid stance
			float raisedBase = (float)Math.PI * 0.85F;
			float aggressiveSway = Mth.sin(time * 0.45F) * 0.12F;
			float aggressiveTwist = Mth.cos(time * 0.40F) * 0.10F;
			this.left_arm.xRot = raisedBase + aggressiveSway;
			this.left_arm.yRot = -0.18F + aggressiveTwist * 0.6F;
			this.left_arm.zRot = (float)Math.PI / 5F + aggressiveTwist * 0.2F;
			this.right_arm.xRot = raisedBase - aggressiveSway;
			this.right_arm.yRot = 0.18F - aggressiveTwist * 0.6F;
			this.right_arm.zRot = -(float)Math.PI / 5F + aggressiveTwist * 0.2F;
		} else if (moving && !stalking) {
			// WANDERING: Normal arm swing when walking
			this.left_arm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * clampedSwing;
			this.left_arm.yRot = 0.0F;
			this.left_arm.zRot = 0.0F;
			this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * clampedSwing;
			this.right_arm.yRot = 0.0F;
			this.right_arm.zRot = 0.0F;
		} else {
			// STALKING: Arms at sides, standing still
			this.left_arm.xRot = 0.0F;
			this.left_arm.yRot = 0.0F;
			this.left_arm.zRot = 0.0F;
			this.right_arm.xRot = 0.0F;
			this.right_arm.yRot = 0.0F;
			this.right_arm.zRot = 0.0F;
		}
		
		// Sleeves hug the arms but flutter when she is moving or aggressive
		this.left_sleeve.xRot = this.left_arm.xRot;
		this.left_sleeve.yRot = this.left_arm.yRot;
		this.left_sleeve.zRot = this.left_arm.zRot;
		this.right_sleeve.xRot = this.right_arm.xRot;
		this.right_sleeve.yRot = this.right_arm.yRot;
		this.right_sleeve.zRot = this.right_arm.zRot;
		if (aggressive) {
			float sleeveWave = Mth.sin(time * 1.25F) * (0.25F + windWave);
			float sleeveTwist = Mth.cos(time * 0.9F) * (0.18F + windWave * 0.6F);
			this.left_sleeve.zRot += sleeveWave;
			this.left_sleeve.yRot += sleeveTwist * 0.5F;
			this.right_sleeve.zRot -= sleeveWave;
			this.right_sleeve.yRot -= sleeveTwist * 0.5F;
		} else if (moving && !stalking) {
			float sleeveWave = Mth.sin(time * 0.8F) * 0.1F;
			this.left_sleeve.zRot += sleeveWave;
			this.right_sleeve.zRot -= sleeveWave;
		}

		// === LEGS ===
		if (aggressive) {
			// AGGRESSIVE/ATTACK MODE: Dangling legs like she's hovering/flying (Vex-style)
			this.left_leg.xRot = 0.2F;
			this.right_leg.xRot = 0.2F;
			this.left_leg.yRot = 0.0F;
			this.right_leg.yRot = 0.0F;
			this.left_leg.zRot = 0.0F;
			this.right_leg.zRot = 0.0F;
		} else if (moving && !stalking) {
			// WANDERING: Normal walking animation when moving around
			this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * clampedSwing;
			this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * clampedSwing;
			this.left_leg.yRot = 0.0F;
			this.right_leg.yRot = 0.0F;
			this.left_leg.zRot = 0.0F;
			this.right_leg.zRot = 0.0F;
		} else {
			// STALKING OR IDLE: Standing still, legs straight
			this.left_leg.xRot = 0.0F;
			this.right_leg.xRot = 0.0F;
			this.left_leg.yRot = 0.0F;
			this.right_leg.yRot = 0.0F;
			this.left_leg.zRot = 0.0F;
			this.right_leg.zRot = 0.0F;
		}
		
		// Pants always follow legs exactly
		this.left_pants.xRot = this.left_leg.xRot;
		this.left_pants.yRot = this.left_leg.yRot;
		this.left_pants.zRot = this.left_leg.zRot;
		this.right_pants.xRot = this.right_leg.xRot;
		this.right_pants.yRot = this.right_leg.yRot;
		this.right_pants.zRot = this.right_leg.zRot;
		
		// Skirts follow legs with slight delay
		this.left_skirt.xRot = this.left_leg.xRot * 0.5F;
		this.left_skirt.zRot = 0.0F;
		this.right_skirt.xRot = this.right_leg.xRot * 0.5F;
		this.right_skirt.zRot = 0.0F;

		// === HAIR ===
		float hairBaseTilt = aggressive ? 0.65F : (moving ? 0.22F : 0.05F);
		float hairSwayAmp = 0.06F + windWave;
		float hairTwistAmp = 0.14F * windStrength;
		this.hair_right.xRot = hairBaseTilt - Mth.sin(time) * hairSwayAmp;
		this.hair_left.xRot = hairBaseTilt - Mth.sin(time + (float)Math.PI / 3F) * hairSwayAmp;
		this.hair_right.zRot = -Mth.sin(ageInTicks * 0.12F) * hairTwistAmp;
		this.hair_left.zRot = Mth.sin(ageInTicks * 0.12F + (float)Math.PI / 5F) * hairTwistAmp;
	}
	
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hair_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hair_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		jacket.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_sleeve.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_sleeve.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_pants.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_pants.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}