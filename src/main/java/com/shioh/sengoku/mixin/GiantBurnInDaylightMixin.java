package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Giants take sunlight/fire damage during daytime similar to zombies/skeletons.
 */
@Mixin(Giant.class)
public class GiantBurnInDaylightMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$burnInDaylight(CallbackInfo ci) {
        Giant giant = (Giant) (Object) this;

        // If it's daytime in the Overworld, make the giant vanish in smoke (no sky checks).
        if (giant.level().isDay() && !giant.level().isClientSide && giant.level().dimension() == Level.OVERWORLD) {
            if (!giant.level().isClientSide) {
                try {
                    com.shioh.sengoku.sengokuFabric.LOGGER.info("[Giant] Vanish triggered at time {} for entity {}", giant.level().getGameTime(), giant.getStringUUID());
                } catch (Throwable ignored) {}

                for (int i = 0; i < 30; i++) {
                    double px = giant.getX() + (giant.getRandom().nextDouble() - 0.5D) * giant.getBbWidth();
                    double py = giant.getY() + giant.getBbHeight() * 0.5D;
                    double pz = giant.getZ() + (giant.getRandom().nextDouble() - 0.5D) * giant.getBbWidth();
                    giant.level().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0.0D, 0.05D, 0.0D);
                }
                giant.discard();
            }
        }
    }
}
