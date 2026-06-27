package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.RedCrownedCraneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Made with Blockbench 5.0.4
 * Converted for Minecraft 1.21.1 Fabric
 */
public class RedCrownedCraneModel extends HierarchicalModel<RedCrownedCraneEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		ResourceLocation.fromNamespaceAndPath("sengoku", "red_crowned_crane"), "main");
	
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart right_leg;
	private final ModelPart left_leg;
	private final ModelPart left_wing;
	private final ModelPart right_wing;
	
	public RedCrownedCraneModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.neck = root.getChild("neck");
		this.head = this.neck.getChild("head");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
		this.left_wing = root.getChild("left_wing");
		this.right_wing = root.getChild("right_wing");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		// Body (expanded to match JEM proportions)
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 13.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(0.0F, 6.0F, -1.0F));
		
		// Neck (taller crane neck to match JEM)
		PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create()
			.texOffs(24, 0).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(0.0F, 3.0F, -1.5F));
		
		// Head (perched at neck top, raised)
		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 14).addBox(-1.5F, -3.0F, -4.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(0.0F, -16.0F, 0.0F));
		
		// Right leg (lengthened)
		PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
			.texOffs(30, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offset(-1.5F, 9.0F, 2.0F));
		
		// Left leg (lengthened)
		PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
			.texOffs(30, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(1.5F, 9.0F, 2.0F));
		
		// Left wing (adjusted placement)
		PartDefinition left_wing = root.addOrReplaceChild("left_wing", CubeListBuilder.create()
			.texOffs(24, 10).addBox(0.0F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(3.5F, 6.0F, 0.0F));
		
		// Right wing (adjusted placement)
		PartDefinition right_wing = root.addOrReplaceChild("right_wing", CubeListBuilder.create()
			.texOffs(24, 10).mirror().addBox(-1.0F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offset(-3.5F, 6.0F, 0.0F));
			
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	@Override
	public ModelPart root() {
		return this.root;
	}
	
	@Override
	public void setupAnim(RedCrownedCraneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		
		// Simple animation: idle or walk
		if (limbSwingAmount > 0.001F) {
			animateWalk(limbSwing, limbSwingAmount);
		} else {
			animateIdle(ageInTicks);
			// Apply head look for idle
			this.neck.yRot = netHeadYaw * DEG_TO_RAD;
			this.neck.xRot += headPitch * DEG_TO_RAD * 0.7F;
			this.head.xRot += headPitch * DEG_TO_RAD * 0.3F;
		}
	}
	
	private void animateIdle(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 2.4167F; // 2.4167 second loop
		
		// Head: slight bobbing
		this.head.xRot = lerp(time, 
			new Keyframe(0.0F, 10.0F), 
			new Keyframe(0.9583F, 15.0F), 
			new Keyframe(2.25F, 10.0F)) * DEG_TO_RAD;

		// Neck: gentle sway
		this.neck.xRot = lerp(time,
			new Keyframe(0.0F, 8.0F),
			new Keyframe(0.9583F, 12.0F),
			new Keyframe(2.25F, 8.0F)) * DEG_TO_RAD;
		
		// Body: subtle rotation
		this.body.xRot = lerp(time,
			new Keyframe(0.0F, 5.0F),
			new Keyframe(0.9583F, 2.5F),
			new Keyframe(2.25F, 5.0F)) * DEG_TO_RAD;
		
		// Right leg: subtle shift
		this.right_leg.xRot = lerp(time,
			new Keyframe(0.0F, 10.0F),
			new Keyframe(1.1667F, 18.0F),
			new Keyframe(2.25F, 10.0F)) * DEG_TO_RAD;
		
		// Left leg: opposite shift
		this.left_leg.xRot = lerp(time,
			new Keyframe(0.0F, 18.0F),
			new Keyframe(1.1667F, 10.0F),
			new Keyframe(2.25F, 18.0F)) * DEG_TO_RAD;
		
		// Wings: folded at sides with slight motion
		this.left_wing.zRot = lerp(time,
			new Keyframe(0.0F, -10.0F),
			new Keyframe(0.7083F, -15.0F),
			new Keyframe(2.25F, -10.0F)) * DEG_TO_RAD;
			
		this.right_wing.zRot = lerp(time,
			new Keyframe(0.0F, 10.0F),
			new Keyframe(0.7083F, 15.0F),
			new Keyframe(2.25F, 10.0F)) * DEG_TO_RAD;
	}
	
	private void animateWalk(float limbSwing, float limbSwingAmount) {
		// Simple walking animation
		float time = limbSwing * 0.5F % 1.0F;
		
		// Neck/head stay relatively level with slight movement
		this.neck.xRot = limbSwingAmount * 0.1F * DEG_TO_RAD;
		this.head.xRot = limbSwingAmount * 0.08F * DEG_TO_RAD;
		
		// Body bobs vertically during walk
		float bodyBob = Mth.sin(time * (float)Math.PI) * limbSwingAmount;
		this.body.y += bodyBob * 0.5F;
		
		// Legs walk in opposite phases
		float rightLegAngle = Mth.sin(time * (float)Math.PI);
		float leftLegAngle = Mth.sin((time + 0.5F) * (float)Math.PI);
		
		this.right_leg.xRot = (15.0F + rightLegAngle * 30.0F) * limbSwingAmount * DEG_TO_RAD;
		this.left_leg.xRot = (15.0F + leftLegAngle * 30.0F) * limbSwingAmount * DEG_TO_RAD;
		
		// Wings slightly flap
		float wingFlap = Mth.sin(time * (float)Math.PI * 2.0F);
		this.left_wing.zRot = (-10.0F + wingFlap * 5.0F) * limbSwingAmount * DEG_TO_RAD;
		this.right_wing.zRot = (10.0F - wingFlap * 5.0F) * limbSwingAmount * DEG_TO_RAD;
	}

// Helper classes and methods for keyframe interpolation
	private static final float DEG_TO_RAD = (float)Math.PI / 180.0F;
	
	private static class Keyframe {
		final float time;
		final float value;
		
		Keyframe(float time, float value) {
			this.time = time;
			this.value = value;
		}
	}
	
	private float lerp(float time, Keyframe... keyframes) {
		if (keyframes.length == 0) return 0.0F;
		if (keyframes.length == 1) return keyframes[0].value;
		
		// Find the two keyframes we're between
		for (int i = 0; i < keyframes.length - 1; i++) {
			Keyframe k1 = keyframes[i];
			Keyframe k2 = keyframes[i + 1];
			
			if (time >= k1.time && time <= k2.time) {
				float delta = k2.time - k1.time;
				if (delta == 0.0F) return k1.value;
				
				float progress = (time - k1.time) / delta;
				return Mth.lerp(progress, k1.value, k2.value);
			}
		}
		
		// If we're past the last keyframe, return the last value
		return keyframes[keyframes.length - 1].value;
	}
}
