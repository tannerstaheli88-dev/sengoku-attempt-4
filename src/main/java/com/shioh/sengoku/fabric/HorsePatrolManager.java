package com.shioh.sengoku.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.Horse;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Simple manager that tracks patrol horses and despawns them after 1 minute if unclaimed.
 * prevents samurai horses from piling up around my humble abode
 */
public final class HorsePatrolManager {
    private static final Set<HorseEntry> TRACKED = new CopyOnWriteArraySet<>();
    private static final long DESPAWN_TICKS = 1200L; // 1 minute unridden
    private static final int CHECK_INTERVAL = 20; // once per second

    private HorsePatrolManager() {}

    public static void register() {
        sengokuFabric.LOGGER.info("HorsePatrolManager.register() called — registering hooks");
        
        // Entity load hook: re-register horses that have the despawn NBT flag after chunk reload
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof Horse)) return;
            Horse horse = (Horse) entity;
            // Check if this horse has the despawn flag in NBT or entity tag
            try {
                if (horse instanceof com.shioh.sengoku.entity.PatrolHorseAccess) {
                    // Mixin already read NBT; check if needsDespawn is true
                    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                    horse.saveWithoutId(tag);
                    if (tag.getBoolean("NeedsDespawn")) {
                        long spawnTime = tag.getLong("PatrolSpawnTime");
                        // Re-register with manager if not already tracked
                        if (TRACKED.stream().noneMatch(e -> e.getHorse() == horse)) {
                            track(horse, spawnTime);
                            sengokuFabric.LOGGER.info("Re-registered patrol horse from NBT: {}", horse.getStringUUID());
                        }
                    }
                } else if (horse.getTags().contains("sengoku_needs_despawn")) {
                    // Fallback tag present
                    if (TRACKED.stream().noneMatch(e -> e.getHorse() == horse)) {
                        track(horse, world.getGameTime());
                        sengokuFabric.LOGGER.info("Re-registered patrol horse from entity tag: {}", horse.getStringUUID());
                    }
                }
            } catch (Throwable ignored) {}
        });
        
        // Player interaction hook: remove tracking when player interacts
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(entity instanceof Horse)) return InteractionResult.PASS;
            Horse horse = (Horse) entity;
            boolean removed = TRACKED.removeIf(entry -> entry.getHorse() == horse);
            if (removed) sengokuFabric.LOGGER.info("Player interacted with patrol horse — removed tracking: {}", horse.getStringUUID());
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL != 0) return;
            for (ServerLevel level : server.getAllLevels()) {
                long now = level.getGameTime();
                Iterator<HorseEntry> it = TRACKED.iterator();
                while (it.hasNext()) {
                    HorseEntry e = it.next();
                    Horse horse = e.getHorse();
                    if (horse == null || horse.isRemoved() || !horse.level().equals(level)) {
                        TRACKED.remove(e);
                        sengokuFabric.LOGGER.debug("Removing stale tracked horse entry (null/removed/different level)");
                        continue;
                    }
                    // If horse is currently mounted, refresh lastMountedTick to 'now'.
                    if (horse.isVehicle()) {
                        e.lastMountedTick = now;
                        continue;
                    }
                    // If unridden for more than DESPAWN_TICKS and still tracked, despawn.
                    long unriddenFor = now - e.lastMountedTick;
                    if (unriddenFor > DESPAWN_TICKS) {
                        sengokuFabric.LOGGER.info("Despawning unclaimed patrol horse {} after {} ticks unridden", horse.getStringUUID(), unriddenFor);
                        horse.discard();
                        TRACKED.remove(e);
                    }
                }
            }
        });
    }

    public static void track(Horse horse, long gameTime) {
        if (horse == null) return;
        // Initialize lastMountedTick to current game time; while mounted, it will keep refreshing
        TRACKED.add(new HorseEntry(horse, gameTime));
        sengokuFabric.LOGGER.info("Tracking patrol horse {} (startTick={})", horse.getStringUUID(), gameTime);
    }

    public static void onPlayerInteracted(Horse horse) {
        boolean removed = TRACKED.removeIf(entry -> entry.getHorse() == horse);
        if (removed) sengokuFabric.LOGGER.info("Player interaction cleared tracking for horse {}", horse == null ? "<null>" : horse.getStringUUID());
    }

    private static final class HorseEntry {
        private final WeakReference<Horse> horseRef;
        private long lastMountedTick;

        HorseEntry(Horse horse, long startTick) {
            this.horseRef = new WeakReference<>(horse);
            this.lastMountedTick = startTick;
        }

        Horse getHorse() {
            return horseRef.get();
        }
    }
}
