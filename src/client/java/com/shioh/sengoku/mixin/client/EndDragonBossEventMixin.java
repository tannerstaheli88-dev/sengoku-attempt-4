package com.shioh.sengoku.mixin.client;

import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents the boss event from creating the screen darkening effect.
 * The dragon fight creates a boss event that triggers both music and darkness.
 */
@Mixin(BossEvent.class)
public class EndDragonBossEventMixin {

    @Inject(method = "shouldCreateWorldFog", at = @At("HEAD"), cancellable = true)
    private void sengoku$disableBossFog(CallbackInfoReturnable<Boolean> cir) {
        // Always return false to prevent any boss from creating world fog/darkness
        cir.setReturnValue(false);
    }

    @Inject(method = "shouldDarkenScreen", at = @At("HEAD"), cancellable = true)
    private void sengoku$disableScreenDarkening(CallbackInfoReturnable<Boolean> cir) {
        // Always return false to prevent screen darkening during boss fights
        cir.setReturnValue(false);
    }
}
