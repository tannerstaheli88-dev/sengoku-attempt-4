package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;

import java.util.ArrayList;
import java.util.List;
/**
 * Custom spawner for Yuki Onna. Mirrors `KuchisakaOnnaPatrolSpawner` behavior but
 * adds a snowy-biome requirement and spawns the `YukiOnna` entity.
 */
public class YukiOnnaPatrolSpawner implements CustomSpawner {
    public static final YukiOnnaPatrolSpawner INSTANCE = new YukiOnnaPatrolSpawner();
    private static final TagKey<Biome> YUKI_ONNA_SPAWNABLE = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("sengoku", "yuki_onna_spawnable"));
    private static final Map<ResourceKey<Level>, Long> nextAttemptExpiry = new ConcurrentHashMap<>(); // per-dimension expiry tick
    private final List<SpawnRecord> recentSpawns = new ArrayList<>();
    private static final int MIN_SPAWN_DISTANCE = 150;
    private static final long SPAWN_RECORD_LIFETIME = 1000; // 5 minutes

    private static class SpawnRecord {
        final BlockPos pos;
        final long time;

        SpawnRecord(BlockPos pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }

public static void recordDeath(ServerLevel level, long time) {
    try {
        SengokuConfig config = SengokuConfig.getInstance();
        long expiry = time + config.yukiOnnaCheckInterval; // same cooldown as a spawn
        nextAttemptExpiry.put(level.dimension(), expiry);
    } catch (Throwable ignored) {}
}

    public static void recordSpawn(ServerLevel level, long time) {
        try {
            SengokuConfig config = SengokuConfig.getInstance();
            long expiry = time + config.yukiOnnaCheckInterval;
            nextAttemptExpiry.put(level.dimension(), expiry);
        } catch (Throwable ignored) {}
    }

    @Override
    public int tick(ServerLevel level, boolean spawnHostiles, boolean spawnPassives) {
        if (!spawnHostiles) return 0;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: patrol spawning disabled by gamerule");
            return 0;
        }

        RandomSource random = level.random;
        long currentTime = level.getGameTime();
        SengokuConfig config = SengokuConfig.getInstance();

        // Respect the spawn-based cooldown window before another attempt.
        try {
            Long expiry = nextAttemptExpiry.get(level.dimension());
            if (expiry != null && currentTime < expiry) {
                return 0;
            }
        } catch (Throwable ignored) {}

        recentSpawns.removeIf(record -> currentTime - record.time > SPAWN_RECORD_LIFETIME);

        int playerCount = level.players().size();
        if (playerCount < 1) return 0;

        var player = level.players().get(random.nextInt(playerCount));
        if (player.isSpectator()) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: selected player is spectator, skipping");
            return 0;
        }

        // Per-player proximity check: don't spawn if a Yuki already exists near this player
        try {
            var playerBox = player.getBoundingBox().inflate(256.0D);
            if (!level.getEntitiesOfClass(YukiOnnaEntity.class, playerBox, e -> e.isAlive()).isEmpty()) {
                sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: active Yuki near player, skipping spawn");
                return 0;
            }
        } catch (Throwable ignored) {}
        if (player.isSpectator()) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: selected player is spectator, skipping");
            return 0;
        }

        // Check if player has the mine_diamond advancement
        try {
            var serverPlayer = level.getServer().getPlayerList().getPlayer(player.getUUID());
            if (serverPlayer != null) {
                var advancements = serverPlayer.getAdvancements();
                var advancementId = ResourceLocation.parse("minecraft:story/mine_diamond");
                var advancement = level.getServer().getAdvancements().get(advancementId);
                if (advancement != null) {
                    var progress = advancements.getOrStartProgress(advancement);
                    if (progress == null || !progress.isDone()) {
                        sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: player has not obtained mine_diamond advancement, skipping");
                        return 0;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Spawn 20-48 blocks away to keep encounters natural
        int spawnDistance = (20 + random.nextInt(29)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable().move(spawnDistance, 0, spawnDistance);
        spawnPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()));

        if (!level.isLoaded(spawnPos)) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: spawn chunk not loaded at {}", spawnPos);
            return 0;
        }

        BlockPos immutablePos = spawnPos.immutable();
        double weatherMultiplier = level.isThundering()
                ? 1.0D
                : (level.isRainingAt(immutablePos) ? 0.5D : 0.0D);
        if (weatherMultiplier <= 0.0D) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: no rain/thunder at {}", immutablePos);
            return 0;
        }

        double effectiveChance = Math.min(config.yukiOnnaSpawnChance * weatherMultiplier, 1.0D);
        if (random.nextDouble() >= effectiveChance) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: spawn chance roll failed (chance={})", effectiveChance);
            return 0;
        }

        for (SpawnRecord record : recentSpawns) {
            if (record.pos.distSqr(immutablePos) < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) {
                sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: recent spawn too close to {}", immutablePos);
                return 0;
            }
        }

        BlockState spawnBlock = level.getBlockState(spawnPos);
        if (!spawnBlock.isAir() && !spawnBlock.canBeReplaced()) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: spawn block not replaceable at {}", spawnPos);
            return 0;
        }

        BlockState blockBelow = level.getBlockState(spawnPos.below());
        if (!isValidSpawnBlock(blockBelow)) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: invalid ground block {} at {}", blockBelow, spawnPos.below());
            return 0;
        }

        // Biome must be in the yuki_onna_spawnable tag
        Holder<Biome> biomeHolder = level.getBiome(immutablePos);
        if (!biomeHolder.is(YUKI_ONNA_SPAWNABLE)) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: biome not in tag at {}", immutablePos);
            return 0;
        }

        // Require precipitation (rain/snow) at the spawn position or a thunderstorm
        if (!level.isThundering() && !level.isRainingAt(immutablePos)) {
            sengokuFabric.LOGGER.debug("YukiOnnaPatrolSpawner: no precipitation at {} (isRainingAt={}, isThundering={})", immutablePos, level.isRainingAt(immutablePos), level.isThundering());
            return 0;
        }
        // Random delay gate: only attempt ~10% of eligible ticks to avoid instant predictable spawns
        if (random.nextDouble() >= 0.001) {
            return 0;
        }
        int spawned = this.spawnPatrol(level, spawnPos, random);
        if (spawned > 0) recentSpawns.add(new SpawnRecord(immutablePos, level.getGameTime()));
        return spawned;
    }

    public boolean isReadyToAttemptSpawn(ServerLevel level) {
        try {
            Long expiry = nextAttemptExpiry.get(level.dimension());
            if (expiry == null) return true;
            return level.getGameTime() >= expiry;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Returns how many ticks remain until the next allowed attempt (>=0).
     */
    public long getTicksUntilNextAttempt(ServerLevel level) {
        try {
            Long expiry = nextAttemptExpiry.get(level.dimension());
            if (expiry == null) return 0L;
            long ticks = expiry - level.getGameTime();
            return Math.max(0L, ticks);
        } catch (Throwable ignored) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Reset the next-attempt timer so the spawner may attempt again immediately.
     */
    public void resetNextAttempt(ServerLevel level) {
        try {
            nextAttemptExpiry.put(level.dimension(), level.getGameTime());
        } catch (Throwable ignored) {}
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
        var yuki = ModEntities.YUKI_ONNA.create(level);
        if (yuki == null || !(yuki instanceof net.minecraft.world.entity.Mob mob)) return false;

        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        level.addFreshEntity(mob);
        recordSpawn(level, level.getGameTime());
        mob.addTag("sengoku_patrol");
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
               state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK) ||
               state.is(net.minecraft.world.level.block.Blocks.ICE) ||
               state.is(net.minecraft.world.level.block.Blocks.PACKED_ICE);
    }
}
