package com.shioh.sengoku.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the End dragon darkness overlay effect that obscures the sky/skybox.
 * The dragon fight applies a darkness modifier via getSkyDarken(), not regular fog.
 */
@Mixin(ClientLevel.class)
public class EndDragonDarknessMixin {

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void sengoku$removeEndDragonDarkness(float partialTick, CallbackInfoReturnable<Float> cir) {
        ClientLevel level = (ClientLevel) (Object) this;
        
        // Only interfere if we're in the End dimension
        ResourceKey<Level> dim = level.dimension();
        if (!dim.equals(Level.END)) return;

        // Check if an Ender Dragon is alive (large radius)
        AABB searchBox = new AABB(-256, -64, -256, 256, 320, 256);
        boolean dragonAlive = !level.getEntitiesOfClass(EnderDragon.class, searchBox).isEmpty();

        if (dragonAlive) {
            // Force full brightness (0.0 = no darkening) to see the skybox clearly
            cir.setReturnValue(0.0F);
        }
    }
}
