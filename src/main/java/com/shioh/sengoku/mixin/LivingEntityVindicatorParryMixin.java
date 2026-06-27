package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.WarlordEntity;
import com.shioh.sengoku.entity.ShinobiLordEntity;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Global hurt hook: if a vindicator is blocking, convert to parry.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityVindicatorParryMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void sengoku$vindicatorParry(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self instanceof WarlordEntity) return;
        if (!(self instanceof Vindicator vind)) return;
        if (vind.getClass() != Vindicator.class) return;
        if (!WeaponBlockGoal.isCurrentlyBlocking(vind)) return;
        // Parry logic copied from samurai
        try { self.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + self.getRandom().nextFloat() * 0.4F); } catch (Throwable ignored) {}
            WeaponBlockGoal.spawnBlockFeedbackParticles(self);
        WeaponBlockGoal.spawnBlockFeedbackParticles(self, amount);
        Entity attackerE = source.getEntity();
        if (!vind.level().isClientSide && attackerE instanceof LivingEntity attacker) {
            Vec3 dir = vind.position().subtract(attacker.position()).normalize();
            vind.setDeltaMovement(vind.getDeltaMovement().add(dir.scale(0.25)));
            vind.hurtMarked = true;
        }
        try { WeaponBlockGoal.onSuccessfulBlock(vind, attackerE instanceof LivingEntity ? (LivingEntity) attackerE : null); } catch (Throwable ignored) {}
        cir.setReturnValue(false);
    }
}
