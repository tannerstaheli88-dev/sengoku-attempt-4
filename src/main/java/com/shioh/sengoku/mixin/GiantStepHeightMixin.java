package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.monster.Giant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Giant.class)
public abstract class GiantStepHeightMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setHighStepHeight(CallbackInfo ci) {
        try {
            Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 3.0f); // always high step
        } catch (Exception ignored) {
            // Field absent on some mappings; ignore to avoid noisy logging
        }
    }
}
