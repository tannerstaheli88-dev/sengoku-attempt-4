package com.shioh.sengoku.mixin;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.natamus.treeharvester.util.Util")
public class TreeHarvesterUtilMixin {
    /**
     * Inject at the head of Util.isTreeLog and return true for stripped logs.
     * This is more robust than redirecting String.contains and avoids relying
     * on invocation locations which can change across versions.
     */
    @Inject(method = "isTreeLog(Lnet/minecraft/world/level/block/Block;)Z", at = @At("HEAD"), cancellable = true)
    private static void injectIsTreeLog(Block block, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (block != null) {
                String name = block.getName().getString();
                if (name.toLowerCase().contains("stripped")) {
                    cir.setReturnValue(true);
                }
            }
        }
        catch (Throwable ignored) {
            // Be defensive; do not crash if Tree Harvester internals differ
        }
    }
}
