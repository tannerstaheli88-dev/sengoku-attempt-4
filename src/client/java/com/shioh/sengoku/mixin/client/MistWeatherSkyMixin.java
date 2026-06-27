package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables skybox rendering during mist weather for better atmosphere
 */
@Mixin(LevelRenderer.class)
public class MistWeatherSkyMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void sengoku$cancelSkyDuringMist(CallbackInfo ci) {
        // Cancel sky rendering during mist weather
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && com.shioh.sengoku.system.MistWeatherSystem.isMisty(mc.level)) {
            ci.cancel();
        }
    }
}
