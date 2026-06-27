package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public class SpectralArrowCustomSoundMixin {

    private AbstractArrow self() {
        return (AbstractArrow)(Object)this;
    }

    @Inject(method = "getDefaultHitGroundSoundEvent", at = @At("HEAD"), cancellable = true)
    private void replaceGroundHitSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (self() instanceof SpectralArrow) {
            cir.setReturnValue(SoundRegistry.BULLET_HIT);
        }
    }

    @ModifyArg(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        ),
        index = 0
    )
    private SoundEvent replaceEntityHitSound(SoundEvent original) {
        if (self() instanceof SpectralArrow && (
            original == SoundEvents.ARROW_HIT ||
            original == SoundEvents.ARROW_HIT_PLAYER ||
            original == SoundEvents.CROSSBOW_HIT
        )) {
            return SoundRegistry.BULLET_HIT;
        }
        return original;
    }
    
    @ModifyArg(
        method = "onHitBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        ),
        index = 0
    )
    private SoundEvent replaceBlockHitSound(SoundEvent original) {
        if (self() instanceof SpectralArrow) {
            return SoundRegistry.BULLET_HIT;
        }
        return original;
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void removeVisualArrowFromPlayer(EntityHitResult result, CallbackInfo ci) {
        if (!(self() instanceof SpectralArrow)) return;

        Level level = self().level();
        if (!level.isClientSide()) {
            Entity hitEntity = result.getEntity();
            if (hitEntity instanceof LivingEntity living) {
                living.setArrowCount(Math.max(0, living.getArrowCount() - 1));
            }
        }
    }

    @Inject(method = "doPostHurtEffects", at = @At("HEAD"), cancellable = true)
    private void disableGlowingEffect(LivingEntity target, CallbackInfo ci) {
        if (self() instanceof SpectralArrow) {
            ci.cancel(); // Prevent glowing outline
        }
    }
}
