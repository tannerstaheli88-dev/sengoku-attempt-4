package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.shioh.sengoku.mixin.MobAccessor;

/**
 * Inject WeaponBlockGoal into vanilla vindicator goals after they register their defaults.
 */
@Mixin(Vindicator.class)
public abstract class VindicatorGoalMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void sengoku$addWeaponBlockGoal(CallbackInfo ci) {
        try {
            Vindicator self = (Vindicator)(Object)this;
            // Priority 1 to match samurai; shift existing if needed.
            ((MobAccessor)self).getGoalSelector().addGoal(1, new WeaponBlockGoal(self));
        } catch (Throwable ignored) {}
    }
}
