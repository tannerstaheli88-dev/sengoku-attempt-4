package com.shioh.sengoku.client.model;

import com.shioh.sengoku.entity.RedCrownedCraneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;

/**
 * Made with Blockbench 5.0.4
 * Converted for Minecraft 1.21.1 Fabric
 */
public class RedCrownedCraneModel extends HierarchicalModel<RedCrownedCraneEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		ResourceLocation.fromNamespaceAndPath("sengoku", "red_crowned_crane"), "main");
	
	private final ModelPart root;
	private final ModelPart right_leg;
	private final ModelPart right_calf;
	private final ModelPart right_foot;
	private final ModelPart left_leg;
	private final ModelPart left_calf;
	private final ModelPart left_foot;
	private final ModelPart neck;
	private final ModelPart lower;
	private final ModelPart upper;
	private final ModelPart head;
	private final ModelPart beak;
	private final ModelPart body;
	private final ModelPart wings;
	private final ModelPart left_wing_folded;
	private final ModelPart right_wing_folded;
	private final ModelPart left_wing;
	private final ModelPart left_secondaries;
	private final ModelPart left_primaries;
	private final ModelPart right_wing;
	private final ModelPart right_secondaries;
	private final ModelPart right_primaries;
	
	public RedCrownedCraneModel(ModelPart root) {
		this.root = root;
		this.right_leg = root.getChild("right_leg");
		this.right_calf = this.right_leg.getChild("right_calf");
		this.right_foot = this.right_calf.getChild("right_foot");
		this.left_leg = root.getChild("left_leg");
		this.left_calf = this.left_leg.getChild("left_calf");
		this.left_foot = this.left_calf.getChild("left_foot");
		this.body = root.getChild("body");
		this.wings = this.body.getChild("wings");
		this.neck = this.body.getChild("neck");
		this.lower = this.neck.getChild("lower");
		this.upper = this.lower.getChild("upper");
		this.head = this.upper.getChild("head");
		this.beak = this.head.getChild("beak");
		this.left_wing_folded = this.wings.getChild("left_wing_folded");
		this.right_wing_folded = this.wings.getChild("right_wing_folded");
		this.left_wing = this.body.getChild("left_wing");
		this.left_secondaries = this.left_wing.getChild("left_secondaries");
		this.left_primaries = this.left_secondaries.getChild("left_primaries");
		this.right_wing = this.body.getChild("right_wing");
		this.right_secondaries = this.right_wing.getChild("right_secondaries");
		this.right_primaries = this.right_secondaries.getChild("right_primaries");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
			.texOffs(74, 59).mirror().addBox(-1.5833F, -0.25F, -1.9167F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
			.texOffs(48, 78).mirror().addBox(-0.5833F, 4.75F, -0.9167F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offset(-1.9167F, 4.1294F, 2.2326F));

		PartDefinition right_calf = right_leg.addOrReplaceChild("right_calf", CubeListBuilder.create()
			.texOffs(54, 78).mirror().addBox(-0.75F, -0.25F, -0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offset(0.1667F, 11.0F, -0.6667F));

		// Thin sole to avoid z-fighting: give the sole tiny thickness and center it
		// around the original plane (y = 0.0) so the rest of the foot remains unchanged.
		PartDefinition right_foot = right_calf.addOrReplaceChild("right_foot", CubeListBuilder.create()
			.texOffs(76, 18).mirror().addBox(-1.0F, -0.05F, -4.0F, 2.0F, 0.1F, 4.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offset(0.25F, 8.75F, 0.75F));

		PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
			.texOffs(74, 59).addBox(-1.4167F, -0.25F, -1.9167F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
			.texOffs(48, 78).addBox(-0.4167F, 4.75F, -0.9167F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(1.9167F, 4.1294F, 2.2326F));

		PartDefinition left_calf = left_leg.addOrReplaceChild("left_calf", CubeListBuilder.create()
			.texOffs(54, 78).addBox(-0.25F, -0.25F, -0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(-0.1667F, 11.0F, -0.6667F));

		// Thin sole to avoid z-fighting: give the sole tiny thickness and center it
		// around the original plane (y = 0.0) so the rest of the foot remains unchanged.
		PartDefinition left_foot = left_calf.addOrReplaceChild("left_foot", CubeListBuilder.create()
			.texOffs(76, 18).addBox(-1.0F, -0.05F, -4.0F, 2.0F, 0.1F, 4.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(-0.25F, 8.75F, 0.75F));



		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), 
			PartPose.offset(0.0F, 4.3573F, 7.3094F));

		// Move neck under body so the neck pivots follow body rotations
		// Adjusted Z offset to bring neck pivot closer to the body for flying pose
		// Neck is a child of body so it follows body rotations. Use the original offsets
		// here; any flying-specific adjustment will be applied in `animateFlying`.
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), 
			PartPose.offset(-0.25F, -6.1424F, -9.9325F));

		PartDefinition lower = neck.addOrReplaceChild("lower", CubeListBuilder.create(), 
			PartPose.offset(-0.25F, -1.2149F, -0.3769F));

		PartDefinition lower_neck_r1 = lower.addOrReplaceChild("lower_neck_r1", CubeListBuilder.create()
			.texOffs(74, 49).addBox(-1.0F, -7.0F, -2.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition upper = lower.addOrReplaceChild("upper", CubeListBuilder.create()
			.texOffs(74, 75).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(0.5F, -5.0F, -2.8F));

		PartDefinition head = upper.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(68, 11).addBox(-1.5F, -2.75F, -3.2538F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), 
			PartPose.offset(0.0F, -7.25F, 0.5538F));

		PartDefinition beak = head.addOrReplaceChild("beak", CubeListBuilder.create(), 
			PartPose.offset(0.0F, -0.75F, -4.7462F));

		PartDefinition upper_r1 = beak.addOrReplaceChild("upper_r1", CubeListBuilder.create()
			.texOffs(76, 31).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, -0.5F, 1.9924F, 0.0873F, 0.0F, 0.0F));

		PartDefinition lower_r1 = beak.addOrReplaceChild("lower_r1", CubeListBuilder.create()
			.texOffs(76, 26).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, 0.5F, 1.9924F, -0.0873F, 0.0F, 0.0F));

		PartDefinition tail_r1 = body.addOrReplaceChild("tail_r1", CubeListBuilder.create()
			.texOffs(0, 21).addBox(0.0F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, 3.6427F, 3.6906F, -0.48F, 0.0F, 0.0F));

		PartDefinition tail_torso_r1 = body.addOrReplaceChild("tail_torso_r1", CubeListBuilder.create()
			.texOffs(0, 65).addBox(-3.0F, -5.0F, 6.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(0, 0).addBox(-4.0F, -8.0F, -7.0F, 8.0F, 8.0F, 13.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, -0.3573F, -7.3094F, -0.5236F, 0.0F, 0.0F));

		PartDefinition wings = body.addOrReplaceChild("wings", CubeListBuilder.create(), 
			PartPose.offset(5.0F, 14.6427F, -7.3094F));

		PartDefinition left_wing_folded = wings.addOrReplaceChild("left_wing_folded", CubeListBuilder.create(), 
			PartPose.offset(0.1667F, -16.0281F, 3.145F));

		PartDefinition cube_r1 = left_wing_folded.addOrReplaceChild("cube_r1", CubeListBuilder.create()
			.texOffs(24, 21).addBox(-1.0F, 1.0F, -1.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(-0.1667F, -7.9719F, -4.145F, -0.48F, 0.0F, 0.0F));

		PartDefinition cube_r2 = left_wing_folded.addOrReplaceChild("cube_r2", CubeListBuilder.create()
			.texOffs(12, 72).addBox(-1.0F, 0.0F, 10.0F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.8333F, -5.9719F, -5.145F, -0.48F, 0.0F, 0.0F));

		PartDefinition right_wing_folded = wings.addOrReplaceChild("right_wing_folded", CubeListBuilder.create(), 
			PartPose.offset(-10.1667F, -16.0281F, 3.145F));

		PartDefinition cube_r3 = right_wing_folded.addOrReplaceChild("cube_r3", CubeListBuilder.create()
			.texOffs(24, 40).addBox(0.0F, 1.0F, -1.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.1667F, -7.9719F, -4.145F, -0.48F, 0.0F, 0.0F));

		PartDefinition cube_r4 = right_wing_folded.addOrReplaceChild("cube_r4", CubeListBuilder.create()
			.texOffs(74, 38).addBox(0.0F, 0.0F, 10.0F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(50, 38).addBox(0.0F, -1.0F, -1.0F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(-0.8333F, -5.9719F, -5.145F, -0.48F, 0.0F, 0.0F));

		PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create(), 
			PartPose.offset(4.5F, -7.5496F, -8.2705F));

		PartDefinition left_secondaries = left_wing.addOrReplaceChild("left_secondaries", CubeListBuilder.create(), 
			PartPose.offsetAndRotation(7.3659F, -0.8789F, 2.3347F, 1.8423F, 0.566F, 1.591F));

		PartDefinition left_secondaries_r1 = left_secondaries.addOrReplaceChild("left_secondaries_r1", CubeListBuilder.create()
			.texOffs(50, 19).addBox(-0.5F, -4.7986F, -0.1422F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.0F, -0.8661F, -9.0657F, -0.2182F, 0.0F, 0.0F));

		PartDefinition left_primaries = left_secondaries.addOrReplaceChild("left_primaries", CubeListBuilder.create(), 
			PartPose.offsetAndRotation(-0.9869F, 3.6291F, 11.1522F, 0.1971F, -0.146F, -0.0744F));

		PartDefinition left_tip_r1 = left_primaries.addOrReplaceChild("left_tip_r1", CubeListBuilder.create()
			.texOffs(0, 72).addBox(-1.0F, 0.0F, 10.0F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(24, 59).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), 
			PartPose.offsetAndRotation(0.5F, -6.8077F, -6.0389F, -0.48F, 0.0F, 0.0F));

		PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create(), 
			PartPose.offset(-4.1364F, -8.6266F, -5.4631F));

		PartDefinition right_secondaries = right_wing.addOrReplaceChild("right_secondaries", CubeListBuilder.create(), 
			PartPose.offsetAndRotation(-12.0F, 0.0F, 0.0F, 1.8423F, -0.566F, -1.591F));

		PartDefinition right_secondaries_r1 = right_secondaries.addOrReplaceChild("right_secondaries_r1", CubeListBuilder.create()
			.texOffs(50, 19).mirror().addBox(-0.5F, -4.7986F, -0.1422F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offsetAndRotation(-0.4934F, -2.2476F, -13.109F, -0.2182F, 0.0F, 0.0F));

		PartDefinition right_primaries = right_secondaries.addOrReplaceChild("right_primaries", CubeListBuilder.create(), 
			PartPose.offsetAndRotation(0.4934F, 2.2476F, 7.109F, 0.1971F, 0.146F, 0.0744F));

		PartDefinition right_tip_r1 = right_primaries.addOrReplaceChild("right_tip_r1", CubeListBuilder.create()
			.texOffs(0, 72).mirror().addBox(0.0F, 0.0F, 10.0F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
			.texOffs(24, 59).mirror().addBox(0.0F, -1.0F, -1.0F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), 
			PartPose.offsetAndRotation(-0.5F, -6.8076F, -6.0389F, -0.48F, 0.0F, 0.0F));
			
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	@Override
	public ModelPart root() {
		return this.root;
	}
	
	@Override
	public void setupAnim(RedCrownedCraneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		
		// Check special animations first (priority: drink > feather) - ONLY if completely still
		// Use the entity's moveAnimationFactor to determine if it's truly stationary
		try {
			// Determine stationarity using client-side signals only: limbSwingAmount and velocity.
			// Relying on server-synced moveAnimationFactor can be delayed and cause animations
			// to start on the client while the entity is already moving.
			Vec3 clientVel = entity.getDeltaMovement();
			double horizSq = clientVel.x * clientVel.x + clientVel.z * clientVel.z;
			boolean navDone = true;
			try { navDone = entity.getNavigation() == null || entity.getNavigation().isDone(); } catch (Throwable ignored) {}

			boolean isStationary = entity.onGround() && !entity.isFlying() && horizSq < 0.0001D && navDone && Math.abs(limbSwingAmount) < 0.01F;

			if (isStationary && entity.isPlayingDrinkAnimation()) {
				animateDrink(ageInTicks);
				return;
			}
			if (isStationary && entity.isPlayingFeatherAnimation()) {
				animateFeather(ageInTicks);
				return;
			}
		} catch (Throwable ignored) {}
		
		// Prefer the floating animation in water over the flying animation.
		if (entity.isInWater()) {
			// Use a dedicated floating animation while the crane is in water
			// (FloatGoal/vanilla float behavior will keep it afloat; this supplies the visual pose)
			animateFloating(ageInTicks);

			// If a player is nearby, blend the floating head animation toward the player's position
			// so the crane will look at players that approach while still playing its floating motion.
			Player nearest = entity.level().getNearestPlayer(entity, 6.0D);
			if (nearest != null) {
				double dx = nearest.getX() - entity.getX();
				double dz = nearest.getZ() - entity.getZ();
				double dy = (nearest.getEyeY()) - (entity.getEyeY());
				double horiz = Math.sqrt(dx * dx + dz * dz);
				float lookYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
				float lookPitch = (float)-Math.toDegrees(Math.atan2(dy, horiz));
				// Convert to radians
				float lookYawRad = lookYaw * DEG_TO_RAD;
				float lookPitchRad = lookPitch * DEG_TO_RAD;
				float dist = (float)nearest.distanceTo(entity);
				float blend = 1.0F - Mth.clamp(dist / 6.0F, 0.0F, 1.0F);
				// Blend current (animated) head rotation toward look-at rotation
				this.head.yRot = Mth.lerp(blend, this.head.yRot, lookYawRad);
				this.head.xRot = Mth.lerp(blend, this.head.xRot, lookPitchRad);
			}
		} else if (entity.isFlying()) {
			animateFlying(ageInTicks);
		} else {
			// On ground: use limbSwingAmount to decide walk vs idle
			// AMPLIFY limbSwingAmount for better leg visibility at slow speeds
			if (limbSwingAmount > 0.001F) {
				// Scale up the limbSwingAmount to make leg movement more visible
				float amplifiedAmount = Math.min(1.0F, limbSwingAmount * 3.0F);
				animateWalk(limbSwing, amplifiedAmount);
				// Suppress head look during active walking for more natural animation
				this.head.yRot = 0.0F;
				this.head.xRot = 15.0F * DEG_TO_RAD;
			} else {
				animateIdle(ageInTicks);
				// Apply head look for idle
				this.head.yRot = netHeadYaw * DEG_TO_RAD;
				this.head.xRot += headPitch * DEG_TO_RAD;
			}
		}
	}
	
	private void animateIdle(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 2.4167F; // 2.4167 second loop
		
		// Head: 15° -> 20° -> 15°
		this.head.xRot = lerp(time, 
			new Keyframe(0.0F, 15.0F), 
			new Keyframe(0.9583F, 20.0F), 
			new Keyframe(2.25F, 15.0F)) * DEG_TO_RAD;
		
		// Body: 5° -> 2.5° -> 5°
		this.body.xRot = lerp(time,
			new Keyframe(0.0F, 5.0F),
			new Keyframe(0.9583F, 2.5F),
			new Keyframe(2.25F, 5.0F)) * DEG_TO_RAD;
		
		// Left wing folded: 0° -> -5° -> 0° (yRot)
		this.left_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, -5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Right wing folded: 0° -> 5° -> 0° (yRot)
		this.right_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, 5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Right leg: 7.5° -> 15° -> 7.5°
		this.right_leg.xRot = lerp(time,
			new Keyframe(0.0F, 7.5F),
			new Keyframe(1.1667F, 15.0F),
			new Keyframe(2.25F, 7.5F)) * DEG_TO_RAD;
		
		// Right calf: -7.5° -> -25° -> -7.5°
		this.right_calf.xRot = lerp(time,
			new Keyframe(0.0F, -7.5F),
			new Keyframe(1.1667F, -25.0F),
			new Keyframe(2.25F, -7.5F)) * DEG_TO_RAD;
		
		// Right foot: 0° -> 10° -> 0°
		this.right_foot.xRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.1667F, 10.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Lower neck: 17.5° -> 10° -> 17.5°
		this.lower.xRot = lerp(time,
			new Keyframe(0.0F, 17.5F),
			new Keyframe(1.3333F, 10.0F),
			new Keyframe(2.3333F, 17.5F)) * DEG_TO_RAD;
		
		// Upper neck: constant -22.5°
		this.upper.xRot = -22.5F * DEG_TO_RAD;
		
		// Wings hidden/shown
		this.left_wing_folded.visible = true;
		this.right_wing_folded.visible = true;
		this.left_wing.visible = false;
		this.right_wing.visible = false;
	}

	/**
	 * Floating animation used when the crane is in water. This is intentionally
	 * conservative: it sets a relaxed floating pose and applies a small bobbing
	 * motion so the model doesn't conflict with stronger animations (drink/feather/fly).
	 */
	private void animateFloating(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 2.4167F; // 2.4167s loop (keeps head motion similar to idle)
		// small sinusoidal bob to make the crane look like it's riding the water
		float bob = Mth.sin(ageInTicks / 10.0F) * 0.5F;

		// Shift the entire model downward while floating so it sits lower in water.
		// This global sink affects legs as well; keep conservative to avoid clipping.
		final float globalFloatDown = 6.0F;
		this.root.y += globalFloatDown;

		// Additionally apply a small body-only downward offset so the torso/neck/wings
		// sit lower relative to the legs (legs are children of root, body is separate).
		// Tweak this value to adjust how far below the legs the body sits.
		final float bodyFloatDown = 5.0F;
		this.body.y += bodyFloatDown;

		// Neck: small vertical/forward shift to tuck the neck slightly while floating
		float neckTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.20835F, -1.8F),
			new Keyframe(2.4167F, 0.0F));
		float neckTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.20835F, -0.6F),
			new Keyframe(2.4167F, 0.0F));
		this.neck.y += neckTransY;
		this.neck.z += neckTransZ;

		// Body: use Blockbench's floating body pitch as a relaxed tilted pose
		this.body.xRot = lerp(time,
			new Keyframe(0.0F, 25.0F),
			new Keyframe(0.6667F, 24.9791F),
			new Keyframe(1.625F, 24.9448F),
			new Keyframe(2.4167F, 25.0F)) * DEG_TO_RAD;
		// gentle vertical bob
		this.body.y += bob * 0.6F;

		// Wings remain folded while floating
		this.left_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, -5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		this.right_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, 5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;

		// Legs tucked/relaxed pose from Blockbench export (static base pose)
		// Apply detailed leg keyframe animations (from provided floating animation)
		// Right leg rotation (mostly static tucked angle)
		this.right_leg.xRot = lerp(time,
			new Keyframe(0.0F, 25.0F)
		) * DEG_TO_RAD;
		// Right leg translations (y, z)
		// Move the legs well below the torso for proper floating pose
		float rightLegTransY = lerp(time,
			new Keyframe(0.0F, 7.0F),
			new Keyframe(0.7917F, 8.0F),
			new Keyframe(1.5833F, 9.0F),
			new Keyframe(1.9583F, 8.37F),
			new Keyframe(2.25F, 7.0F),
			new Keyframe(2.4167F, 7.0F)
		);
		float rightLegTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7917F, 2.0F),
			new Keyframe(1.5833F, 0.0F),
			new Keyframe(1.9583F, -1.56F),
			new Keyframe(2.25F, -1.0F),
			new Keyframe(2.4167F, 0.0F)
		);
		this.right_leg.y += rightLegTransY;
		this.right_leg.z += rightLegTransZ;

		// Right calf and foot rotations
		this.right_calf.xRot = lerp(time,
			new Keyframe(0.0F, -105.0F),
			new Keyframe(1.0833F, -80.0F),
			new Keyframe(2.4167F, -105.0F)
		) * DEG_TO_RAD;
		this.right_foot.xRot = lerp(time,
			new Keyframe(0.0F, 140.0F),
			new Keyframe(1.0833F, 167.5F),
			new Keyframe(2.4167F, 140.0F)
		) * DEG_TO_RAD;

		// Left leg rotation (base tucked) and translations
		this.left_leg.xRot = lerp(time,
			new Keyframe(0.0F, 20.0F)
		) * DEG_TO_RAD;
		float leftLegTransY = lerp(time,
			new Keyframe(0.0F, 7.0F),
			new Keyframe(0.375F, 6.37F),
			new Keyframe(0.6667F, 5.0F),
			new Keyframe(1.0417F, 5.0F),
			new Keyframe(1.8333F, 6.0F),
			new Keyframe(2.4167F, 7.0F)
		);
		float leftLegTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.375F, -1.56F),
			new Keyframe(0.6667F, -1.0F),
			new Keyframe(1.0417F, 0.0F),
			new Keyframe(1.8333F, 2.0F),
			new Keyframe(2.4167F, 0.0F)
		);
		this.left_leg.y += leftLegTransY;
		this.left_leg.z += leftLegTransZ;

		// Left calf and foot rotations
		this.left_calf.xRot = lerp(time,
			new Keyframe(0.0F, -55.0F),
			new Keyframe(1.0833F, -77.5F),
			new Keyframe(2.4167F, -55.0F)
		) * DEG_TO_RAD;
		this.left_foot.xRot = lerp(time,
			new Keyframe(0.0F, 112.5F),
			new Keyframe(1.0833F, 132.5F),
			new Keyframe(2.4167F, 112.5F)
		) * DEG_TO_RAD;

		// Maintain neck segments like Blockbench default for floating
		this.lower.xRot = 17.5F * DEG_TO_RAD;
		this.upper.xRot = -22.5F * DEG_TO_RAD;

		// Ensure folded wings visible, primaries hidden
		this.left_wing_folded.visible = true;
		this.right_wing_folded.visible = true;
		this.left_wing.visible = false;
		this.right_wing.visible = false;
	}
	
	private void animateDrink(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 3.5417F; // 3.5417 second animation
		
		// Head: 15° -> -105.11° -> -105.11° -> 15°
		this.head.xRot = lerp(time,
			new Keyframe(0.0F, 15.0F),
			new Keyframe(0.4583F, -105.11F),
			new Keyframe(2.6667F, -105.11F),
			new Keyframe(3.0833F, 15.0F)) * DEG_TO_RAD;
		
		// Neck translate Y: 0 -> 7 (held) - applying to neck directly lowers pivot
		// Use positive values so adding moves the neck downward in model space.
		float neckTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4583F, 7.0F),
			new Keyframe(2.6667F, 7.0F),
			new Keyframe(3.0833F, 0.0F));
		this.neck.y += neckTransY;
		
		// Body rotation: 5° -> 21.3° -> 23.8° -> 27.97° -> 22.39° -> 21.3° -> 5°
		this.body.xRot = lerp(time,
			new Keyframe(0.0F, 5.0F),
			new Keyframe(0.4583F, 21.3F),
			new Keyframe(1.0417F, 23.8F),
			new Keyframe(1.5833F, 27.97F),
			new Keyframe(2.0833F, 22.39F),
			new Keyframe(2.6667F, 21.3F),
			new Keyframe(3.0833F, 5.0F)) * DEG_TO_RAD;
		
		// Left wing folded yRot: 0° -> -5° -> 0°
		this.left_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, -5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Right wing folded yRot: 0° -> 5° -> 0°
		this.right_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, 5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Right leg: 7.5° -> 15° -> 15° -> 7.5°
		this.right_leg.xRot = lerp(time,
			new Keyframe(0.0F, 7.5F),
			new Keyframe(1.1667F, 15.0F),
			new Keyframe(1.7917F, 15.0F),
			new Keyframe(3.0833F, 7.5F)) * DEG_TO_RAD;
		
		// Right calf: -7.5° -> -11.88° -> -7.5°
		this.right_calf.xRot = lerp(time,
			new Keyframe(0.0F, -7.5F),
			new Keyframe(1.125F, -11.88F),
			new Keyframe(3.0833F, -7.5F)) * DEG_TO_RAD;
		
		// Right foot: 0° -> -0.36° -> 0°
		this.right_foot.xRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.125F, -0.36F),
			new Keyframe(3.0833F, 0.0F)) * DEG_TO_RAD;
		
		// Lower neck rotation: 17.5° -> 132.42° -> complex motion -> 132.42° -> 17.5°
		float lowerXRot = lerp(time,
			new Keyframe(0.0F, 17.5F),
			new Keyframe(0.4583F, 132.42F),
			new Keyframe(1.1667F, 139.429F),
			new Keyframe(1.75F, 140.0276F),
			new Keyframe(2.125F, 140.0563F),
			new Keyframe(2.6667F, 132.42F),
			new Keyframe(3.0833F, 17.5F));
		float lowerYRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.1667F, 6.4874F),
			new Keyframe(1.75F, -3.2169F),
			new Keyframe(2.125F, 2.5354F),
			new Keyframe(2.6667F, 0.0F));
		float lowerZRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.1667F, 7.5299F),
			new Keyframe(1.75F, -3.8298F),
			new Keyframe(2.125F, -8.6445F),
			new Keyframe(2.6667F, 0.0F));
		this.lower.xRot = lowerXRot * DEG_TO_RAD;
		this.lower.yRot = lowerYRot * DEG_TO_RAD;
		this.lower.zRot = lowerZRot * DEG_TO_RAD;
		
		// Lower translate: 0,0,0 -> 0,-4,-2 -> 0,0,0
		float lowerTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.625F, -4.0F),
			new Keyframe(3.0833F, 0.0F));
		float lowerTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.625F, -2.0F),
			new Keyframe(3.0833F, 0.0F));
		this.lower.y += lowerTransY;
		this.lower.z += lowerTransZ;
		
		// Upper neck: -22.5° -> 12.5° -> 12.5° -> -22.5°
		this.upper.xRot = lerp(time,
			new Keyframe(0.0F, -22.5F),
			new Keyframe(0.4583F, 12.5F),
			new Keyframe(2.6667F, 12.5F),
			new Keyframe(3.0833F, -22.5F)) * DEG_TO_RAD;
		
		// Wings folded
		this.left_wing_folded.visible = true;
		this.right_wing_folded.visible = true;
		this.left_wing.visible = false;
		this.right_wing.visible = false;
	}
	
	private void animateFlying(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 1.2083F; // 1.2083 second loop

		// Apply flight-only neck translation so the pivot is visually closer to the body
		// (this mirrors the temporary PartPose changes we tested previously but only
		// applies them during the flying animation).
		final float flightNeckShiftZ = 4.0F; // bring neck 4 units toward the body
		final float flightNeckShiftY = 1.0F; // raise neck 1 unit (less negative Y)
		this.neck.z += flightNeckShiftZ;
		this.neck.y += flightNeckShiftY;
		
		// Head: constant -72.5°
		this.head.xRot = -72.5F * DEG_TO_RAD;
		
		// Neck rotation: 0° -> 0°,0°,-10° -> 0°
		this.neck.zRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, -10.0F),
			new Keyframe(1.125F, 0.0F)) * DEG_TO_RAD;
		
		// Neck translate X: 0 -> -1 -> 0
		float neckTransX = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, -1.0F),
			new Keyframe(1.125F, 0.0F));
		this.neck.x += neckTransX;
		
		// Body rotation: 20° base with roll
		float bodyXRot = 20.0F * DEG_TO_RAD;
		float bodyYRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, 2.5587F),
			new Keyframe(1.0F, 0.0F)) * DEG_TO_RAD;
		float bodyZRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, -7.0524F),
			new Keyframe(1.0F, 0.0F)) * DEG_TO_RAD;
		this.body.xRot = bodyXRot;
		this.body.yRot = bodyYRot;
		this.body.zRot = bodyZRot;
		
		// Left wing rotation (Z axis flapping): 0° -> -62.5° -> 50° -> 0° -> -62.5° -> 50° -> 0°
		this.left_wing.zRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.2083F, -62.5F),
			new Keyframe(0.4167F, 50.0F),
			new Keyframe(0.625F, 0.0F),
			new Keyframe(0.8333F, -62.5F),
			new Keyframe(1.0417F, 50.0F),
			new Keyframe(1.2083F, 0.0F)) * DEG_TO_RAD;
		
		// Right wing rotation (opposite)
		this.right_wing.zRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.2083F, 67.5F),
			new Keyframe(0.4167F, -65.0F),
			new Keyframe(0.625F, 0.0F),
			new Keyframe(0.8333F, 67.5F),
			new Keyframe(1.0417F, -65.0F),
			new Keyframe(1.2083F, 0.0F)) * DEG_TO_RAD;
		
		// Right leg: 72.5° with small variation
		float rightLegXRot = lerp(time,
			new Keyframe(0.0F, 72.5F),
			new Keyframe(0.4167F, 73.0616F),
			new Keyframe(1.0F, 72.5F));
		float rightLegYRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, -4.4638F),
			new Keyframe(1.0F, 0.0F));
		float rightLegZRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.4167F, -14.335F),
			new Keyframe(1.0F, 0.0F));
		this.right_leg.xRot = rightLegXRot * DEG_TO_RAD;
		this.right_leg.yRot = rightLegYRot * DEG_TO_RAD;
		this.right_leg.zRot = rightLegZRot * DEG_TO_RAD;
		
		// Left leg: same as right
		this.left_leg.xRot = rightLegXRot * DEG_TO_RAD;
		this.left_leg.yRot = rightLegYRot * DEG_TO_RAD;
		this.left_leg.zRot = rightLegZRot * DEG_TO_RAD;
		
		// Feet: constant 100°
		this.right_foot.xRot = 100.0F * DEG_TO_RAD;
		this.left_foot.xRot = 100.0F * DEG_TO_RAD;
		
		// Lower neck: constant 47.5° with translate
		this.lower.xRot = 47.5F * DEG_TO_RAD;
		this.lower.y += -4.0F;
		this.lower.z += -3.0F;
		
		// Upper neck: constant 27.5°
		this.upper.xRot = 27.5F * DEG_TO_RAD;
		
		// Wings extended
		this.left_wing_folded.visible = false;
		this.right_wing_folded.visible = false;
		this.left_wing.visible = true;
		this.right_wing.visible = true;
	}
	
	private void animateWalk(float limbSwing, float limbSwingAmount) {
		// Walk speed multiplier: values > 1.0 make the walk cycle play faster.
		final float walkSpeed = 1.8F; // tuned faster per request

		// Apply the speed multiplier to limbSwing so the keyframe time runs faster.
		float time = (limbSwing * 0.6662F * walkSpeed) % (0.5417F * 20.0F / 0.6662F); // Match to animation length
		time = (time * 0.6662F / 20.0F) % 0.5417F;
		
		// Head rotation: 0° -> 10° -> -22.5° -> 0°
		float headXRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.1667F, 10.0F),
			new Keyframe(0.3333F, -22.5F),
			new Keyframe(0.5417F, 0.0F));
		// Reduce head rotation amplitude so it stays visually attached at higher speeds
		this.head.xRot = headXRot * DEG_TO_RAD * limbSwingAmount * 0.75F;
		
		// Head translate Y: 0 -> -1 -> 0
		float headTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.3333F, -1.0F),
			new Keyframe(0.5417F, 0.0F));
		// Apply the small vertical head offset to the upper neck so the head remains attached
		this.upper.y += headTransY * limbSwingAmount * 0.6F;
		
		// Neck translate: complex bounce pattern
		float neckTransY = lerp(time,
			new Keyframe(0.0F, -1.0F),
			new Keyframe(0.1667F, 0.0F),
			new Keyframe(0.3333F, 0.56F),
			new Keyframe(0.4583F, -1.44F),
			new Keyframe(0.5417F, -1.0F));
		float neckTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.1667F, 0.0F),
			new Keyframe(0.3333F, -1.56F),
			new Keyframe(0.4583F, -1.56F),
			new Keyframe(0.5417F, 0.0F));
		this.neck.y += neckTransY * limbSwingAmount;
		this.neck.z += neckTransZ * limbSwingAmount;
		
		// Body translate: vertical bounce (apply to Y so the torso moves up/down)
		float bodyTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.0833F, 1.0F),
			new Keyframe(0.1667F, 1.0F),
			new Keyframe(0.3333F, -2.0F),
			new Keyframe(0.4167F, -1.2F),
			new Keyframe(0.5417F, 0.0F));
		this.body.y += bodyTransY * limbSwingAmount;
		
		// Left wing folded: 0° -> -10° -> 0° (zRot)
		this.left_wing_folded.zRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.2917F, -10.0F),
			new Keyframe(0.5417F, 0.0F)) * DEG_TO_RAD * limbSwingAmount;
		
		// Right wing folded: 0° -> 7.5° -> 0° (zRot)
		this.right_wing_folded.zRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.2917F, 7.5F),
			new Keyframe(0.5417F, 0.0F)) * DEG_TO_RAD * limbSwingAmount;
		
		// Right leg rotation: 37.5° -> -10° -> 67.5° -> 37.5°
		// Speed up the forward (to 67.5°) portion so leg snaps forward quicker.
		float rightLegXRot = lerp(time,
			new Keyframe(0.0F, 37.5F),
			new Keyframe(0.08F, -10.0F),
			new Keyframe(0.32F, 67.5F),
			new Keyframe(0.5417F, 37.5F));
		this.right_leg.xRot = rightLegXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Right leg translate
		// Shift translation keyframes earlier to match the snappier forward leg timing.
		float rightLegTransY = lerp(time,
			new Keyframe(0.0F, -1.0F),
			new Keyframe(0.08F, 0.5F),
			new Keyframe(0.21F, -1.0F),
			new Keyframe(0.32F, -2.5F),
			new Keyframe(0.5417F, -1.0F));
		float rightLegTransZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.08F, -2.0F),
			new Keyframe(0.21F, -3.75F),
			new Keyframe(0.32F, -1.5F),
			new Keyframe(0.5417F, 0.0F));
		this.right_leg.y += rightLegTransY * limbSwingAmount;
		this.right_leg.z += rightLegTransZ * limbSwingAmount;
		
		// Right calf: -107.5° -> -84.17° -> -20° -> -45° -> -75° -> -107.5°
		// Make calf transitions quicker in the forward phase to match leg snap.
		float rightCalfXRot = lerp(time,
			new Keyframe(0.0F, -107.5F),
			new Keyframe(0.053F, -84.17F),
			new Keyframe(0.08F, -20.0F),
			new Keyframe(0.21F, -45.0F),
			new Keyframe(0.32F, -75.0F),
			new Keyframe(0.5417F, -107.5F));
		this.right_calf.xRot = rightCalfXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Right foot: 127.5° -> 164.17° -> 60° -> 20° -> 47.5° -> 102.5°
		float rightFootXRot = lerp(time,
			new Keyframe(0.0F, 127.5F),
			new Keyframe(0.053F, 164.17F),
			new Keyframe(0.08F, 60.0F),
			new Keyframe(0.21F, 20.0F),
			new Keyframe(0.32F, 47.5F),
			new Keyframe(0.5417F, 102.5F));
		this.right_foot.xRot = rightFootXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Upper neck: 0° -> -15° -> 20° -> 0°
		float upperXRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.1667F, -15.0F),
			new Keyframe(0.3333F, 20.0F),
			new Keyframe(0.5417F, 0.0F));
		this.upper.xRot = upperXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Left leg rotation: 67.5° -> 37.5° -> -10° -> 67.5°
		// Mirror the snappier timing for the opposite leg.
		float leftLegXRot = lerp(time,
			new Keyframe(0.0F, 67.5F),
			new Keyframe(0.12F, 37.5F),
			new Keyframe(0.245F, -10.0F),
			new Keyframe(0.5417F, 67.5F));
		this.left_leg.xRot = leftLegXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Left leg translate
		float leftLegTransY = lerp(time,
			new Keyframe(0.0F, -2.5F),
			new Keyframe(0.1667F, -1.0F),
			new Keyframe(0.2917F, 0.5F),
			new Keyframe(0.4167F, -1.0F),
			new Keyframe(0.5417F, -2.5F));
		float leftLegTransZ = lerp(time,
			new Keyframe(0.0F, -1.5F),
			new Keyframe(0.1667F, 0.0F),
			new Keyframe(0.2917F, -2.0F),
			new Keyframe(0.4167F, -3.75F),
			new Keyframe(0.5417F, -1.5F));
		this.left_leg.y += leftLegTransY * limbSwingAmount;
		this.left_leg.z += leftLegTransZ * limbSwingAmount;
		
		// Left calf: -75° -> -107.5° -> -20° -> -45° -> -75°
		float leftCalfXRot = lerp(time,
			new Keyframe(0.0F, -75.0F),
			new Keyframe(0.1667F, -107.5F),
			new Keyframe(0.2917F, -20.0F),
			new Keyframe(0.4167F, -45.0F),
			new Keyframe(0.5417F, -75.0F));
		this.left_calf.xRot = leftCalfXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Left foot: 47.5° -> 102.5° -> 60° -> 15° -> 47.5°
		float leftFootXRot = lerp(time,
			new Keyframe(0.0F, 47.5F),
			new Keyframe(0.1667F, 102.5F),
			new Keyframe(0.2917F, 60.0F),
			new Keyframe(0.4167F, 15.0F),
			new Keyframe(0.5417F, 47.5F));
		this.left_foot.xRot = leftFootXRot * DEG_TO_RAD * limbSwingAmount;
		
		// Wings folded
		this.left_wing_folded.visible = true;
		this.right_wing_folded.visible = true;
		this.left_wing.visible = false;
		this.right_wing.visible = false;
	}
	
	// Helper classes and methods for keyframe interpolation
	private static final float DEG_TO_RAD = (float)Math.PI / 180.0F;
	// FLOAT_DOWN removed — restored literal offset in `animateFloating`
	
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
	
	private void animateFeather(float ageInTicks) {
		float time = (ageInTicks / 20.0F) % 2.5417F; // 2.5417 second animation
		
		// Head: constant 15°
		this.head.xRot = 15.0F * DEG_TO_RAD;
		
		// Neck rotation: 0° -> 17.5° (Y) -> 0°
		this.neck.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.6667F, 17.5F),
			new Keyframe(2.2083F, 0.0F)) * DEG_TO_RAD;
		
		// Neck translate: 0,0,0 -> 0,-3.24,0 -> 0,-4,0 -> 0,0,0
		float neckTransY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.375F, -3.24F),
			new Keyframe(0.6667F, -4.0F),
			new Keyframe(2.2083F, 0.0F));
		this.neck.y += neckTransY;
		
		// Body rotation: 5° -> 21.3° -> 23.8° -> 27.97° -> 22.39° -> 21.3° -> 5°
		this.body.xRot = lerp(time,
			new Keyframe(0.0F, 5.0F),
			new Keyframe(0.2917F, 21.3F),
			new Keyframe(0.875F, 23.8F),
			new Keyframe(1.1667F, 27.97F),
			new Keyframe(1.4583F, 22.39F),
			new Keyframe(1.9167F, 21.3F),
			new Keyframe(2.125F, 5.0F)) * DEG_TO_RAD;
		
		// Left wing folded: 0° -> -5° -> 0° (yRot)
		this.left_wing_folded.yRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.7083F, -5.0F),
			new Keyframe(2.25F, 0.0F)) * DEG_TO_RAD;
		
		// Right wing folded: complex motion with itching behavior
		float rightWingY = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.25F, 5.0F),
			new Keyframe(0.9583F, 0.0F));
		float rightWingZ = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.0833F, -11.7557F),
			new Keyframe(1.3333F, -11.7557F),
			new Keyframe(1.4583F, 0.0F));
		this.right_wing_folded.yRot = rightWingY * DEG_TO_RAD;
		this.right_wing_folded.zRot = rightWingZ * DEG_TO_RAD;
		
		// Right leg: 7.5° -> 15° -> 15° -> 7.5°
		this.right_leg.xRot = lerp(time,
			new Keyframe(0.0F, 7.5F),
			new Keyframe(1.1667F, 15.0F),
			new Keyframe(1.7917F, 15.0F),
			new Keyframe(2.4583F, 7.5F)) * DEG_TO_RAD;
		
		// Right calf: -7.5° -> -11.88° -> -7.5°
		this.right_calf.xRot = lerp(time,
			new Keyframe(0.0F, -7.5F),
			new Keyframe(1.125F, -11.88F),
			new Keyframe(2.5417F, -7.5F)) * DEG_TO_RAD;
		
		// Right foot: 0° -> -0.36° -> 0°
		this.right_foot.xRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(1.125F, -0.36F),
			new Keyframe(2.5417F, 0.0F)) * DEG_TO_RAD;
		
		// Lower neck: complex rotation for preening
		float lowerXRot = lerp(time,
			new Keyframe(0.0F, 17.5F),
			new Keyframe(0.6667F, 10.1499F),
			new Keyframe(1.8333F, 10.1499F),
			new Keyframe(2.2083F, 17.5F));
		float lowerYRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.6667F, 26.4751F),
			new Keyframe(1.8333F, 26.4751F),
			new Keyframe(2.2083F, 0.0F));
		float lowerZRot = lerp(time,
			new Keyframe(0.0F, 0.0F),
			new Keyframe(0.6667F, -14.0992F),
			new Keyframe(1.8333F, -14.0992F),
			new Keyframe(2.2083F, 0.0F));
		this.lower.xRot = lowerXRot * DEG_TO_RAD;
		this.lower.yRot = lowerYRot * DEG_TO_RAD;
		this.lower.zRot = lowerZRot * DEG_TO_RAD;
		
		// Upper neck: complex oscillating motion (preening/scratching)
		float upperXRot, upperYRot, upperZRot;
		if (time <= 0.6667F) {
			upperXRot = Mth.lerp(time / 0.6667F, -22.5F, 290.7561F);
			upperYRot = Mth.lerp(time / 0.6667F, 0.0F, 52.6635F);
			upperZRot = Mth.lerp(time / 0.6667F, 0.0F, 198.8701F);
		} else if (time <= 1.8333F) {
			// Oscillate between 285° and 290° for scratching motion
			float oscProgress = (time - 0.6667F) / (1.8333F - 0.6667F);
			float oscCycle = Mth.sin(oscProgress * (float)Math.PI * 10.0F); // Multiple oscillations
			upperXRot = 287.5F + oscCycle * 2.5F; // Oscillate between 285 and 290
			upperYRot = 52.6635F;
			upperZRot = 198.8701F;
		} else if (time <= 1.875F) {
			float t = (time - 1.8333F) / (1.875F - 1.8333F);
			upperXRot = Mth.lerp(t, 287.5F, 302.2367F);
			upperYRot = Mth.lerp(t, 52.6635F, 51.7689F);
			upperZRot = Mth.lerp(t, 198.8701F, 306.9131F);
		} else if (time <= 2.0417F) {
			float t = (time - 1.875F) / (2.0417F - 1.875F);
			upperXRot = Mth.lerp(t, 302.2367F, 157.0824F);
			upperYRot = Mth.lerp(t, 51.7689F, 73.9081F);
			upperZRot = Mth.lerp(t, 306.9131F, 161.3359F);
		} else if (time <= 2.125F) {
			float t = (time - 2.0417F) / (2.125F - 2.0417F);
			upperXRot = Mth.lerp(t, 157.0824F, 12.8435F);
			upperYRot = Mth.lerp(t, 73.9081F, 33.2978F);
			upperZRot = Mth.lerp(t, 161.3359F, 8.0001F);
		} else {
			float t = (time - 2.125F) / (2.2083F - 2.125F);
			upperXRot = Mth.lerp(t, 12.8435F, -22.5F);
			upperYRot = Mth.lerp(t, 33.2978F, 0.0F);
			upperZRot = Mth.lerp(t, 8.0001F, 0.0F);
		}
		this.upper.xRot = upperXRot * DEG_TO_RAD;
		this.upper.yRot = upperYRot * DEG_TO_RAD;
		this.upper.zRot = upperZRot * DEG_TO_RAD;
		
		// Wings folded
		this.left_wing_folded.visible = true;
		this.right_wing_folded.visible = true;
		this.left_wing.visible = false;
		this.right_wing.visible = false;
	}
}
