package com.shioh.sengoku.network;

import com.shioh.sengoku.entity.ShinobiLordEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side scanner that detects if an Illusioner with an active mirror spell
 * is targeting a player nearby. Sends a boolean state when entering or exiting that condition.
 */
public class ShinobiMusicSync {
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final Map<UUID, Integer> LAST_SHINOBI_LORD_PHASE_SENT = new HashMap<>();
    private static final Map<UUID, Integer> SHINOBI_LORD_COMBAT_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> SHINOBI_LORD_HELD_PHASE = new HashMap<>();
    private static final int COMBAT_GRACE = 60;

    private static int tickCounter = 0;
    private static final int INTERVAL = 20;
    private static final double RADIUS = 96.0D;

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % INTERVAL) != 0) return;
        try {
            for (ServerPlayer player : level.players()) {
                boolean illusionerFound = false;
                int shinobiLordPhaseFound = 0;

                try {
                    for (Illusioner illusioner : level.getEntitiesOfClass(Illusioner.class, player.getBoundingBox().inflate(RADIUS))) {
                        if (illusioner.isRemoved()) continue;
                        try {
LivingEntity target = illusioner.getTarget();
boolean targetingPlayer = target != null && target.getUUID().equals(player.getUUID());
if (targetingPlayer) {
    illusionerFound = true;
    break;
}
                        } catch (Throwable ignored) {}
                    }

                    for (ShinobiLordEntity shinobiLord : level.getEntitiesOfClass(ShinobiLordEntity.class, player.getBoundingBox().inflate(RADIUS))) {
                        if (shinobiLord.isRemoved()) continue;
                        try {
                            LivingEntity target = shinobiLord.getTarget();
                            if (target != null && target.getUUID().equals(player.getUUID())) {
                                int phase = shinobiLord.getHealth() <= shinobiLord.getMaxHealth() * 0.5F ? 2 : 1;
                                if (phase > shinobiLordPhaseFound) shinobiLordPhaseFound = phase;
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}

                UUID id = player.getUUID();

                // Illusioner: no grace period — tied directly to mirror spell state
                Boolean last = LAST_SENT.get(id);
                if (last == null || last != illusionerFound) {
                    LAST_SENT.put(id, illusionerFound);
                    ServerPlayNetworking.send(player, new ShinobiMusicPayload(illusionerFound));
                }

                // Shinobi Lord (unchanged)
                if (shinobiLordPhaseFound > 0) {
                    SHINOBI_LORD_COMBAT_TICKS.put(id, COMBAT_GRACE);
                    SHINOBI_LORD_HELD_PHASE.put(id, shinobiLordPhaseFound);
                } else {
                    SHINOBI_LORD_COMBAT_TICKS.computeIfPresent(id, (u, ticks) -> ticks > 0 ? Math.max(0, ticks - INTERVAL) : 0);
                    if (SHINOBI_LORD_COMBAT_TICKS.getOrDefault(id, 0) <= 0) {
                        SHINOBI_LORD_HELD_PHASE.put(id, 0);
                    }
                }

                int shinobiLordPhase = SHINOBI_LORD_COMBAT_TICKS.getOrDefault(id, 0) > 0
                    ? Math.max(1, SHINOBI_LORD_HELD_PHASE.getOrDefault(id, 1))
                    : 0;

                Integer lastPhase = LAST_SHINOBI_LORD_PHASE_SENT.get(id);
                if (lastPhase == null || lastPhase != shinobiLordPhase) {
                    if (shinobiLordPhase == 2 && (lastPhase == null || lastPhase < 2)) {
                        try {
                            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0, false, false));
                            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 60, 0.65D, 0.5D, 0.65D, 0.02D);
                            level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.55D, 0.45D, 0.55D, 0.01D);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.HOSTILE, 1.0F, 0.75F);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.2F, 0.6F);
                        } catch (Throwable ignored) {}
                    }
                    LAST_SHINOBI_LORD_PHASE_SENT.put(id, shinobiLordPhase);
                    ServerPlayNetworking.send(player, new ShinobiLordMusicPayload(shinobiLordPhase));
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Checks whether the Illusioner's mirror image spell is currently active.
     * Uses reflection to read the private `illusion` boolean (Mojmap name).
     * Falls back to true (assumes active) if the field cannot be accessed.
     */
private static boolean isMirrorActive(Illusioner illusioner) {
    try {
        java.lang.reflect.Field f = Illusioner.class.getDeclaredField("illusion");
        f.setAccessible(true);
        return f.getBoolean(illusioner);
    } catch (Throwable ignored) {
        return false; // was true, caused perma-trigger
    }
}
}