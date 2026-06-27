package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HurtByTargetGoal.class)
public abstract class HurtByTargetGoalNoThroughWallMixin {

    @Inject(method = "alertOthers", at = @At("HEAD"), cancellable = true)
    private void onAlertOthers(LivingEntity attacker, CallbackInfo ci) {
        HurtByTargetGoalAccessor accessor = (HurtByTargetGoalAccessor) (Object) this;
        Mob mob = accessor.getMob();
        if (mob == null || attacker == null) return;

        // ONLY disable vanilla alerting when the attacker is a PLAYER and the illager
        // cannot see the attacker. This prevents illagers from alerting allies through
        // walls/floors while preserving normal mob-on-mob alerting behavior.
        try {
            if (mob instanceof AbstractIllager && attacker instanceof Player) {
                if (!mob.hasLineOfSight(attacker)) {
                    ci.cancel();
                }
            }
        } catch (Throwable ignored) {}
    }

    // Fallback for mappings where `alertOthers()` has no parameters.
    @Inject(method = "alertOthers", at = @At("HEAD"), cancellable = true)
    private void onAlertOthersFallback(CallbackInfo ci) {
        HurtByTargetGoalAccessor accessor = (HurtByTargetGoalAccessor) (Object) this;
        Mob mob = accessor.getMob();
        if (mob == null) return;

        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null) return;

        try {
            if (mob instanceof AbstractIllager && attacker instanceof Player) {
                if (!mob.hasLineOfSight(attacker)) {
                    ci.cancel();
                }
            }
        } catch (Throwable ignored) {}
    }
}
