package com.shioh.sengoku.spawn;

import com.shioh.sengoku.entity.UmiInuEntity;
import com.shioh.sengoku.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class UmiInuSpawner {

    private static final TagKey<Biome> UMI_INU_SPAWNABLE = TagKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("sengoku", "umi_inu_spawnable")
    );

    private static final int INTERVAL_TICKS = 3600; // ~30 seconds
    private static final int MIN_SPAWNS_PER_INTERVAL = 1;
    private static final int MAX_SPAWNS_PER_INTERVAL = 2;
    private static final int MIN_RADIUS = 24;
    private static final int MAX_RADIUS = 80;
    private static final int MAX_NEARBY_AROUND_PLAYER = 2;
    private static final int MIN_WATER_DEPTH = 4; // less strict than Umi Bozu/Ikuchi

    private UmiInuSpawner() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    if (level.dimension() != Level.OVERWORLD) return;
                    ServerLevel slevel = (ServerLevel) level;
                    if (slevel.getGameTime() % INTERVAL_TICKS != 0) return;
                    trySpawn(slevel);
                } catch (Throwable ignored) {}
            });
        });
    }

    private static void trySpawn(ServerLevel level) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;

        List<ServerPlayer> players = level.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty()) return;

        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));

        // Check nearby cap
        AABB nearbyBox = player.getBoundingBox().inflate(MAX_RADIUS, 48.0D, MAX_RADIUS);
        int nearbyExisting = level.getEntitiesOfClass(UmiInuEntity.class, nearbyBox, Entity::isAlive).size();
        if (nearbyExisting >= MAX_NEARBY_AROUND_PLAYER) return;

        int attempts = MIN_SPAWNS_PER_INTERVAL + rand.nextInt(MAX_SPAWNS_PER_INTERVAL - MIN_SPAWNS_PER_INTERVAL + 1);

        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos spawnPos = findWaterSpawn(level, player, rand);
            if (spawnPos == null) continue;

            if (!isSpawnableBiome(level, spawnPos)) continue;

            boolean spawned = spawnUmiInu(level, spawnPos);
            com.shioh.sengoku.sengokuFabric.LOGGER.info(
                "Umi Inu spawn attempt at {}: {}",
                spawnPos,
                spawned ? "SUCCESS" : "FAILED"
            );
        }
    }

    private static BlockPos findWaterSpawn(ServerLevel level, ServerPlayer player, RandomSource rand) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double radius = MIN_RADIUS + rand.nextDouble() * (MAX_RADIUS - MIN_RADIUS);

            int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, surfaceY - 1, z);

            if (!level.getFluidState(surface).is(FluidTags.WATER)) continue;

            // Scan down to find floor
            int floorY = surface.getY();
            for (int y = surface.getY(); y >= level.getMinBuildHeight() + 2; y--) {
                BlockPos check = new BlockPos(x, y, z);
                if (!level.getFluidState(check).is(FluidTags.WATER)) {
                    floorY = y + 1;
                    break;
                }
            }

            int waterDepth = surface.getY() - floorY;
            if (waterDepth < MIN_WATER_DEPTH) continue;

            // Spawn anywhere in the water column, not just the floor
            int spawnY = floorY + 1 + rand.nextInt(Math.max(1, waterDepth - 1));
            BlockPos spawnPos = new BlockPos(x, spawnY, z);

            if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) continue;
            if (!level.getFluidState(spawnPos.below()).is(FluidTags.WATER)) continue;

            return spawnPos;
        }
        return null;
    }

    private static boolean isSpawnableBiome(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(UMI_INU_SPAWNABLE);
    }

    private static boolean spawnUmiInu(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return false;

        Entity entity = ModEntities.UMI_INU.create(level);
        if (!(entity instanceof UmiInuEntity umiInu)) return false;

        umiInu.moveTo(
            pos.getX() + 0.5D,
            pos.getY() + 0.5D,
            pos.getZ() + 0.5D,
            level.getRandom().nextFloat() * 360.0F,
            0.0F
        );

        umiInu.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(pos),
            MobSpawnType.NATURAL,
            null
        );

        return level.addFreshEntity(umiInu);
    }
}