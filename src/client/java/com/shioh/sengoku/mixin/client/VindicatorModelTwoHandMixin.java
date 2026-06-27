package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
// Avoid direct reference to client ModelPart type to keep compilation robust;
// treat model parts as generic objects and set rotation fields via reflection.
// Using string-target mixin to avoid importing client-only class into compilation unit
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.model.VindicatorModel")
public abstract class VindicatorModelTwoHandMixin {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void sengoku$applyTwoHandPose(Vindicator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        try {
            if (entity == null) return;
            boolean blocking = false;
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("sengoku$isWeaponBlocking");
                m.setAccessible(true);
                Object o = m.invoke(entity);
                if (o instanceof Boolean && (Boolean) o) blocking = true;
            } catch (Throwable ignored) {}
            if (!blocking) {
                try { blocking = WeaponBlockGoal.isCurrentlyBlocking(entity); } catch (Throwable ignored) {}
            }
            if (!blocking) return;

            // If the entity's arm pose is already CROSSBOW_HOLD, prefer vanilla
            // pillager-style locked pose and skip our custom rotations.
            try {
                if (entity.getArmPose() == net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) return;
            } catch (Throwable ignored) {}

            // Find arm parts (as generic objects) and apply rotations reflectively
            List<Object> rightParts = findModelPartsContaining(this.getClass(), "right", "right_arm", "rightarm", "arm");
            List<Object> leftParts = findModelPartsContaining(this.getClass(), "left", "left_arm", "leftarm", "arm");
            if (rightParts.isEmpty() || leftParts.isEmpty()) return;

            for (Object p : rightParts) applyRotationRecursively(p, -1.6F, 0.0F, 0.0F);
            for (Object p : leftParts) applyRotationRecursively(p, -1.6F, 0.0F, 0.0F);

            // inward yaw
            boolean mainIsRight = entity.getMainArm().name().equals("RIGHT");
            float inward = 0.28F;
            boolean mainHas = !entity.getMainHandItem().isEmpty();
            boolean offHas = !entity.getOffhandItem().isEmpty();
            boolean weaponInMain = mainHas || !offHas;
            float rightY = 0.0F, leftY = 0.0F;
            if (weaponInMain) {
                if (mainIsRight) { rightY = -inward; leftY = inward; }
                else { rightY = inward; leftY = -inward; }
            } else {
                if (mainIsRight) { rightY = inward; leftY = -inward; }
                else { rightY = -inward; leftY = inward; }
            }
            for (Object p : rightParts) applyRotationRecursively(p, getRotationFloat(p, "xRot"), rightY, 0.0F);
            for (Object p : leftParts) applyRotationRecursively(p, getRotationFloat(p, "xRot"), leftY, 0.0F);
        } catch (Throwable ignored) {}
    }

    private List<Object> findModelPartsContaining(Class<?> cls, String... tokens) {
        List<Object> parts = new ArrayList<>();
        while (cls != null) {
            for (Field f : cls.getDeclaredFields()) {
                try {
                    String fname = f.getName().toLowerCase();
                    for (String t : tokens) {
                        if (fname.contains(t)) {
                            f.setAccessible(true);
                            Object val = f.get(this);
                            if (val != null) {
                                if (!parts.contains(val)) parts.add(val);
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

    private void applyRotationRecursively(Object part, float x, float y, float z) {
        try {
            setRotationFloat(part, "xRot", x);
            setRotationFloat(part, "yRot", y);
            setRotationFloat(part, "zRot", z);

            for (Field f : part.getClass().getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object val = f.get(part);
                    if (val == null) continue;
                    if (val instanceof java.util.List) {
                        for (Object o : (java.util.List<?>) val) applyRotationRecursively(o, x, y, z);
                    } else {
                        applyRotationRecursively(val, x, y, z);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private void setRotationFloat(Object obj, String fieldName, float v) {
        if (obj == null) return;
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            // handle both float and double fields
            if (f.getType() == float.class) f.setFloat(obj, v);
            else if (f.getType() == double.class) f.setDouble(obj, v);
            else {
                try { f.set(obj, Float.valueOf(v)); } catch (Throwable ignored) {}
            }
        } catch (NoSuchFieldException nsf) {
            // ignore: some model implementations store rotations on child parts differently
        } catch (Throwable ignored) {}
    }

    private float getRotationFloat(Object obj, String fieldName) {
        if (obj == null) return 0.0F;
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            if (val instanceof Float) return (Float) val;
            if (val instanceof Double) return ((Double) val).floatValue();
            if (f.getType() == float.class) return f.getFloat(obj);
            if (f.getType() == double.class) return (float) f.getDouble(obj);
        } catch (Throwable ignored) {}
        return 0.0F;
    }
}
