package com.shioh.sengoku.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.shioh.sengoku.entity.MacaqueEntity;
import com.shioh.sengoku.client.animation.MacaqueAnimations;

/**
 * Macaque model with keyframe-based idle/walk/run animations.
 * Converted from Blockbench export (user attachment) and adapted
 * to the project's animation helpers (keyframe lerp style).
 */
public class MacaqueModel extends HierarchicalModel<MacaqueEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("sengoku", "macaque"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart legs;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart arms;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    // cached base translations so we can assign offsets without accumulating jitter
    private final float baseBodyY;
    private final float baseBodyZ;
    private final float baseHeadY;
    private final float baseHeadZ;
    private final float baseArmsY;
    private final float baseArmsZ;
    private final float baseLegsY;
    private final float baseLegsZ;
    private final float baseRightLegZ;
    private final float baseLeftLegZ;
    private final float baseRightArmZ;
    private final float baseLeftArmZ;

    public MacaqueModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.legs = root.getChild("legs");
        this.rightLeg = this.legs.getChild("right_leg");
        this.leftLeg = this.legs.getChild("left_leg");
        this.arms = root.getChild("arms");
        this.rightArm = this.arms.getChild("right_arm");
        this.leftArm = this.arms.getChild("left_arm");
        // cache base translation values from the freshly constructed ModelParts
        this.baseBodyY = this.body.y;
        this.baseBodyZ = this.body.z;
        this.baseHeadY = this.head.y;
        this.baseHeadZ = this.head.z;
        this.baseArmsY = this.arms.y;
        this.baseArmsZ = this.arms.z;
        this.baseLegsY = this.legs.y;
        this.baseLegsZ = this.legs.z;
        this.baseRightLegZ = this.rightLeg.z;
        this.baseLeftLegZ = this.leftLeg.z;
        this.baseRightArmZ = this.rightArm.z;
        this.baseLeftArmZ = this.leftArm.z;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 19).addBox(-4.0F, -2.3542F, -5.7292F, 8.0F, 7.0F, 10.0F)
                .texOffs(0, 0).addBox(-4.5F, -2.8542F, -6.2292F, 9.0F, 8.0F, 11.0F)
                .texOffs(52, 14).addBox(-1.0F, -2.3542F, 4.2708F, 2.0F, 2.0F, 3.0F),
            PartPose.offset(0.0F, 14.3542F, 1.7292F));

        PartDefinition head = body.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(36, 19).addBox(-3.0F, -3.4167F, -6.4167F, 6.0F, 6.0F, 6.0F)
                .texOffs(0, 36).addBox(-3.5F, -3.9167F, -5.9167F, 7.0F, 7.0F, 7.0F)
                .texOffs(40, 14).addBox(-2.0F, -0.6667F, -7.1667F, 4.0F, 3.0F, 2.0F),
            PartPose.offset(0.0F, -0.9375F, -4.3125F));

        PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 14.5F, 3.5F));

        PartDefinition right_leg = legs.addOrReplaceChild("right_leg",
            CubeListBuilder.create()
                .texOffs(44, 31).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 10.0F, 3.0F)
                .texOffs(28, 36).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 10.0F, 4.0F),
            PartPose.offset(-3.75F, 0.0F, 0.25F));

        PartDefinition left_leg = legs.addOrReplaceChild("left_leg",
            CubeListBuilder.create()
                .texOffs(44, 31).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 10.0F, 3.0F)
                .texOffs(28, 36).mirror().addBox(-2.0F, -1.5F, -2.0F, 4.0F, 10.0F, 4.0F),
            PartPose.offset(3.75F, 0.0F, 0.25F));

        PartDefinition arms = root.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 14.25F, -4.0F));

        PartDefinition right_arm = arms.addOrReplaceChild("right_arm",
            CubeListBuilder.create()
                .texOffs(24, 50).addBox(-1.0F, -1.25F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(44, 44).addBox(-1.5F, -1.25F, -1.5F, 3.0F, 10.0F, 3.0F),
            PartPose.offset(-4.0F, 0.0F, -1.0F));

        PartDefinition left_arm = arms.addOrReplaceChild("left_arm",
            CubeListBuilder.create().mirror()
                .texOffs(24, 50).addBox(-1.0F, -1.25F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(44, 44).addBox(-1.5F, -1.25F, -1.5F, 3.0F, 10.0F, 3.0F),
            PartPose.offset(4.0F, 0.0F, -1.0F));

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MacaqueEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head look
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Grooming has highest visual priority.
        if (entity.isGrooming()) {
            animateGroom(ageInTicks);
            return;
        }

        // Movement should drive walk/run animations first for active locomotion.
        float moveSpeed = Math.abs(limbSwingAmount);
        if (moveSpeed >= 0.01F) {
            if (moveSpeed < 0.6F) animateWalk(limbSwing, limbSwingAmount, ageInTicks);
            else animateRun(limbSwing, limbSwingAmount, ageInTicks);
            return;
        }

        // If not moving, prefer sitting visuals when physically sitting or being groomed/pack-sitting.
        if (entity.isSitting() || entity.isBeingGroomed() || entity.isPackSitting()) {
            animateSitting(ageInTicks);
        } else {
            animateIdle(ageInTicks);
        }
    }

    private void animateGroom(float ageInTicks) {
        float time = (ageInTicks / 20.0F) % MacaqueAnimations.IDLE_SITTING_PERIOD;

        // Body - subtle lean/return similar to sitting
        this.body.xRot = lerp(time,
            new Keyframe(0.0F, -55.0F),
            new Keyframe(0.4583F, -50.0F),
            new Keyframe(1.375F, -50.0F),
            new Keyframe(1.75F, -55.0F)) * DEG_TO_RAD;

        // Body translate down/back (stable assignment relative to base)
        // apply a small downward offset so the body sits lower while grooming
        this.body.y = this.baseBodyY + lerp(time,
            new Keyframe(0.0F, -1.0F),
            new Keyframe(0.4167F, -2.0F),
            new Keyframe(1.4583F, -2.0F),
            new Keyframe(1.75F, -1.0F)) * 0.12F;
        // lower the body so the macaque sits lower; add positive offset to move down in world Y
        this.body.y += 1.8F;

        // Legs remain folded as in sitting
        this.legs.xRot = -90.0F * DEG_TO_RAD;
        // lower the legs more so the macaque appears settled on the ground
        // invert offset sign so legs translate down (increase Y) instead of up
        // reduce multiplier slightly so legs sit a bit higher (user requested)
        this.legs.y = this.baseLegsY + (7.5F * 0.90F);

        // Head performs a more complex grooming motion (nods/tilts)
        this.head.xRot = lerp(time,
            new Keyframe(0.0F, 77.5F),
            new Keyframe(0.5417F, 70.0F),
            new Keyframe(0.625F, 80.0F),
            new Keyframe(0.7083F, 75.0F),
            new Keyframe(0.7917F, 80.0F),
            new Keyframe(0.875F, 75.0F),
            new Keyframe(0.9583F, 80.0F),
            new Keyframe(1.0417F, 75.0F),
            new Keyframe(1.125F, 80.0F),
            new Keyframe(1.2083F, 75.0F),
            new Keyframe(1.2917F, 80.0F),
            new Keyframe(1.375F, 75.0F),
            new Keyframe(1.75F, 77.5F)) * DEG_TO_RAD;
        // slightly raise the head during grooming to reduce extreme forward pitch
        this.head.xRot -= 12.0F * DEG_TO_RAD;

        this.head.y = this.baseHeadY + lerp(time,
            new Keyframe(0.0F, -1.0F),
            new Keyframe(0.4167F, -2.0F),
            new Keyframe(1.4583F, -2.0F),
            new Keyframe(1.75F, -1.0F)) * 0.12F;
        // lift the head slightly in world Y so it appears more raised
        this.head.y += 0.5F;
        this.head.z = this.baseHeadZ + lerp(time,
            new Keyframe(0.0F, -2.0F),
            new Keyframe(1.75F, -2.0F)) * 0.12F;

        // Arms: perform grooming reach/sweep on both arms
        this.arms.xRot = lerp(time,
            new Keyframe(0.0F, -25.0F),
            new Keyframe(0.4583F, -30.0F),
            new Keyframe(1.0F, -28.0F),
            new Keyframe(1.5F, -30.0F),
            new Keyframe(1.75F, -25.0F)) * DEG_TO_RAD;
        this.arms.y = this.baseArmsY + lerp(time,
            new Keyframe(0.0F, -1.0F),
            new Keyframe(1.75F, -1.0F)) * 0.12F;
        this.arms.z = this.baseArmsZ + lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.4583F, 2.0F),
            new Keyframe(0.7083F, 0.0F),
            new Keyframe(1.5F, 2.0F),
            new Keyframe(1.75F, 0.0F)) * 0.12F;

        // Right arm has an active grooming sequence
        this.rightArm.xRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.4583F, -38.669F),
            new Keyframe(1.0F, -37.8566F),
            new Keyframe(1.5F, -38.669F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;
        this.rightArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.4583F, -11.1448F),
            new Keyframe(1.0F, 2.6371F),
            new Keyframe(1.5F, -11.1448F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;

        // Left arm mirrors a grooming reach
        this.leftArm.xRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.4583F, -45.0014F),
            new Keyframe(1.0F, -43.5368F),
            new Keyframe(1.5F, -45.0014F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;
        this.leftArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.4583F, 13.1248F),
            new Keyframe(1.0F, -1.2688F),
            new Keyframe(1.5F, 13.1248F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;

        // Slight leg offsets to keep the seated posture
        this.rightLeg.zRot = -12.5F * DEG_TO_RAD;
        this.leftLeg.zRot = 10.0F * DEG_TO_RAD;
    }

    private void animateSitting(float ageInTicks) {
        float time = (ageInTicks / 20.0F) % MacaqueAnimations.IDLE_SITTING_PERIOD;

        // Body rotation deep lean (-55 -> -47.5 -> -55)
        this.body.xRot = lerp(time,
            new Keyframe(0.0F, -55.0F),
            new Keyframe(0.8333F, -47.5F),
            new Keyframe(1.75F, -55.0F)) * DEG_TO_RAD;

        // Body translate down/back (scaled) — assign relative to base to avoid jitter
        // apply a small downward offset so the body sits lower while sitting
        this.body.y = this.baseBodyY + lerp(time,
            new Keyframe(0.0F, -3.0F),
            new Keyframe(1.75F, -3.0F)) * 0.12F;
        // lower the body so the macaque sits lower; add positive offset to move down in world Y
        this.body.y += 1.8F;
        this.body.z = this.baseBodyZ + lerp(time,
            new Keyframe(0.0F, 1.0F),
            new Keyframe(1.75F, 1.0F)) * 0.12F;

        // Legs folded: rotate X to -90 degrees and translate upwards (stable assignment)
        this.legs.xRot = -90.0F * DEG_TO_RAD;
        // lower the legs during grooming as well
        // invert offset sign so legs translate down (increase Y) instead of up
        // reduce multiplier slightly so legs sit a bit higher (user requested)
        this.legs.y = this.baseLegsY + (7.5F * 0.90F);

        // Head pitched forward and slightly translated
        this.head.xRot = lerp(time,
            new Keyframe(0.0F, 50.0F),
            new Keyframe(0.8333F, 45.0F),
            new Keyframe(1.75F, 50.0F)) * DEG_TO_RAD;
        this.head.y = this.baseHeadY + lerp(time,
            new Keyframe(0.0F, -1.0F),
            new Keyframe(0.8333F, -1.0F),
            new Keyframe(1.75F, -1.0F)) * 0.12F;
        this.head.z = this.baseHeadZ + lerp(time,
            new Keyframe(0.0F, -2.0F),
            new Keyframe(0.8333F, -2.0F),
            new Keyframe(1.75F, -2.0F)) * 0.12F;

        // Arms relaxed onto belly: rotate X / translate
        this.arms.xRot = -25.0F * DEG_TO_RAD;
        this.arms.y = this.baseArmsY + (-1.0F * 0.12F);
        this.arms.z = this.baseArmsZ + (3.0F * 0.12F);

        // Slight right leg roll and small arm yaw (subtle)
        this.rightLeg.zRot = -12.5F * DEG_TO_RAD;
        this.rightArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.8333F, -10.0F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;
        this.leftArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.8333F, 10.0F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;
        this.leftLeg.zRot = 10.0F * DEG_TO_RAD;
    }

    private void animateIdle(float ageInTicks) {
        float time = (ageInTicks / 20.0F) % MacaqueAnimations.IDLE_STANDING_PERIOD; // from Blockbench

        // Body: -20 -> -15 -> -20 deg
        this.body.xRot = lerp(time,
            new Keyframe(0.0F, -20.0F),
            new Keyframe(0.9167F, -15.0F),
            new Keyframe(1.75F, -20.0F)) * DEG_TO_RAD;

        // Head: 22.5 -> 17.5 -> 22.5 deg
        this.head.xRot = lerp(time,
            new Keyframe(0.0F, 22.5F),
            new Keyframe(0.9167F, 17.5F),
            new Keyframe(1.75F, 22.5F)) * DEG_TO_RAD;

        // Right arm z-rotation small sweep 0 -> -10 -> 0
        this.rightArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.9167F, -10.0F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;

        // Left arm opposite
        this.leftArm.zRot = lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.9167F, 10.0F),
            new Keyframe(1.75F, 0.0F)) * DEG_TO_RAD;
    }

    private void animateWalk(float limbSwing, float limbSwingAmount, float ageInTicks) {
        // Drive walk animation from Blockbench keyframes (approximation)
        float time = (ageInTicks / 20.0F) % MacaqueAnimations.WALKING_PERIOD;

        // Body rotation keyframes (use X component of Blockbench rotational vectors)
        this.body.xRot = lerp(time,
            new Keyframe(0.0F, -20.0F),
            new Keyframe(0.5F, -17.2573F),
            new Keyframe(0.9167F, -15.0F),
            new Keyframe(1.2083F, -17.2713F),
            new Keyframe(1.5F, -20.0F)) * DEG_TO_RAD;

        // Body vertical translation keyframes (stable assignment)
        this.body.y = this.baseBodyY + lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.6667F, 1.83F),
            new Keyframe(1.5F, 0.0F)) * 0.1F; // scaled down for in-world units

        // Head small bob/rotation (matches idle pattern)
        this.head.xRot = lerp(time,
            new Keyframe(0.0F, 22.5F),
            new Keyframe(0.9167F, 17.5F),
            new Keyframe(1.5F, 22.5F)) * DEG_TO_RAD;

        // Simple limb-swing for quadruped motion: arms act as forelegs
        float swing = limbSwing;
        float amount = Math.min(1.0F, limbSwingAmount);
        // Hind legs
        this.rightLeg.xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount * DEG_TO_RAD * 30F;
        this.leftLeg.xRot = Mth.cos(swing * 0.6662F + (float)Math.PI) * 1.4F * amount * DEG_TO_RAD * 30F;
        // Forelegs (arms) — opposite phase to the hind legs for a typical trot
        this.rightArm.xRot = Mth.cos(swing * 0.6662F + (float)Math.PI) * 1.4F * amount * DEG_TO_RAD * 30F;
        this.leftArm.xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount * DEG_TO_RAD * 30F;
    }

    private void animateRun(float limbSwing, float limbSwingAmount, float ageInTicks) {
        // Use Blockbench running keyframes (short period)
        float time = (ageInTicks / 20.0F) % MacaqueAnimations.RUNNING_PERIOD;

        // Body rotation (X component)
        this.body.xRot = lerp(time,
            new Keyframe(0.0F, -20.0F),
            new Keyframe(0.125F, -17.2573F),
            new Keyframe(0.2083F, -15.0F),
            new Keyframe(0.25F, -17.2713F),
            new Keyframe(0.4167F, -20.0F)) * DEG_TO_RAD;

        // Body translate (small bounce) — assign relative to base to avoid jitter
        this.body.y = this.baseBodyY + lerp(time,
            new Keyframe(0.0F, 0.0F),
            new Keyframe(0.125F, 1.64F),
            new Keyframe(0.25F, 0.4F),
            new Keyframe(0.4167F, 0.0F)) * 0.12F;

        // Head rotation / translate
        this.head.xRot = lerp(time,
            new Keyframe(0.0F, 22.5F),
            new Keyframe(0.125F, 17.5F),
            new Keyframe(0.4167F, 22.5F)) * DEG_TO_RAD;

        // Use Blockbench running keyframes for legs and arms (rotate + translate)
        // Simple limb-swing for running: stronger amplitude
        float swing = limbSwing * 1.5F;
        float amount = Math.min(1.0F, limbSwingAmount);
        this.rightLeg.xRot = Mth.cos(swing * 0.6662F) * 1.6F * amount * DEG_TO_RAD * 40F;
        this.leftLeg.xRot = Mth.cos(swing * 0.6662F + (float)Math.PI) * 1.6F * amount * DEG_TO_RAD * 40F;
        this.rightArm.xRot = Mth.cos(swing * 0.6662F + (float)Math.PI) * 1.6F * amount * DEG_TO_RAD * 30F;
        this.leftArm.xRot = Mth.cos(swing * 0.6662F) * 1.6F * amount * DEG_TO_RAD * 30F;

        
    }

    // --- Keyframe helper (small, compatible subset used in other models) ---
    private static final float DEG_TO_RAD = (float)Math.PI / 180.0F;

    private static class Keyframe {
        final float time;
        final float value;
        Keyframe(float time, float value) { this.time = time; this.value = value; }
    }

    private float lerp(float time, Keyframe... keyframes) {
        if (keyframes.length == 0) return 0.0F;
        if (keyframes.length == 1) return keyframes[0].value;

        Keyframe a = keyframes[0];
        for (int i = 1; i < keyframes.length; i++) {
            Keyframe b = keyframes[i];
            if (time <= b.time) {
                float t = (time - a.time) / Math.max(1e-6F, (b.time - a.time));
                return a.value + (b.value - a.value) * t;
            }
            a = b;
        }
        return keyframes[keyframes.length - 1].value;
    }
}
