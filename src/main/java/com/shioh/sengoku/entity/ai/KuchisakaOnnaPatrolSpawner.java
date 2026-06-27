package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom spawner for the "kuchisakaonna" patrol event.
 * Spawns a single Enderman occasionally at night, with a cooldown after each successful spawn.
 */
public class KuchisakaOnnaPatrolSpawner implements CustomSpawner {
    public static final KuchisakaOnnaPatrolSpawner INSTANCE = new KuchisakaOnnaPatrolSpawner();
    private int nextTick;
    private long spawnCooldownExpiry = 0; // game time after which another spawn is allowed
    private final List<SpawnRecord> recentSpawns = new ArrayList<>();
    private static final int MIN_SPAWN_DISTANCE = 150;
    private static final long SPAWN_RECORD_LIFETIME = 6000; // 5 minutes

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
        this.nextTick = config.kuchisakaOnnaCheckInterval;

        long currentTime = level.getGameTime();

        // Respect post-spawn cooldown — don't attempt again until it expires
        if (currentTime < spawnCooldownExpiry) return 0;

        recentSpawns.removeIf(record -> currentTime - record.time > SPAWN_RECORD_LIFETIME);

        // Only spawn at night (13000 - 23000) to match yokai theme
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 13000L || dayTime > 23000L) return 0;

        if (random.nextFloat() > config.kuchisakaOnnaSpawnChance) return 0;

        int playerCount = level.players().size();
        if (playerCount < 1) return 0;

        var player = level.players().get(random.nextInt(playerCount));
        if (player.isSpectator()) return 0;

        // Per-player proximity check: don't spawn if an Enderman already exists near this player
        try {
            var playerBox = player.getBoundingBox().inflate(256.0D);
            var endermenNearby = level.getEntitiesOfClass(net.minecraft.world.entity.monster.EnderMan.class, playerBox, e -> e.isAlive());
            if (!endermenNearby.isEmpty()) return 0;
        } catch (Throwable ignored) {}

        // Spawn 20-48 blocks away to keep encounters natural
        int spawnDistance = (20 + random.nextInt(29)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable().move(spawnDistance, 0, spawnDistance);
        spawnPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()));

        if (!level.isLoaded(spawnPos)) return 0;

        BlockPos immutablePos = spawnPos.immutable();
        for (SpawnRecord record : recentSpawns) {
            if (record.pos.distSqr(immutablePos) < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) return 0;
        }

        BlockState spawnBlock = level.getBlockState(spawnPos);
        if (!spawnBlock.isAir() && !spawnBlock.canBeReplaced()) return 0;

        BlockState blockBelow = level.getBlockState(spawnPos.below());
        if (!isValidSpawnBlock(blockBelow)) return 0;

        // Random delay gate: prevents instant predictable spawns once conditions are met
        if (random.nextDouble() >= 0.10) return 0;

        int spawned = this.spawnPatrol(level, spawnPos, random);
        if (spawned > 0) {
            recentSpawns.add(new SpawnRecord(immutablePos, currentTime));
            spawnCooldownExpiry = currentTime + config.kuchisakaOnnaCheckInterval;
        }
        return spawned;
    }

    public int forceSpawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.spawnPatrol(level, pos, random);
    }

    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        int spawned = 0;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos spawnPos = new BlockPos(pos.getX(), surfaceY, pos.getZ());

        if (this.spawnLeader(level, spawnPos, random)) spawned++;
        return spawned;
    }

    private boolean spawnLeader(ServerLevel level, BlockPos pos, RandomSource random) {
        EntityType<?> entityType = EntityType.ENDERMAN;
        var entity = entityType.create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob leader)) return false;

        leader.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        level.addFreshEntity(leader);
        leader.addTag("sengoku_patrol");
        return true;
    }

    private boolean isValidSpawnBlock(BlockState state) {
        return state.is(net.minecraft.world.level.block.Blocks.DIRT) ||
               state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) ||
               state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) ||
               state.is(net.minecraft.world.level.block.Blocks.SAND) ||
               state.is(net.minecraft.world.level.block.Blocks.GRAVEL) ||
               state.is(net.minecraft.world.level.block.Blocks.ROOTED_DIRT) ||
               state.is(net.minecraft.world.level.block.Blocks.PODZOL) ||
               state.is(net.minecraft.world.level.block.Blocks.SNOW) ||
               state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK);
    }
}