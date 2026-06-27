package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import java.lang.reflect.Field;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;

/**
 * Apply a two-handed blocking pose on IllagerModel when the entity is blocking.
 * This mixin runs in the model's animation setup so it composes with other
 * model animations and works reliably across mappings by using shadowed parts.
 */
@Mixin(IllagerModel.class)
public abstract class IllagerModelTwoHandMixin {

    // Avoid shadowing model fields (mapping differences). We'll look them up via
    // reflection at runtime to support both yarn/obf/name mappings such as
    // (rightArm/leftArm) and (right_arm/left_arm).

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void sengoku$applyTwoHandPose(AbstractIllager entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        try {
            if (entity == null) return;

            // Robust blocking detection: prefer the WeaponBlockGoal helper (works when
            // client has using-item synced), but fall back to other indicators so
            // vanilla Vindicators and other illagers pick up the two-handed pose even
            // when the using-item flag isn't present on the client.
            boolean blocking = false;
            // First, prefer the explicit synced flag added by mixin on AbstractIllager
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("sengoku$isWeaponBlocking");
                m.setAccessible(true);
                Object o = m.invoke(entity);
                if (o instanceof Boolean && (Boolean) o) blocking = true;
            } catch (Throwable ignored) {}
            // Next, try the server-synced using-item indicator via the helper
            if (!blocking) try { blocking = WeaponBlockGoal.isCurrentlyBlocking(entity); } catch (Throwable ignored) {}
            // Heuristic: for vanilla Vindicators, assume a two-handed block pose when
            // they are aggressive and holding a melee weapon — helps clients show
            // the pose even when the using-item flag is not reliably synced.
            if (!blocking) {
                try {
                    if (entity instanceof net.minecraft.world.entity.monster.Vindicator vind) {
                        // Only treat as blocking when NOT currently aggressive —
                        // the previous heuristic caused the mob to show the two-
                        // handed block pose while actively attacking. Require the
                        // mob to be non-aggressive and near its target with a
                        // sword-like mainhand to consider it "postured".
                        if (!vind.isAggressive()) {
                            net.minecraft.world.entity.LivingEntity t = vind.getTarget();
                            if (t != null) {
                                double d = vind.distanceToSqr(t);
                                if (d < 36.0D) {
                                    // check for sword-like mainhand
                                    try {
                                        String id = vind.getMainHandItem().getItem().toString().toLowerCase();
                                        if (id.contains("sword") || id.contains("katana") || id.contains("blade") || id.contains("yari")) {
                                            blocking = true;
                                        }
                                    } catch (Throwable ignored) {}
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
            if (!blocking) {
                try {
                    // Last resort: if the client reports the mob is using an item, treat as blocking
                    // but avoid treating bow charging as a blocking pose. Only consider
                    // it blocking when the held mainhand item is a blockable melee weapon.
                    Object using = entity.getClass().getMethod("isUsingItem").invoke(entity);
                    if (using instanceof Boolean && (Boolean) using) {
                        try {
                            // prefer explicit arm pose check first
                            try {
                                net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose ap = entity.getArmPose();
                                if (ap == net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
                                    // charging bow — not a blocking posture
                                } else if (ap == net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
                                    // crossbow hold is a locked pose — don't force two-handed block
                                } else {
                                    // Fallback: check mainhand item for blockable types
                                    String id = "";
                                    try { id = entity.getMainHandItem().getItem().toString().toLowerCase(); } catch (Throwable ignored) {}
                                    if (id.contains("sword") || id.contains("yari") || id.contains("katana") || id.contains("blade") || entity.getMainHandItem().getItem() instanceof net.minecraft.world.item.TridentItem) {
                                        blocking = true;
                                    }
                                }
                            } catch (Throwable ignored) {
                                // If arm pose lookup failed, fall back to checking main hand item
                                try {
                                    String id = entity.getMainHandItem().getItem().toString().toLowerCase();
                                    if (id.contains("sword") || id.contains("yari") || id.contains("katana") || id.contains("blade") || entity.getMainHandItem().getItem() instanceof net.minecraft.world.item.TridentItem) {
                                        blocking = true;
                                    }
                                } catch (Throwable ignored2) {}
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }
            if (!blocking) return;

            // no-op: apply pose without logging

            // Find arm model parts broadly: some models use a single "arms" part when folded,
            // others use separate left/right parts and multiple sleeve layers. Match any
            // declared field that contains common arm tokens and apply the pose to all
            // matched parts. This ensures both folded and combat arm phases are overridden.
            java.util.List<ModelPart> rightParts = findModelPartsContaining("right", "right_arm", "rightarm", "right_arm_", "arms", "arm");
            java.util.List<ModelPart> leftParts = findModelPartsContaining("left", "left_arm", "leftarm", "left_arm_", "arms", "arm");
            if (rightParts.isEmpty() || leftParts.isEmpty()) return;

            // Apply stronger meet-in-front pose (tuned to ensure hands visibly meet)
            for (ModelPart p : rightParts) { applyRotationRecursively(p, -1.6F, 0.0F, 0.0F); }
            for (ModelPart p : leftParts)  { applyRotationRecursively(p, -1.6F, 0.0F, 0.0F); }

            // Determine which hand actually holds the weapon so arms rotate toward it
            boolean mainHas = !entity.getMainHandItem().isEmpty();
            boolean offHas = !entity.getOffhandItem().isEmpty();
            boolean weaponInMain = mainHas || !offHas; // prefer main if either

            // Default inward rotation magnitude
            float inward = 0.28F;

            // Choose rotation signs based on which hand holds the weapon and main arm
            // Choose rotation signs based on which hand holds the weapon and main arm
            boolean mainIsRight = entity.getMainArm().name().equals("RIGHT");
            float rightY = 0.0F, leftY = 0.0F;
            if (weaponInMain) {
                if (mainIsRight) { rightY = -inward; leftY = inward; }
                else { rightY = inward; leftY = -inward; }
            } else {
                if (mainIsRight) { rightY = inward; leftY = -inward; }
                else { rightY = -inward; leftY = inward; }
            }

            // Apply Y/Z rotations to all matched parts. Keep the pose perfectly still
            // while blocking by avoiding any sway or movement-based offsets.
            for (ModelPart p : rightParts) { applyRotationRecursively(p, p.xRot, rightY, 0.0F); }
            for (ModelPart p : leftParts)  { applyRotationRecursively(p, p.xRot, leftY, 0.0F); }
        } catch (Throwable ignored) {}
    }

    private ModelPart findModelPart(String... names) {
        Class<?> cls = this.getClass();
        // walk class hierarchy to find the declared field
        while (cls != null) {
            for (String n : names) {
                try {
                    Field f = cls.getDeclaredField(n);
                    f.setAccessible(true);
                    Object val = f.get(this);
                    if (val instanceof ModelPart) return (ModelPart) val;
                } catch (Throwable ignored) {}
            }
            cls = cls.getSuperclass();
        }
        return null;
    }
    private List<ModelPart> findModelPartsContaining(String... tokens) {
        List<ModelPart> parts = new ArrayList<>();
        Class<?> cls = this.getClass();

        // Scan declared fields in the class hierarchy and match any field whose
        // lowercase name contains one of the provided tokens.
        while (cls != null) {
            for (Field f : cls.getDeclaredFields()) {
                try {
                    String fname = f.getName().toLowerCase();
                    for (String t : tokens) {
                        if (fname.contains(t)) {
                            f.setAccessible(true);
                            Object val = f.get(this);
                            if (val instanceof ModelPart) {
                                ModelPart mp = (ModelPart) val;
                                if (!parts.contains(mp)) parts.add(mp);
                            }
                            break;
                        }
                    }
                } catch (Throwable ignored) {}
            }
            cls = cls.getSuperclass();
        }

        return parts;
    }

    /**
     * Apply rotation to a ModelPart and recursively to any child ModelParts
     * discovered via reflection (tries common child names/fields). This ensures
     * layered model parts (sleeves, overlays) are also rotated for the pose.
     */
    private void applyRotationRecursively(ModelPart part, float x, float y, float z) {
        try {
            part.xRot = x;
            part.yRot = y;
            part.zRot = z;
            // Try common child storage fields on ModelPart (children or childParts)
            for (Field f : part.getClass().getDeclaredFields()) {
                try {
                    if (java.util.List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object val = f.get(part);
                        if (val instanceof java.util.List) {
                            for (Object o : (java.util.List<?>) val) {
                                if (o instanceof ModelPart) applyRotationRecursively((ModelPart) o, x, y, z);
                            }
                        }
                    } else if (ModelPart.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object child = f.get(part);
                        if (child instanceof ModelPart) applyRotationRecursively((ModelPart) child, x, y, z);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
