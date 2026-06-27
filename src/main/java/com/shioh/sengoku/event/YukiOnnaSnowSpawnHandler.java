package com.shioh.sengoku.event;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import com.shioh.sengoku.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class YukiOnnaSnowSpawnHandler {

    private static final int MIN_PLAYER_DISTANCE = 32;
    private static final int MAX_PLAYER_DISTANCE = 96;
    private static final int MOB_DESPAWN_RADIUS = 64;
    private static final TagKey<Biome> YUKI_ONNA_SPAWNABLE = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("sengoku", "yuki_onna_spawnable"));

    private YukiOnnaSnowSpawnHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SengokuConfig config = SengokuConfig.getInstance();
            if (!config.yukiOnnaEnabled) return;
            if (server.getTickCount() % config.yukiOnnaCheckInterval != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                if (level.dimension() == Level.OVERWORLD) {
                    trySpawn(level);
                }
            }
        });
    }

    private static void trySpawn(ServerLevel level) {
        if (level.players().isEmpty()) return;

        SengokuConfig config = SengokuConfig.getInstance();
        RandomSource random = level.getRandom();

        // Respect the central spawner's cooldown/expiry so manual and patrol spawners stay in sync.
        try {
            if (!com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.isReadyToAttemptSpawn(level)) return;
        } catch (Throwable ignored) {}

        double weatherMultiplier = level.isThundering() ? 1.0D : (level.isRaining() ? 0.5D : 0.0D);
        double chance = Math.min(config.yukiOnnaSpawnChance * weatherMultiplier, 1.0D);

        if (random.nextDouble() >= chance) return;

        List<ServerPlayer> players = level.players().stream()
                .filter(p -> !p.isSpectator())
                .toList();
        if (players.isEmpty()) return;

        ServerPlayer target = players.get(random.nextInt(players.size()));

        // Avoid full-world scans; only block when another Yuki already exists nearby.
        try {
            AABB nearbyBox = target.getBoundingBox().inflate(256.0D);
            if (!level.getEntitiesOfClass(YukiOnnaEntity.class, nearbyBox, e -> e.isAlive()).isEmpty()) return;
        } catch (Throwable ignored) {}

        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = MIN_PLAYER_DISTANCE + random.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE + 1);
            int dz = MIN_PLAYER_DISTANCE + random.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE + 1);
            if (random.nextBoolean()) dx = -dx;
            if (random.nextBoolean()) dz = -dz;
            int x = target.blockPosition().getX() + dx;
            int z = target.blockPosition().getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight()) continue;
            BlockPos spawnPos = new BlockPos(x, y, z);
            if (!level.isLoaded(spawnPos)) continue;
            BlockState blockBelow = level.getBlockState(spawnPos.below());
            if (!blockBelow.isSolidRender(level, spawnPos.below())) continue;
            if (!level.getBlockState(spawnPos).isAir()) continue;
            if (!level.getBlockState(spawnPos.above()).isAir()) continue;
            Holder<Biome> biome = level.getBiome(spawnPos);
            if (!biome.is(YUKI_ONNA_SPAWNABLE)) continue;
            if (!level.isThundering() && !level.isRainingAt(spawnPos)) continue;
            YukiOnnaEntity yukiOnna = ModEntities.YUKI_ONNA.create(level);
            if (yukiOnna == null) {
                System.out.println("[Sengoku] YukiOnna create() returned null");
                return;
            }
            yukiOnna.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
            yukiOnna.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null);
            yukiOnna.setPersistenceRequired();
            yukiOnna.addTag("yuki_natural");
            level.addFreshEntity(yukiOnna);
            System.out.println("[Sengoku] Yuki Onna spawned at " + spawnPos);
            despawnNearbyHostileMobs(level, spawnPos);
            return;
        }
    }

    public static boolean triggerSpawnNearPlayer(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();

        try {
            AABB nearbyBox = player.getBoundingBox().inflate(256.0D);
            if (!level.getEntitiesOfClass(YukiOnnaEntity.class, nearbyBox, e -> e.isAlive()).isEmpty()) return false;
        } catch (Throwable ignored) {}

        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = MIN_PLAYER_DISTANCE + random.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE + 1);
            int dz = MIN_PLAYER_DISTANCE + random.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE + 1);
            if (random.nextBoolean()) dx = -dx;
            if (random.nextBoolean()) dz = -dz;
            int x = player.blockPosition().getX() + dx;
            int z = player.blockPosition().getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight()) continue;
            BlockPos spawnPos = new BlockPos(x, y, z);
            if (!level.isLoaded(spawnPos)) continue;
            BlockState blockBelow = level.getBlockState(spawnPos.below());
            if (!blockBelow.isSolidRender(level, spawnPos.below())) continue;
            if (!level.getBlockState(spawnPos).isAir()) continue;
            if (!level.getBlockState(spawnPos.above()).isAir()) continue;
            YukiOnnaEntity yukiOnna = ModEntities.YUKI_ONNA.create(level);
            if (yukiOnna == null) return false;
            yukiOnna.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
            yukiOnna.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.COMMAND, null);
            yukiOnna.setPersistenceRequired();
            yukiOnna.addTag("yuki_natural");
            level.addFreshEntity(yukiOnna);
            despawnNearbyHostileMobs(level, spawnPos);
            return true;
        }

        return false;
    }

    private static void despawnNearbyHostileMobs(ServerLevel level, BlockPos center) {
        AABB box = new AABB(center).inflate(MOB_DESPAWN_RADIUS);
        List<Entity> nearby = level.getEntities((Entity) null, box, entity ->
                entity instanceof Zombie ||
                entity instanceof Skeleton ||
                entity instanceof Stray ||
                entity instanceof Creeper ||
                entity instanceof Spider ||
                entity instanceof CaveSpider ||
                entity instanceof Witch ||
                entity instanceof EnderMan ||
                entity instanceof Drowned ||
                entity instanceof Husk ||
                entity instanceof Slime ||
                entity instanceof Phantom ||
                entity instanceof ZombieVillager
        );
        nearby.forEach(Entity::discard);
    }
}