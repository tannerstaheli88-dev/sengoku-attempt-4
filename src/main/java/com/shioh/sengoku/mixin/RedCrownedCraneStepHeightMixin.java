package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.RedCrownedCraneEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(RedCrownedCraneEntity.class)
public abstract class RedCrownedCraneStepHeightMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$setStepHeight(CallbackInfo ci) {
        try {
            Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 1.0f);
        } catch (Exception ignored) {
            // Field may be obfuscated or missing on some mappings; ignore quietly.
        }
    }
}
