package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents suffocation damage for any entity (player or mob) that is currently
 * riding another entity. Vanilla only special-cases boats; this generalizes
 * the exemption to all mounts so riders of custom Sengoku mobs don't take
 * suffocation damage when the mount clips into solid blocks (e.g. tall
 * entities passing under overhangs, dragon segments, etc).
 */
@Mixin(LivingEntity.class)
public class LivingEntitySuffocationMixin {

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void sengoku$noSuffocateWhileMounted(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (vehicle != null) {
            cir.setReturnValue(false);
        }
    }
}