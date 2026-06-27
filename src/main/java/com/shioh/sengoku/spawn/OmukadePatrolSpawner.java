package com.shioh.sengoku.spawn;

import com.shioh.sengoku.entity.OmukadeEntity;
import com.shioh.sengoku.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Periodic roaming spawner for Omukade.
 * Favors underground cave patrols, with an occasional surface patrol spawn.
 */
public final class OmukadePatrolSpawner {
    private static final TagKey<Biome> OMUKADE_NOT_SPAWNABLE = TagKey.create(
        Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("sengoku", "omukade_not_spawnable")
    );
    private static final int INTERVAL_TICKS = 1800;
    private static final int MIN_SPAWNS_PER_INTERVAL = 0;
    private static final int MAX_SPAWNS_PER_INTERVAL = 1;
    private static final double SURFACE_PATROL_CHANCE = 0.18D;
    private static final int MIN_RADIUS = 28;
    private static final int MAX_RADIUS = 72;
    private static final int MAX_NEARBY_OMUKADE = 1;
    private static final int SINGLE_INSTANCE_CHECK_RADIUS = 160;

    private OmukadePatrolSpawner() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    if (level.dimension() != Level.OVERWORLD) {
                        return;
                    }

                    ServerLevel slevel = (ServerLevel) level;
                    long time = slevel.getGameTime();
                    if (time % INTERVAL_TICKS != 0) {
                        return;
                    }

                    trySpawnPatrol(slevel);
                } catch (Throwable ignored) {
                }
            });
        });
    }

    private static void trySpawnPatrol(ServerLevel slevel) {
        if (!slevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        List<ServerPlayer> players = slevel.getPlayers(player -> !player.isSpectator());
        if (players.isEmpty()) {
            return;
        }

        RandomSource rand = slevel.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));

        if (hasActiveOmukadeNearAnyPlayer(slevel, players)) {
            return;
        }

        if (!hasObtainArmorUnlockNearby(slevel, player.blockPosition(), player)) {
            return;
        }

        int attempts = MIN_SPAWNS_PER_INTERVAL + rand.nextInt(MAX_SPAWNS_PER_INTERVAL - MIN_SPAWNS_PER_INTERVAL + 1);
        for (int attempt = 0; attempt < attempts; attempt++) {
            boolean surfacePatrol = rand.nextDouble() < SURFACE_PATROL_CHANCE;
            BlockPos spawnPos = surfacePatrol
                ? findSurfaceSpawn(slevel, player, rand)
                : findUndergroundSpawn(slevel, player, rand);

            if (spawnPos == null) {
                continue;
            }

            if (isBlockedBiome(slevel, spawnPos)) {
                continue;
            }

            AABB nearbyBox = new AABB(spawnPos).inflate(96.0D, 48.0D, 96.0D);
            int existing = slevel.getEntitiesOfClass(OmukadeEntity.class, nearbyBox, Entity::isAlive).size();
            if (existing >= MAX_NEARBY_OMUKADE) {
                return;
            }

            if (spawnOmukade(slevel, spawnPos)) {
                break;
            }
        }
    }

    private static boolean hasActiveOmukadeNearAnyPlayer(ServerLevel level, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            AABB checkBox = new AABB(player.blockPosition()).inflate(SINGLE_INSTANCE_CHECK_RADIUS, 64.0D, SINGLE_INSTANCE_CHECK_RADIUS);
            if (!level.getEntitiesOfClass(OmukadeEntity.class, checkBox, Entity::isAlive).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos findUndergroundSpawn(ServerLevel level, ServerPlayer player, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos base = randomRingPos(player, random);
            int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
            int startY = Math.min(topY - 4, Math.max(player.blockPosition().getY() + 10, level.getSeaLevel() - 4));
            int minY = Math.max(level.getMinBuildHeight() + 12, Math.min(player.blockPosition().getY() - 26, level.getSeaLevel() - 24));

            for (int y = startY; y >= minY; y--) {
                BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
                if (isValidUndergroundPatrolSpawn(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static BlockPos findSurfaceSpawn(ServerLevel level, ServerPlayer player, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos base = randomRingPos(player, random);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
            BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
            if (isValidSurfacePatrolSpawn(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos randomRingPos(ServerPlayer player, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = MIN_RADIUS + random.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
        int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
        return new BlockPos(x, player.blockPosition().getY(), z);
    }

    private static boolean isValidUndergroundPatrolSpawn(ServerLevel level, BlockPos pos) {
        if (pos.getY() > level.getSeaLevel() - 8) {
            return false;
        }
        if (level.canSeeSky(pos)) {
            return false;
        }
        if (level.getMaxLocalRawBrightness(pos) > 7) {
            return false;
        }
        return hasSolidGroundAndSpace(level, pos);
    }

    private static boolean isValidSurfacePatrolSpawn(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getSeaLevel() - 2) {
            return false;
        }
        if (!level.canSeeSky(pos)) {
            return false;
        }
        if (level.getMaxLocalRawBrightness(pos) < 8) {
            return false;
        }
        return hasSolidGroundAndSpace(level, pos);
    }

    private static boolean hasSolidGroundAndSpace(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.isAir() || ground.getCollisionShape(level, below).isEmpty()) {
            return false;
        }
        return level.getBlockState(pos).isAir()
            && level.getBlockState(pos.above()).isAir()
            && !level.getBlockState(below).getFluidState().isSource();
    }

    private static boolean isBlockedBiome(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(OMUKADE_NOT_SPAWNABLE);
    }

    private static boolean spawnOmukade(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return false;
        }

        Entity entity = ModEntities.OMUKADE.create(level);
        if (!(entity instanceof OmukadeEntity omukade)) {
            return false;
        }

        omukade.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        omukade.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        return level.addFreshEntity(omukade);
    }

    private static boolean hasObtainArmorUnlockNearby(ServerLevel level, BlockPos spawnPos, ServerPlayer nearPlayer) {
        try {
            if (nearPlayer != null && playerHasObtainArmorAdvancement(nearPlayer)) {
                return true;
            }

            for (var p : level.players()) {
                try {
                    if (p.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) <= 64.0 * 64.0) {
                        if (playerHasObtainArmorAdvancement(p)) {
                            return true;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean playerHasObtainArmorAdvancement(ServerPlayer player) {
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/obtain_armor");
            var advancements = player.getAdvancements();
            var progress = advancements.getOrStartProgress(
                player.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
