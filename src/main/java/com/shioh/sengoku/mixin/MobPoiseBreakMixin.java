package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobPoiseBreakMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void sengoku$tickBrokenPoiseState(CallbackInfo ci) {
        WeaponBlockGoal.tickMobCombatState((Mob) (Object) this);
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void sengoku$preventBrokenPoiseAttacks(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (WeaponBlockGoal.isPoiseBroken((Mob) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
