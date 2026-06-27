package com.shioh.sengoku.mixin;

import com.shioh.sengoku.worldgen.AdjustableBeardRegistry;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("all")
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
  @Inject(method = "loadLevel", at = @At("TAIL"))
  private void onWorldLoad(CallbackInfo ci) {
    AdjustableBeardRegistry.clear();
  }

  @Inject(method = "stopServer", at = @At("HEAD"))
  private void onServerStop(CallbackInfo ci) {
    AdjustableBeardRegistry.clear();
  }
} 