package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Increase ambient sound interval for illagers so many nearby
 * instances don't spam ambient/idle noises.
 */
@Mixin(Mob.class)
public abstract class MobAmbientIntervalMixin {

    // Increase ambient interval for illagers to ~10 seconds (200 ticks)
    @Inject(method = "getAmbientSoundInterval", at = @At("HEAD"), cancellable = true)
    private void sengoku$limitVindicatorAmbient(CallbackInfoReturnable<Integer> cir) {
        try {
            Mob self = (Mob) (Object) this;
            if (self instanceof AbstractIllager) {
                cir.setReturnValue(200); // 10 seconds between ambient sounds
            }
        } catch (Throwable ignored) {}
    }
}
