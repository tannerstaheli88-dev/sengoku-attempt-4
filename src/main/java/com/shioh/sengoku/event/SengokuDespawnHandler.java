package com.shioh.sengoku.event;

import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles natural despawn conditions for boss mobs with reduced overhead:
 *  - Giants despawn during daytime (night window = ticks 13000-23000) unless persistent.
 *  - Yuki Onna despawns when weather is clear (no rain/snow at her position) unless persistent.
 *
 * Scans only active loaded areas around players and spawn, and runs less frequently
 * to reduce allocation and GC pressure.
 */
public final class SengokuDespawnHandler {

    private static final int DESPAWN_CHECK_INTERVAL_TICKS = 100; // once every 5 seconds
    private static final double DESPAWN_SEARCH_RADIUS = 256.0D;

    private SengokuDespawnHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % DESPAWN_CHECK_INTERVAL_TICKS != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                if (level.dimension() != Level.OVERWORLD) continue;

                tickGiantDespawn(level);
                tickYukiOnnaDespawn(level);
            }
        });
    }

    private static void tickGiantDespawn(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        boolean isNight = time >= 13000L && time <= 23000L;
        if (isNight) return; // Nothing to despawn at night

        List<AABB> searchAreas = buildSearchAreas(level);
        if (searchAreas.isEmpty()) return;

        for (AABB area : searchAreas) {
            List<Giant> giants = level.getEntities(EntityType.GIANT, area,
                    giant -> !giant.isPersistenceRequired()
                            && !giant.getTags().contains("brain")
                            && !giant.getTags().contains("finale"));
            for (Giant giant : giants) {
                giant.discard();
            }
        }
    }

    private static void tickYukiOnnaDespawn(ServerLevel level) {
        if (level.isRaining()) return;

        List<AABB> searchAreas = buildSearchAreas(level);
        if (searchAreas.isEmpty()) return;

        for (AABB area : searchAreas) {
            List<YukiOnnaEntity> yukiList = level.getEntitiesOfClass(YukiOnnaEntity.class, area,
                    yuki -> yuki.getTags().contains("yuki_natural"));
            for (YukiOnnaEntity yuki : yukiList) {
                yuki.discard();
            }
        }
    }

    private static List<AABB> buildSearchAreas(ServerLevel level) {
        List<AABB> areas = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            areas.add(player.getBoundingBox().inflate(DESPAWN_SEARCH_RADIUS));
        }

        BlockPos spawnPos = level.getSharedSpawnPos();
        if (level.isLoaded(spawnPos)) {
            areas.add(new AABB(spawnPos).inflate(DESPAWN_SEARCH_RADIUS));
        }

        return areas;
    }
}
