package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensure all illagers use the locked crossbow arm pose while blocking so the
 * hands remain clamped rather than using bow-like animations that can move
 * other body parts (legs/torso) on some models.
 */
@Mixin(AbstractIllager.class)
public abstract class AbstractIllagerBlockingPoseMixin {
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void sengoku$forceCrossbowWhenBlocking(CallbackInfoReturnable<AbstractIllager.IllagerArmPose> cir) {
        try {
            AbstractIllager self = (AbstractIllager) (Object) this;
            boolean blocking = false;
            // Prefer explicit synced flag if present
            try {
                java.lang.reflect.Method m = self.getClass().getMethod("sengoku$isWeaponBlocking");
                m.setAccessible(true);
                Object o = m.invoke(self);
                if (o instanceof Boolean && (Boolean) o) blocking = true;
            } catch (Throwable ignored) {}

            // prefer synced flag — nothing to record here

            if (!blocking) {
                try { blocking = WeaponBlockGoal.isCurrentlyBlocking((Mob) self); } catch (Throwable ignored) {}
            }

            if (blocking) {
                // detected blocking — do not force vanilla arm pose here
            }
        } catch (Throwable ignored) {}
    }
}
