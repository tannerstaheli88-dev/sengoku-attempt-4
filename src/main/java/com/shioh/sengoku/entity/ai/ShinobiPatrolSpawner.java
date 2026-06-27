package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom spawner for night-time shinobi encounters.
 * Spawns a single Illusioner on the surface on blocks listed in sengoku:shinobi_spawnable_on.
 */
public class ShinobiPatrolSpawner implements CustomSpawner {
    public static final ShinobiPatrolSpawner INSTANCE = new ShinobiPatrolSpawner();
    private static final TagKey<Block> ALLOWED_GROUND = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("sengoku", "shinobi_spawnable_on")
    );
    private static final int MIN_SPAWN_DISTANCE = 150;
    private static final long SPAWN_RECORD_LIFETIME = 6000L;

    private int nextTick;
    private final List<SpawnRecord> recentSpawns = new ArrayList<>();

    private static class SpawnRecord {
        final BlockPos pos;
        final long time;

        SpawnRecord(BlockPos pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }

    @Override
    public int tick(ServerLevel level, boolean spawnHostiles, boolean spawnPassives) {
        if (!spawnHostiles) return 0;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) return 0;

        RandomSource random = level.random;
        --this.nextTick;
        if (this.nextTick > 0) return 0;

        SengokuConfig config = SengokuConfig.getInstance();
        if (!config.shinobiPatrolsEnabled) return 0;

        this.nextTick = config.shinobiPatrolCheckInterval + random.nextInt(Math.max(1, config.shinobiPatrolCheckInterval));

        long currentTime = level.getGameTime();
        recentSpawns.removeIf(record -> currentTime - record.time > SPAWN_RECORD_LIFETIME);

        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 13000L || dayTime > 23000L) return 0;

        if (random.nextDouble() > config.shinobiPatrolSpawnChance) return 0;

        var validPlayers = level.players().stream().filter(p -> !p.isSpectator()).toList();
        if (validPlayers.isEmpty()) return 0;

        var player = validPlayers.get(random.nextInt(validPlayers.size()));
        int spawnDistance = (20 + random.nextInt(29)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable().move(spawnDistance, 0, spawnDistance);
        spawnPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()));

        if (!level.isLoaded(spawnPos)) return 0;
        if (!level.canSeeSky(spawnPos)) return 0;

        BlockPos immutablePos = spawnPos.immutable();
        for (SpawnRecord record : recentSpawns) {
            if (record.pos.distSqr(immutablePos) < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) return 0;
        }

        BlockState spawnBlock = level.getBlockState(spawnPos);
        if (!spawnBlock.isAir() && !spawnBlock.canBeReplaced()) return 0;

        BlockState blockBelow = level.getBlockState(spawnPos.below());
        if (!blockBelow.is(ALLOWED_GROUND)) return 0;

        int spawned = this.spawnPatrol(level, spawnPos, random, player);
        if (spawned > 0) {
            recentSpawns.add(new SpawnRecord(immutablePos, level.getGameTime()));
        }
        return spawned;
    }

    public int forceSpawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.spawnPatrol(level, pos, random, null);
    }

    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random, net.minecraft.world.entity.player.Player nearPlayer) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos spawnPos = new BlockPos(pos.getX(), surfaceY, pos.getZ());

        // Base patrol size is 1; increase with nearby players' advancements
        int patrolSize = 1;
        try {
            boolean nearbyHasSmelt = false;
            boolean nearbyHasDiamond = false;
            boolean nearbyHasShiny = false;
            if (nearPlayer != null) {
                if (playerHasSmeltIronAdvancement(nearPlayer)) nearbyHasSmelt = true;
                if (playerHasMineDiamondAdvancement(nearPlayer)) nearbyHasDiamond = true;
                if (playerHasShinyGearAdvancement(nearPlayer)) nearbyHasShiny = true;
            }
            if (!nearbyHasSmelt || !nearbyHasDiamond || !nearbyHasShiny) {
                for (var p : level.players()) {
                    try {
                        if (p.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) <= 64.0 * 64.0) {
                            if (!nearbyHasSmelt && playerHasSmeltIronAdvancement(p)) nearbyHasSmelt = true;
                            if (!nearbyHasDiamond && playerHasMineDiamondAdvancement(p)) nearbyHasDiamond = true;
                            if (!nearbyHasShiny && playerHasShinyGearAdvancement(p)) nearbyHasShiny = true;
                        }
                    } catch (Throwable ignored) {}
                }
            }
            if (nearbyHasSmelt) patrolSize += 1;
            if (nearbyHasDiamond) patrolSize += 1;
            if (nearbyHasShiny) patrolSize += 2;
        } catch (Throwable ignored) {}

        // Cap patrol size to avoid huge groups
        patrolSize = Math.min(6, Math.max(1, patrolSize));

        int spawned = 0;
        for (int i = 0; i < patrolSize; i++) {
            BlockPos s = spawnPos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
            BlockPos finalPos = new BlockPos(s.getX(), surface, s.getZ());
            if (!level.isLoaded(finalPos)) continue;
            if (!level.getBlockState(finalPos).isAir() && !level.getBlockState(finalPos).canBeReplaced()) continue;
            if (this.spawnLeader(level, finalPos, random)) spawned++;
        }

        return spawned;
    }

    private boolean spawnLeader(ServerLevel level, BlockPos pos, RandomSource random) {
        var entity = EntityType.ILLUSIONER.create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) return false;

        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        level.addFreshEntity(mob);
        mob.addTag("sengoku_patrol");
        return true;
    }

    private boolean playerHasSmeltIronAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("shioh", "main/hunting_rats");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean playerHasMineDiamondAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("shioh", "main/man_without_honor");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean playerHasShinyGearAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/shiny_gear");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }
}