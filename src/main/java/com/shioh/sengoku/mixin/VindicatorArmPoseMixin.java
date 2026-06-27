package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vindicator.class)
public abstract class VindicatorArmPoseMixin {
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void sengoku$forceTwoHandPose(CallbackInfoReturnable<AbstractIllager.IllagerArmPose> cir) {
        // disabled: do not force CROSSBOW_HOLD here; prefer vanilla or explicit client-side adjustments
    }
}
