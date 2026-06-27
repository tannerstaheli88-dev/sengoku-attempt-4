package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.PolarBear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents repeated setTarget calls on PolarBear within a short cooldown window to avoid
 * replaying detection/anger particles when the same target is re-applied rapidly.
 */
@Mixin(PolarBear.class)
public class PolarBearSetTargetMixin {

    private static final String ALERT_TAG_PREFIX = "sengoku_last_alert:";
    private static final long COOLDOWN_TICKS = 40L; // 2 seconds

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        try {
            PolarBear self = (PolarBear) (Object) this;
            if (target == null) return; // allow clearing target

            if (self.level() == null) return;
            long now = self.level().getGameTime();

            String toRemove = null;
            for (String t : self.getTags()) {
                if (t.startsWith(ALERT_TAG_PREFIX)) {
                    try {
                        long last = Long.parseLong(t.substring(ALERT_TAG_PREFIX.length()));
                        if (now - last <= COOLDOWN_TICKS && self.getTarget() == target) {
                            // Suppress redundant re-targeting
                            ci.cancel();
                            return;
                        }
                        toRemove = t; // remove old timestamp tag
                        break;
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (toRemove != null) {
                try { self.getTags().remove(toRemove); } catch (Throwable ignored) {}
            }

            // record a new last-alert timestamp tag
            try { self.addTag(ALERT_TAG_PREFIX + now); } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }
}
