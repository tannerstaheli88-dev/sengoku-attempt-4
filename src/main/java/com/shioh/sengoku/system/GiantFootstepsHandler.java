package com.shioh.sengoku.system;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.entity.monster.Giant;

public class GiantFootstepsHandler {

    public static void tickFootsteps(Giant giant) {
        // Random chance to play footsteps (20% per tick)
        if (giant.level().random.nextFloat() < 0.2F) {
            giant.level().playSound(
                null,
                giant.blockPosition(),
                SoundRegistry.GIANT_FOOTSTEPS,
                net.minecraft.sounds.SoundSource.HOSTILE,
                1.0F,
                1.0F
            );
        }
    }
}
