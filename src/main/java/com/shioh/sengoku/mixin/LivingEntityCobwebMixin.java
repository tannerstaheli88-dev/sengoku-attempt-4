package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.monster.Illusioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent Illusioners from being slowed or stuck by cobwebs.
 * Cancels LivingEntity.makeStuckInBlock when the entity is an Illusioner.
 */
@Mixin(LivingEntity.class)
public class LivingEntityCobwebMixin {

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void preventIllusionerCobwebSlow(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        if ((Object)this instanceof Illusioner) {
            // Cancel the vanilla slowdown behavior for Illusioners
            ci.cancel();
        }
    }
}
