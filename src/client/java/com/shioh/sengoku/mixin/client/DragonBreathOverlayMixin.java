package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.particle.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;

/**
 * Adds an overlay particle `dragon_splash` whenever the vanilla
 * `DRAGON_BREATH` particle is spawned. This does not replace the
 * vanilla particle; it simply spawns our custom particle alongside it.
 */
@Mixin(ClientLevel.class)
public class DragonBreathOverlayMixin {

    private static final double MAX_DIST_SQ = 32 * 32;
    private static final double SPAWN_CHANCE = 0.03;

    @Inject(method = "addParticle", at = @At("HEAD"))
    private void sengoku$overlayDragonBreath(ParticleOptions particle, double x, double y, double z,
                                              double vx, double vy, double vz, CallbackInfo ci) {
        if (particle.getType() != ParticleTypes.DRAGON_BREATH) return;

        ClientLevel level = (ClientLevel) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.player == null) return;

        double dx = mc.player.getX() - x;
        double dy = mc.player.getY() - y;
        double dz = mc.player.getZ() - z;
        if (dx*dx + dy*dy + dz*dz > MAX_DIST_SQ) return;

        if (level.random.nextDouble() >= SPAWN_CHANCE) return;

        level.addParticle(ModParticles.DRAGON_SPLASH, true, x, y, z, vx, vy, vz);
    }
}