package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * Custom spawner for night-time zombie patrols.
 * Spawns a patrol led by an Oni Brute with several regular zombies at night.
 */
public class NightZombiePatrolSpawner implements CustomSpawner {
    public static final NightZombiePatrolSpawner INSTANCE = new NightZombiePatrolSpawner();
    private int nextTick;
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
        this.nextTick = config.nightZombiePatrolCheckInterval + random.nextInt(Math.max(1, config.nightZombiePatrolCheckInterval));

        long currentTime = level.getGameTime();
        recentSpawns.removeIf(record -> currentTime - record.time > SPAWN_RECORD_LIFETIME);

        // Only spawn at night (13000 - 23000)
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 13000L || dayTime > 23000L) {
            return 0;
        }

        if (random.nextFloat() > config.nightZombiePatrolSpawnChance) return 0;

        int playerCount = level.players().size();
        if (playerCount < 1) return 0;

        var player = level.players().get(random.nextInt(playerCount));
        if (player.isSpectator()) return 0;

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

        int spawned = this.spawnPatrol(level, spawnPos, random, player);
        if (spawned > 0) recentSpawns.add(new SpawnRecord(immutablePos, level.getGameTime()));
        return spawned;
    }

    public int forceSpawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.spawnPatrol(level, pos, random, null);
    }

    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random, net.minecraft.world.entity.player.Player nearPlayer) {
        // Patrol size: 2-5 members (including leader)
        int patrolSize = 2 + random.nextInt(4);
        int spawned = 0;
        boolean oniBruteUnlocked = hasObtainArmorUnlockNearby(level, pos, nearPlayer);

        for (int i = 0; i < patrolSize; i++) {
            BlockPos spawnPos = pos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
            spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());

            if (i == 0) {
                if (this.spawnPatrolLeader(level, spawnPos, random, oniBruteUnlocked)) spawned++;
            } else {
                if (this.spawnPatrolMember(level, spawnPos, random)) spawned++;
            }
        }

        return spawned;
    }

    private boolean spawnPatrolLeader(ServerLevel level, BlockPos pos, RandomSource random, boolean oniBruteUnlocked) {
        EntityType<?> entityType = oniBruteUnlocked ? ModEntities.ONI_BRUTE : EntityType.ZOMBIE;
        var entity = entityType.create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob leader)) return false;

        leader.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        level.addFreshEntity(leader);
        leader.addTag("sengoku_patrol");
        return true;
    }

    private boolean hasObtainArmorUnlockNearby(ServerLevel level, BlockPos spawnPos, net.minecraft.world.entity.player.Player nearPlayer) {
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

    private boolean playerHasObtainArmorAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/obtain_armor");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean spawnPatrolMember(ServerLevel level, BlockPos pos, RandomSource random) {
        EntityType<?> entityType = EntityType.ZOMBIE;
        var entity = entityType.create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob member)) return false;

        member.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        member.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        level.addFreshEntity(member);
        member.addTag("sengoku_patrol");
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
