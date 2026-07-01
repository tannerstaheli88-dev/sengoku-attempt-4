package com.shioh.sengoku.spawn;

import com.shioh.sengoku.entity.UmiBozuEntity;
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

public final class UmiBozuSpawner {

    private static final TagKey<Biome> UMIBOZU_SPAWNABLE = TagKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("sengoku", "umibozu_spawnable")
    );

    private static final int INTERVAL_TICKS = 5600; // ~3 minutes
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 8;
    private static final int MAX_NEARBY_UMIBOZU = 1;
    private static final int SINGLE_INSTANCE_CHECK_RADIUS = 400;
    private static final int MIN_WATER_DEPTH = 16; // needs deep ocean

    private UmiBozuSpawner() {}

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

        // Night only
        if (level.isDay()) return;

        List<ServerPlayer> players = level.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty()) return;

        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));

        if (hasActiveUmiBozuNearAnyPlayer(level, players)) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Umi Bozu spawn blocked: one already exists nearby");
            return;
        }

        BlockPos spawnPos = findOceanSpawn(level, player, rand);
        if (spawnPos == null) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Umi Bozu spawn blocked: no valid ocean spawn found near {}", player.blockPosition());
            return;
        }

        if (!isSpawnableBiome(level, spawnPos)) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Umi Bozu spawn blocked: biome not in spawnable tag at {}", spawnPos);
            return;
        }

        AABB nearbyBox = new AABB(spawnPos).inflate(120.0D, 60.0D, 120.0D);
        int existing = level.getEntitiesOfClass(UmiBozuEntity.class, nearbyBox, Entity::isAlive).size();
        if (existing >= MAX_NEARBY_UMIBOZU) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Umi Bozu spawn blocked: too many nearby at {}", spawnPos);
            return;
        }

        boolean spawned = spawnUmiBozu(level, spawnPos);
        com.shioh.sengoku.sengokuFabric.LOGGER.info("Umi Bozu spawn attempt at {}: {}", spawnPos, spawned ? "SUCCESS" : "FAILED");
    }

    private static boolean hasActiveUmiBozuNearAnyPlayer(ServerLevel level, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            AABB box = new AABB(player.blockPosition()).inflate(SINGLE_INSTANCE_CHECK_RADIUS, 64.0D, SINGLE_INSTANCE_CHECK_RADIUS);
            if (!level.getEntitiesOfClass(UmiBozuEntity.class, box, Entity::isAlive).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos findOceanSpawn(ServerLevel level, ServerPlayer player, RandomSource rand) {
        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double radius = MIN_RADIUS + rand.nextDouble() * (MAX_RADIUS - MIN_RADIUS);

            int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, surfaceY - 1, z);

            if (!level.getFluidState(surface).is(FluidTags.WATER)) continue;

            // Scan down to ocean floor
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

            // Spawn at ocean floor so it rises dramatically
            BlockPos spawnPos = new BlockPos(x, floorY + 1, z);

            if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) continue;
            if (!level.getFluidState(spawnPos.below()).is(FluidTags.WATER)) continue;

            return spawnPos;
        }
        return null;
    }

    private static boolean isSpawnableBiome(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(UMIBOZU_SPAWNABLE);
    }

    private static boolean spawnUmiBozu(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return false;

        Entity entity = ModEntities.UMI_BOZU.create(level);
        if (!(entity instanceof UmiBozuEntity umiBozu)) return false;

        umiBozu.moveTo(
            pos.getX() + 0.5D,
            pos.getY() + 0.5D,
            pos.getZ() + 0.5D,
            level.getRandom().nextFloat() * 360.0F,
            0.0F
        );

        umiBozu.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(pos),
            MobSpawnType.NATURAL,
            null
        );

        return level.addFreshEntity(umiBozu);
    }
}