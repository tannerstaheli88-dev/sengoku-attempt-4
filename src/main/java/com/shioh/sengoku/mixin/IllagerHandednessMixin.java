package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class IllagerHandednessMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$forceIllagersRightHanded(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self instanceof AbstractIllager) {
            self.setLeftHanded(false);
        }
    }
}