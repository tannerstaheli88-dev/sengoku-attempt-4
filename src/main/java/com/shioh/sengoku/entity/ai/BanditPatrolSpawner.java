package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.entity.RoninEntity;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom spawner for bandit patrols.
 * Spawns groups of bandits and ronin during the day.
 */
public class BanditPatrolSpawner implements CustomSpawner {
    public static final BanditPatrolSpawner INSTANCE = new BanditPatrolSpawner();
    private int nextTick;
    private final List<SpawnRecord> recentSpawns = new ArrayList<>();
    private static final int MIN_SPAWN_DISTANCE = 200; // Minimum blocks between spawns
    private static final long SPAWN_RECORD_LIFETIME = 6000; // 5 minutes in ticks
    private static final int RESPAWN_PROXIMITY_CHUNK_MARGIN = 5; // Same radius behavior as clan patrols
    private static final float RESPAWN_PROXIMITY_SPAWN_MULTIPLIER = 0.4f; // 60% reduction near beds/respawn points
    
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
        if (!spawnHostiles) {
            return 0;
        }
        
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        RandomSource random = level.random;
        --this.nextTick;
        
        if (this.nextTick > 0) {
            return 0;
        }
        
        // Reset timer - use config value for check interval
        SengokuConfig config = SengokuConfig.getInstance();
        this.nextTick = config.banditPatrolCheckInterval + random.nextInt(Math.max(1, config.banditPatrolCheckInterval));
        
        // Clean up old spawn records
        long currentTime = level.getGameTime();
        recentSpawns.removeIf(record -> currentTime - record.time > SPAWN_RECORD_LIFETIME);
        
        // Get day time - only spawn during day
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime >= 13000L || dayTime < 0L) {
            return 0; // Only spawn during day (0-13000)
        }

        // Respect enabled flag; if disabled, skip spawning
        if (!config.banditPatrolsEnabled) {
            return 0;
        }

        int playerCount = level.players().size();
        if (playerCount < 1) {
            return 0;
        }

        // Pick a random player to spawn near
        var player = level.players().get(random.nextInt(playerCount));
        // Only skip spectators, allow creative mode
        if (player.isSpectator()) {
            return 0;
        }

        // Find spawn position 24-48 blocks away from player
        int spawnDistance = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable().move(spawnDistance, 0, spawnDistance);
        
        // Adjust to surface height (ignore leaves so we don't spawn on tree tops)
        spawnPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()));

        // Near player respawn points, keep spawning possible but reduce natural spawn rate by 60%.
        if (isNearAnyPlayerRespawn(level, spawnPos) && random.nextFloat() > RESPAWN_PROXIMITY_SPAWN_MULTIPLIER) {
            return 0;
        }
        
        // Check if position is valid
        if (!level.isLoaded(spawnPos)) {
            return 0;
        }
        
        // Check if too close to recent spawns
        BlockPos immutablePos = spawnPos.immutable();
        for (SpawnRecord record : recentSpawns) {
            if (record.pos.distSqr(immutablePos) < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) {
                return 0; // Too close to a recent spawn
            }
        }
        
        // Check block at spawn position - must be air or replaceable (grass, flowers, etc.)
        BlockState spawnBlock = level.getBlockState(spawnPos);
        if (!spawnBlock.isAir() && !spawnBlock.canBeReplaced()) {
            return 0;
        }
        
        // Check block below - must be a valid spawn block
        BlockState blockBelow = level.getBlockState(spawnPos.below());
        if (!isValidSpawnBlock(blockBelow)) {
            return 0;
        }
        
        // Don't spawn near artificial light sources (torches, lanterns, etc.)
        if (level.getBrightness(LightLayer.BLOCK, spawnPos) > 0) {
            return 0;
        }

        // Spawn the patrol; pass the chosen player so we can tailor patrol size
        int spawned = this.spawnPatrol(level, spawnPos, random, player);
        if (spawned > 0) {
            recentSpawns.add(new SpawnRecord(immutablePos, level.getGameTime()));
        }
        return spawned;
    }
    
    /**
     * Force spawn a patrol at the given position (for testing/commands).
     */
    public int forceSpawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.spawnPatrol(level, pos, random, null);
    }
    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random, net.minecraft.world.entity.player.Player nearPlayer) {
        // 5% chance for a solo elite ronin patrol once the player has diamond armor
        if (hasCoverMeUnlockNearby(level, pos, nearPlayer) && random.nextFloat() < 0.05f) {
            return spawnEliteRoninPatrol(level, pos, random);
        }

        // Patrol size: 1-3 members by default
        int patrolSize = 1 + random.nextInt(3);

        // Ronin only join patrols after the player has smelted iron.
        final boolean roninUnlocked = hasSmeltIronUnlockNearby(level, pos, nearPlayer);
        // Once mine_diamond is unlocked, ronin use iron swords in patrols.
        final boolean roninUseIronWeapons = hasMineDiamondUnlockNearby(level, pos, nearPlayer);

        // If the nearby player has the 'shiny_gear' advancement, or any nearby player does, increase patrol size
        try {
            boolean nearbyHasAdv = false;
            if (nearPlayer != null && playerHasCoverMeAdvancement(nearPlayer)) nearbyHasAdv = true;
            // Also check any player within 64 blocks of the spawn position
            if (!nearbyHasAdv) {
                for (var p : level.players()) {
                    try {
                        if (p.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 64.0 * 64.0) {
                            if (playerHasCoverMeAdvancement(p)) { nearbyHasAdv = true; break; }
                        }
                    } catch (Throwable ignored) {}
                }
            }
            if (nearbyHasAdv) patrolSize += 2; // increase by two members
        } catch (Throwable ignored) {}
        int spawned = 0;
        
        // 70% chance patrol leader is a ronin, 30% chance it's a bandit (only when unlocked)
        boolean leaderIsRonin = roninUnlocked && random.nextFloat() < 0.7f;
        
        for (int i = 0; i < patrolSize; i++) {
            BlockPos spawnPos = pos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
            spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());
            
            // Skip this slot if it's lit by artificial light (torches, lanterns, etc.)
            if (level.getBrightness(LightLayer.BLOCK, spawnPos) > 0) {
                continue;
            }
            
            // First member is the leader
            if (i == 0) {
                if (this.spawnPatrolLeader(level, spawnPos, random, leaderIsRonin, roninUseIronWeapons)) {
                    spawned++;
                }
            } else {
                // 60% bandit, 40% ronin for regular patrol members
                boolean spawnBandit = !roninUnlocked || random.nextFloat() < 0.6f;
                if (this.spawnPatrolMember(level, spawnPos, random, spawnBandit, roninUseIronWeapons)) {
                    spawned++;
                }
            }
        }
        
        return spawned;
    }

    private int spawnEliteRoninPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        var entity = ModEntities.RONIN.create(level);
        if (!(entity instanceof RoninEntity ronin)) return 0;
        ronin.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        ronin.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        ronin.sengoku$setElite(true);
        level.addFreshEntity(ronin);
        ronin.addTag("sengoku_patrol");
        return 1;
    }

    private boolean hasCoverMeUnlockNearby(ServerLevel level, BlockPos spawnPos, net.minecraft.world.entity.player.Player nearPlayer) {
        try {
            if (nearPlayer != null && playerHasCoverMeAdvancement(nearPlayer)) return true;
            for (var p : level.players()) {
                try {
                    if (p.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) <= 64.0 * 64.0) {
                        if (playerHasCoverMeAdvancement(p)) return true;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean isNearAnyPlayerRespawn(ServerLevel level, BlockPos spawnPos) {
        try {
            int spawnChunkX = spawnPos.getX() >> 4;
            int spawnChunkZ = spawnPos.getZ() >> 4;
            for (ServerPlayer sp : level.players()) {
                try {
                    Object resp = null;
                    try {
                        resp = sp.getRespawnPosition();
                    } catch (Throwable t) {
                        // Some mappings expose respawn as Optional - try that next.
                        try {
                            java.util.Optional<?> opt = (java.util.Optional<?>) (Object) sp.getClass().getMethod("getRespawnPosition").invoke(sp);
                            if (opt != null && opt.isPresent()) {
                                resp = opt.get();
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    if (resp instanceof BlockPos bedPos) {
                        int bedChunkX = bedPos.getX() >> 4;
                        int bedChunkZ = bedPos.getZ() >> 4;
                        int dx = Math.abs(spawnChunkX - bedChunkX);
                        int dz = Math.abs(spawnChunkZ - bedChunkZ);
                        if (Math.max(dx, dz) <= RESPAWN_PROXIMITY_CHUNK_MARGIN) {
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

    private boolean playerHasCoverMeAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return false;
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

    private boolean hasSmeltIronUnlockNearby(ServerLevel level, BlockPos spawnPos, net.minecraft.world.entity.player.Player nearPlayer) {
        try {
            if (nearPlayer != null && playerHasSmeltIronAdvancement(nearPlayer)) {
                return true;
            }

            for (var p : level.players()) {
                try {
                    if (p.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) <= 64.0 * 64.0) {
                        if (playerHasSmeltIronAdvancement(p)) {
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

    private boolean playerHasSmeltIronAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/smelt_iron");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean hasMineDiamondUnlockNearby(ServerLevel level, BlockPos spawnPos, net.minecraft.world.entity.player.Player nearPlayer) {
        try {
            if (nearPlayer != null && playerHasMineDiamondAdvancement(nearPlayer)) {
                return true;
            }

            for (var p : level.players()) {
                try {
                    if (p.distanceToSqr(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5) <= 64.0 * 64.0) {
                        if (playerHasMineDiamondAdvancement(p)) {
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

    private boolean playerHasMineDiamondAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return false;
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/mine_diamond");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean spawnPatrolLeader(ServerLevel level, BlockPos pos, RandomSource random, boolean isRonin, boolean roninUseIronWeapons) {
        // Spawn the leader entity
        EntityType<?> entityType = isRonin ? ModEntities.RONIN : ModEntities.BANDIT;
        var entity = entityType.create(level);
        
        if (!(entity instanceof net.minecraft.world.entity.Mob leader)) {
            return false;
        }

        leader.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);

        if (isRonin && roninUseIronWeapons) {
            leader.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        
        // No banner for bandit/ronin patrol leaders - they're outlaws, not an official clan
        
        // No horses for bandit/ronin patrols
        
        level.addFreshEntity(leader);
        // Mark entity as spawned by our patrol spawner so other systems can detect it
        leader.addTag("sengoku_patrol");
        return true;
    }

    private boolean spawnPatrolMember(ServerLevel level, BlockPos pos, RandomSource random, boolean isBandit, boolean roninUseIronWeapons) {
        EntityType<?> entityType = isBandit ? ModEntities.BANDIT : ModEntities.RONIN;
        var entity = entityType.create(level);
        
        if (!(entity instanceof net.minecraft.world.entity.Mob member)) {
            return false;
        }

        member.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        member.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);

        if (!isBandit && roninUseIronWeapons) {
            member.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        
        level.addFreshEntity(member);
        // Mark entity as spawned by our patrol spawner
        member.addTag("sengoku_patrol");
        return true;
    }
    
    /**
     * Check if the block is valid for patrol spawning.
     * Allows: dirt, grass_block, coarse_dirt, sand, gravel, rooted_dirt, podzol, snow, snow_block
     */
    private boolean isValidSpawnBlock(BlockState state) {
         return state.is(net.minecraft.world.level.block.Blocks.DIRT) ||
             state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) ||
             state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) ||
             state.is(net.minecraft.world.level.block.Blocks.FARMLAND) ||
             state.is(net.minecraft.world.level.block.Blocks.MYCELIUM) ||
             state.is(net.minecraft.world.level.block.Blocks.SAND) ||
             state.is(net.minecraft.world.level.block.Blocks.GRAVEL) ||
             state.is(net.minecraft.world.level.block.Blocks.ROOTED_DIRT) ||
             state.is(net.minecraft.world.level.block.Blocks.PODZOL) ||
             state.is(net.minecraft.world.level.block.Blocks.SNOW) ||
             state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK);
    }
}
