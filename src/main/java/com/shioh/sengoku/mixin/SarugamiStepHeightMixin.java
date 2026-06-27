package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.SarugamiEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(SarugamiEntity.class)
public abstract class SarugamiStepHeightMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$setStepHeight(CallbackInfo ci) {
        try {
            Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            // Slightly higher than default so Sarugami can traverse small obstacles
            stepHeightField.setFloat(this, 1.2f);
        } catch (Exception ignored) {
            // Ignore on mappings where the field is unavailable
        }
    }
}
