package com.shioh.sengoku.system;

import com.shioh.sengoku.registry.ParticleRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manager for ongoing blood sprays that continue for multiple ticks (used when an entity dies).
 */
public class BloodSprayManager {

    private static final Map<ServerLevel, List<ActiveSpray>> active = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                List<ActiveSpray> list = active.get(level);
                if (list == null || list.isEmpty()) continue;

                for (ActiveSpray spray : list.toArray(new ActiveSpray[0])) {
                    // spawn a moderate burst each tick for this spray
                    int perTick = Math.max(4, (int)Math.ceil(spray.damage * 4.0));
                    double spread = 0.04D; // still centered
                    double speed = 0.12 + Math.min(1.0, spray.damage * 0.08);

                    level.sendParticles(ParticleRegistry.BLOOD_PARTICLE,
                        spray.pos.x, spray.pos.y, spray.pos.z,
                        perTick,
                        spread, spread, spread,
                        speed);

                    spray.remainingTicks--;
                    if (spray.remainingTicks <= 0) {
                        list.remove(spray);
                    }
                }
                if (list.isEmpty()) active.remove(level);
            }
        });
    }

    public static void startSpray(ServerLevel level, Vec3 pos, int durationTicks, float damage) {
        active.computeIfAbsent(level, k -> new CopyOnWriteArrayList<>()).add(new ActiveSpray(pos, durationTicks, damage));
    }

    private static class ActiveSpray {
        final Vec3 pos;
        int remainingTicks;
        final float damage;

        ActiveSpray(Vec3 pos, int remainingTicks, float damage) {
            this.pos = pos;
            this.remainingTicks = remainingTicks;
            this.damage = damage;
        }
    }
}
