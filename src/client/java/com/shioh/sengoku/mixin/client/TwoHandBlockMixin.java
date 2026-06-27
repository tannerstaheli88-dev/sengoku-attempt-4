package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Mixin that temporarily adjusts Illager model arm rotations to a two-handed
 * blocking pose while `WeaponBlockGoal` reports blocking.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class TwoHandBlockMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    // Keep previous arm rotations so we can restore them after rendering
    private static final Map<EntityModel<?>, float[]> PREV = new WeakHashMap<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void sengoku$preRenderAdjustArms(T entity, float limbSwing, float limbSwingAmount, PoseStack matrices, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        try {
            if (!(entity instanceof AbstractIllager)) return;
            AbstractIllager ill = (AbstractIllager) entity;
            if (!WeaponBlockGoal.isCurrentlyBlocking(ill)) return;

            // Always apply our custom two-handed pose when blocking. Relying on
            // vanilla `CROSSBOW_HOLD` produces inconsistent leg/torso adjustments
            // on some models, so we explicitly set arm rotations here.

            // (debugging removed)

            // Save current rotations (rightX, rightY, rightZ, leftX, leftY, leftZ)
            float[] prev = new float[6];
            prev[0] = getFieldFloat(model, "rightArm.xRot");
            prev[1] = getFieldFloat(model, "rightArm.yRot");
            prev[2] = getFieldFloat(model, "rightArm.zRot");
            prev[3] = getFieldFloat(model, "leftArm.xRot");
            prev[4] = getFieldFloat(model, "leftArm.yRot");
            prev[5] = getFieldFloat(model, "leftArm.zRot");
            PREV.put(model, prev);

            // Apply meet-in-front pose (pillager-like locked hands)
            // Use stronger inward rotation and a steady x-rotation so hands meet.
            setFieldFloat(model, "rightArm.xRot", -1.6F);
            setFieldFloat(model, "leftArm.xRot", -1.6F);
            // Determine inward yaw sign based on main arm
            boolean mainIsRight = false;
            try { mainIsRight = entity.getMainArm().name().equals("RIGHT"); } catch (Throwable ignored) {}
            float inward = 0.28F;
            float rY = inward, lY = -inward;
            if (mainIsRight) { rY = -inward; lY = inward; }
            setFieldFloat(model, "rightArm.yRot", rY);
            setFieldFloat(model, "leftArm.yRot", lY);
            setFieldFloat(model, "rightArm.zRot", 0.0F);
            setFieldFloat(model, "leftArm.zRot", 0.0F);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void sengoku$postRenderRestoreArms(T entity, float limbSwing, float limbSwingAmount, PoseStack matrices, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        try {
            float[] prev = PREV.remove(model);
            if (prev == null) return;
            setFieldFloat(model, "rightArm.xRot", prev[0]);
            setFieldFloat(model, "rightArm.yRot", prev[1]);
            setFieldFloat(model, "rightArm.zRot", prev[2]);
            setFieldFloat(model, "leftArm.xRot", prev[3]);
            setFieldFloat(model, "leftArm.yRot", prev[4]);
            setFieldFloat(model, "leftArm.zRot", prev[5]);
        } catch (Throwable ignored) {}
    }

    // Reflection helpers — access nested fields like rightArm.xRot safely
    private static float getFieldFloat(Object root, String path) {
        try {
            String[] parts = path.split("\\.");
            Object cur = root;
            for (String p : parts) {
                java.lang.reflect.Field f = findField(cur.getClass(), p);
                if (f == null) return 0.0F;
                f.setAccessible(true);
                cur = f.get(cur);
            }
            if (cur instanceof Float) return (Float) cur;
        } catch (Throwable ignored) {}
        return 0.0F;
    }

    private static void setFieldFloat(Object root, String path, float value) {
        try {
            String[] parts = path.split("\\.");
            Object cur = root;
            for (int i = 0; i < parts.length - 1; i++) {
                java.lang.reflect.Field f = findField(cur.getClass(), parts[i]);
                if (f == null) return;
                f.setAccessible(true);
                cur = f.get(cur);
            }
            java.lang.reflect.Field last = findField(cur.getClass(), parts[parts.length - 1]);
            if (last == null) return;
            last.setAccessible(true);
            last.setFloat(cur, value);
        } catch (Throwable ignored) {}
    }

    private static java.lang.reflect.Field findField(Class<?> cls, String name) {
        Class<?> cur = cls;
        while (cur != null) {
            try {
                java.lang.reflect.Field f = cur.getDeclaredField(name);
                return f;
            } catch (NoSuchFieldException e) {
                // Try a permissive search: look for any declared field whose name
                // contains the requested name tokens (case-insensitive). This helps
                // across mappings where fields may be named `right_arm` / `rightArm`.
                for (java.lang.reflect.Field f : cur.getDeclaredFields()) {
                    try {
                        String fname = f.getName().toLowerCase();
                        String want = name.toLowerCase();
                        // Normalize by removing non-alphanumeric characters so that
                        // `right_arm`, `rightArm`, or `right-arm` all normalize to
                        // `rightarm` and will match when requested name is either form.
                        String fnameNorm = fname.replaceAll("[^a-z0-9]", "");
                        String wantNorm = want.replaceAll("[^a-z0-9]", "");

                        if (fname.contains(want) || fnameNorm.contains(wantNorm)) return f;

                        // If requested name contains a side token (right/left), ensure the
                        // candidate contains both the side and "arm" after normalization
                        // to avoid matching leg fields like "right_leg".
                        if (wantNorm.contains("right") || wantNorm.contains("left")) {
                            boolean sideMatch = (wantNorm.contains("right") && fnameNorm.contains("right")) || (wantNorm.contains("left") && fnameNorm.contains("left"));
                            if (sideMatch && fnameNorm.contains("arm")) return f;
                        }
                    } catch (Throwable ignored) {}
                }
                cur = cur.getSuperclass();
            }
        }
        return null;
    }
}
