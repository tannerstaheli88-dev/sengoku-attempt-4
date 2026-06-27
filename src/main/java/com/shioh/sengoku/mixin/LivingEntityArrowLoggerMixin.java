package com.shioh.sengoku.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Temporary logger: logs non-spectral arrow velocity at LivingEntity.hurt HEAD and TAIL.
 * Keeps original code untouched and is easy to remove.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArrowLoggerMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void sengoku$logArrowHead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try {
            java.lang.Object self = (Object) this;
            if (!(self instanceof LivingEntity)) return;
            LivingEntity living = (LivingEntity) self;
            if (living.level().isClientSide()) return;
            java.lang.Object direct = source.getDirectEntity();
            if (direct instanceof AbstractArrow a && !(a instanceof net.minecraft.world.entity.projectile.SpectralArrow)) {
                // debug log removed
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    private void sengoku$logArrowTail(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try {
            java.lang.Object self = (Object) this;
            if (!(self instanceof LivingEntity)) return;
            LivingEntity living = (LivingEntity) self;
            if (living.level().isClientSide()) return;
            java.lang.Object direct = source.getDirectEntity();
            if (direct instanceof AbstractArrow a && !(a instanceof net.minecraft.world.entity.projectile.SpectralArrow)) {
                // debug log removed
            }
        } catch (Throwable ignored) {}
    }
}
