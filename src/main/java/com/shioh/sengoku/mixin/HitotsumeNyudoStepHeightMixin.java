package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.HitotsumeNyudoEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(HitotsumeNyudoEntity.class)
public abstract class HitotsumeNyudoStepHeightMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$setStepHeight(CallbackInfo ci) {
        try {
            Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 2.0f);
        } catch (Exception ignored) {
            // Field missing on some mappings/versions; ignore to avoid printing stack traces.
        }
    }
}
