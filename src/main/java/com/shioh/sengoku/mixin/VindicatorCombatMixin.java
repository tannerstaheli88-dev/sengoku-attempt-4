package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ShinobiLordEntity;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends vanilla Vindicator with weapon block & counter behavior matching samurai.
 */
@Mixin(Vindicator.class)
public abstract class VindicatorCombatMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void sengoku$injectWeaponBlockGoal(CallbackInfo ci) {
        try {
            Vindicator self = (Vindicator)(Object)this;
            if (self.getClass() != Vindicator.class) return;
            // Reflectively access protected goalSelector
            try {
                java.lang.reflect.Field f = Mob.class.getDeclaredField("goalSelector");
                f.setAccessible(true);
                GoalSelector selector = (GoalSelector) f.get(self);
                selector.addGoal(1, new WeaponBlockGoal(self));
            } catch (Throwable ignored2) {}
        } catch (Throwable ignored) {}
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void sengoku$weaponBlockHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Vindicator self = (Vindicator)(Object)this;
        if (self.getClass() != Vindicator.class) return;
        if (WeaponBlockGoal.isCurrentlyBlocking(self)) {
            try { self.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + self.getRandom().nextFloat() * 0.4F); } catch (Throwable ignored) {}
            WeaponBlockGoal.spawnBlockFeedbackParticles(self);
            Entity attackerE = source.getEntity();
            if (!self.level().isClientSide && attackerE instanceof LivingEntity attacker) {
                Vec3 dir = self.position().subtract(attacker.position()).normalize();
                self.setDeltaMovement(self.getDeltaMovement().add(dir.scale(0.25)));
                self.hurtMarked = true;
            }
            try { WeaponBlockGoal.onSuccessfulBlock(self, attackerE instanceof LivingEntity ? (LivingEntity) attackerE : null); } catch (Throwable ignored) {}
            cir.setReturnValue(false); // fully blocked
        }
    }
}
