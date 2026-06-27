package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Endermen (Kuchisake-onna) burn in daylight like undead/yokai
 */
@Mixin(EnderMan.class)
public class EndermanBurnInDaylightMixin {
    
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void sengoku$burnInDaylight(CallbackInfo ci) {
        EnderMan enderman = (EnderMan) (Object) this;
        
        // Don't burn during mist weather (mist provides cover like rain/clouds)
        if (com.shioh.sengoku.system.MistWeatherSystem.isMisty(enderman.level())) {
            return;
        }
        
        // Check if it's daytime, in the Overworld, and the enderman is in sunlight
        if (enderman.level().isDay() && !enderman.level().isClientSide && enderman.level().dimension() == Level.OVERWORLD) {
            float brightness = enderman.getLightLevelDependentMagicValue();
            
            // If exposed to sunlight (brightness > 0.5), teleport far away and despawn
            if (brightness > 0.5F && enderman.level().canSeeSky(enderman.blockPosition())) {
                try {
                    // Attempt to teleport the enderman a large distance away to simulate fleeing
                    double angle = enderman.getRandom().nextDouble() * Math.PI * 2.0;
                    int distance = 200 + enderman.getRandom().nextInt(200); // 200-399 blocks
                    int tx = enderman.blockPosition().getX() + (int)(Math.cos(angle) * distance);
                    int tz = enderman.blockPosition().getZ() + (int)(Math.sin(angle) * distance);
                    int ty = enderman.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);

                    // Teleport and then discard so it doesn't linger in daylight
                    enderman.teleportTo((double)tx + 0.5D, (double)ty, (double)tz + 0.5D);
                    enderman.discard();
                } catch (Throwable ignored) {
                    // If teleport fails for any reason, fall back to normal despawn
                    enderman.discard();
                }
            }
        }
    }
}
