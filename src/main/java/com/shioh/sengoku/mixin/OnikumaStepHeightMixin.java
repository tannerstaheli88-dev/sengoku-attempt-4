package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.OnikumaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(OnikumaEntity.class)
public abstract class OnikumaStepHeightMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$setStepHeight(CallbackInfo ci) {
        try {
            Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 1.5f);
        } catch (Exception ignored) {
            // Avoid noisy stack traces during entity creation; field may be absent in this runtime.
        }
    }
}
