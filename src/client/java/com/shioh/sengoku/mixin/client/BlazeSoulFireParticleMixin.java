package com.shioh.sengoku.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Swaps Blaze combat flame particles to soul fire when in the Nether.
 * Intercepts particle spawning on the client to change FLAME to SOUL_FIRE_FLAME.
 */
@Mixin(ClientLevel.class)
public class BlazeSoulFireParticleMixin {

    @ModifyVariable(
        method = "addParticle",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ParticleOptions sengoku$swapFlameToSoulFire(ParticleOptions particle) {
        ClientLevel level = (ClientLevel) (Object) this;
        
        // Only swap FLAME particles to SOUL_FIRE_FLAME when in the Nether
        if (level.dimension() == Level.NETHER && particle == ParticleTypes.FLAME) {
            return ParticleTypes.SOUL_FIRE_FLAME;
        }
        
        return particle;
    }
}
