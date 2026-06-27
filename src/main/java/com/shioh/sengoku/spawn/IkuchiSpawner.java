package com.shioh.sengoku.spawn;

import com.shioh.sengoku.entity.IkuchiEntity;
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

/**
 * Periodic spawner for Ikuchi.
 * Only spawns in deep ocean water, away from shallow shores.
 */
public final class IkuchiSpawner {

    private static final TagKey<Biome> IKUCHI_SPAWNABLE = TagKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("sengoku", "ikuchi_spawnable")
    );

    private static final int INTERVAL_TICKS = 2400; // test frequency
    private static final int MIN_RADIUS = 32;
    private static final int MAX_RADIUS = 96;
    private static final int MAX_NEARBY_IKUCHI = 1;
    private static final int SINGLE_INSTANCE_CHECK_RADIUS = 300;
    private static final int MIN_WATER_DEPTH = 8;

    private IkuchiSpawner() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    if (level.dimension() != Level.OVERWORLD) return;

                    ServerLevel slevel = (ServerLevel) level;

                    if (slevel.getGameTime() % INTERVAL_TICKS != 0) return;

                    trySpawn(slevel);

                } catch (Throwable ignored) {
                }
            });
        });
    }

    private static void trySpawn(ServerLevel level) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Ikuchi spawn blocked: mob spawning disabled");
            return;
        }

        List<ServerPlayer> players = level.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty()) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Ikuchi spawn blocked: no players");
            return;
        }

        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));

        if (hasActiveIkuchiNearAnyPlayer(level, players)) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info("Ikuchi spawn blocked: one already exists nearby");
            return;
        }

        BlockPos spawnPos = findOceanSpawn(level, player, rand);
        if (spawnPos == null) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info(
                "Ikuchi spawn blocked: no valid ocean spawn found near {}",
                player.blockPosition()
            );
            return;
        }

        if (!isAllowedBiome(level, spawnPos)) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info(
                "Ikuchi spawn blocked: biome not in spawnable tag at {}",
                spawnPos
            );
            return;
        }

        AABB nearbyBox = new AABB(spawnPos).inflate(120.0D, 60.0D, 120.0D);
        int existing = level.getEntitiesOfClass(
            IkuchiEntity.class,
            nearbyBox,
            Entity::isAlive
        ).size();

        if (existing >= MAX_NEARBY_IKUCHI) {
            com.shioh.sengoku.sengokuFabric.LOGGER.info(
                "Ikuchi spawn blocked: too many nearby at {}",
                spawnPos
            );
            return;
        }

        boolean spawned = spawnIkuchi(level, spawnPos);

        com.shioh.sengoku.sengokuFabric.LOGGER.info(
            "Ikuchi spawn attempt at {}: {}",
            spawnPos,
            spawned ? "SUCCESS" : "FAILED"
        );
    }

    private static boolean hasActiveIkuchiNearAnyPlayer(ServerLevel level, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            AABB box = new AABB(player.blockPosition()).inflate(
                SINGLE_INSTANCE_CHECK_RADIUS,
                64.0D,
                SINGLE_INSTANCE_CHECK_RADIUS
            );

            if (!level.getEntitiesOfClass(IkuchiEntity.class, box, Entity::isAlive).isEmpty()) {
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

            // Find actual world surface
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, surfaceY - 1, z);

            // Must actually be water here
            if (!level.getFluidState(surface).is(FluidTags.WATER)) continue;

            // Scan downward until hitting ocean floor
            int floorY = surface.getY();

            for (int y = surface.getY(); y >= level.getMinBuildHeight() + 2; y--) {
                BlockPos check = new BlockPos(x, y, z);

                if (!level.getFluidState(check).is(FluidTags.WATER)) {
                    floorY = y + 1;
                    break;
                }
            }

            int waterDepth = surface.getY() - floorY;

            // Require deep enough water
            if (waterDepth < MIN_WATER_DEPTH) continue;

            // Spawn low in the water column
            int spawnY = floorY + 2 + rand.nextInt(Math.max(1, waterDepth / 2));
            BlockPos spawnPos = new BlockPos(x, spawnY, z);

            if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) continue;
            if (!level.getFluidState(spawnPos.below()).is(FluidTags.WATER)) continue;

            return spawnPos;
        }

        return null;
    }

    private static boolean isAllowedBiome(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(IKUCHI_SPAWNABLE);
    }

    private static boolean spawnIkuchi(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return false;
        }

        Entity entity = ModEntities.IKUCHI.create(level);

        if (!(entity instanceof IkuchiEntity ikuchi)) {
            return false;
        }

        ikuchi.moveTo(
            pos.getX() + 0.5D,
            pos.getY() + 0.5D,
            pos.getZ() + 0.5D,
            level.getRandom().nextFloat() * 360.0F,
            0.0F
        );

        ikuchi.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(pos),
            MobSpawnType.NATURAL,
            null
        );

        return level.addFreshEntity(ikuchi);
    }
}