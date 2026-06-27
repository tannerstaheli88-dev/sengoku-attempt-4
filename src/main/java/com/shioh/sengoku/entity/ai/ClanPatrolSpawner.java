package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.entity.PatrolHorseAccess;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom spawner for clan patrols (Takeda, Kobayakawa, Satomi).
 * Spawns patrols of ashigaru and samurai with mounted leaders.
 * Has a chance to spawn rival clans nearby for immersive warfare.
 */
public class ClanPatrolSpawner implements CustomSpawner {
    public static final ClanPatrolSpawner INSTANCE = new ClanPatrolSpawner();
    private int nextTick;
    private final List<ClanSpawnRecord> recentClanSpawns = new ArrayList<>();
    private static final long CLAN_SPAWN_RECORD_LIFETIME = 1200; // 1 minute to allow rival spawns
    private static final int RIVAL_SPAWN_DISTANCE = 80; // Distance for rival clan spawn
    private static final float RIVAL_SPAWN_CHANCE = 0.20f; // 20% chance for rival spawn

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
        this.nextTick = config.clanPatrolCheckInterval + random.nextInt(Math.max(1, config.clanPatrolCheckInterval));
        
        // Clean up old clan spawn records
        long currentTime = level.getGameTime();
        recentClanSpawns.removeIf(record -> currentTime - record.time > CLAN_SPAWN_RECORD_LIFETIME);
        
        // Get day time - only spawn during day
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime >= 13000L || dayTime < 0L) {
            return 0; // Only spawn during day (0-13000)
        }

        // Respect enabled flag; if disabled, skip spawning
        if (!config.clanPatrolsEnabled) {
            return 0;
        }

        // Apply configurable spawn chance so patrols have a chance not to spawn
        if (random.nextDouble() >= config.clanPatrolSpawnChance) {
            return 0;
        }

        // No numeric spawn chance - enabled flag controls whether patrols spawn

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

        // Find spawn position 28-56 blocks away from player
        int spawnDistance = (28 + random.nextInt(28)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable().move(spawnDistance, 0, spawnDistance);
        
        // Adjust to surface height (avoid leaves)
        spawnPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ()));

        // Reject positions that are not exposed to the sky within this column
        if (!level.canSeeSky(spawnPos)) {
            return 0;
        }

        // Prevent clan patrols spawning within 5 chunks (~80 blocks) of any player's respawn bed.
        try {
            final int CHUNK_MARGIN = 5;
            int spawnChunkX = spawnPos.getX() >> 4;
            int spawnChunkZ = spawnPos.getZ() >> 4;
            for (ServerPlayer sp : level.players()) {
                try {
                    Object resp = null;
                    try {
                        resp = sp.getRespawnPosition();
                    } catch (Throwable t) {
                        // Some mappings expose respawn as Optional — try that next
                        try {
                            java.util.Optional<?> opt = (java.util.Optional<?>) (Object) sp.getClass().getMethod("getRespawnPosition").invoke(sp);
                            if (opt != null && opt.isPresent()) resp = opt.get();
                        } catch (Throwable ignored) {}
                    }

                    if (resp instanceof BlockPos bedPos) {
                        int bedChunkX = bedPos.getX() >> 4;
                        int bedChunkZ = bedPos.getZ() >> 4;
                        int dx = Math.abs(spawnChunkX - bedChunkX);
                        int dz = Math.abs(spawnChunkZ - bedChunkZ);
                        if (Math.max(dx, dz) <= CHUNK_MARGIN) {
                            return 0;
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        
        // Check if position is valid
        if (!level.isLoaded(spawnPos)) {
            return 0;
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

        // Spawn the patrol
        int spawned = this.spawnPatrol(level, spawnPos, random);
        
        if (spawned > 0) {
            Clan spawnedClan = getLastSpawnedClan();
            recentClanSpawns.add(new ClanSpawnRecord(spawnPos.immutable(), spawnedClan, level.getGameTime()));
            
            // 20% chance to spawn a rival clan nearby for warfare
            if (random.nextFloat() < RIVAL_SPAWN_CHANCE) {
                trySpawnRivalClan(level, spawnPos, spawnedClan, random);
            }
        }
        
        return spawned;
    }
    
    private Clan lastSpawnedClan = null;
    
    private Clan getLastSpawnedClan() {
        return lastSpawnedClan;
    }
    
    /**
     * Attempts to spawn a rival clan patrol nearby to create warfare.
     */
    private void trySpawnRivalClan(ServerLevel level, BlockPos originalPos, Clan originalClan, RandomSource random) {
        // Pick a rival clan (not the same as the original)
        Clan rivalClan;
        do {
            rivalClan = Clan.values()[random.nextInt(Clan.values().length)];
        } while (rivalClan == originalClan);
        
        // Try to find a spawn position for the rival
        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = RIVAL_SPAWN_DISTANCE + random.nextInt(30);
            int dz = RIVAL_SPAWN_DISTANCE + random.nextInt(30);
            if (random.nextBoolean()) dx = -dx;
            if (random.nextBoolean()) dz = -dz;
            
            BlockPos rivalPos = originalPos.offset(dx, 0, dz);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, rivalPos.getX(), rivalPos.getZ());
            rivalPos = new BlockPos(rivalPos.getX(), surfaceY, rivalPos.getZ());
            
            if (!level.isLoaded(rivalPos)) continue;

            // Ensure rival spawn start is exposed to sky; otherwise try another location
            if (!level.canSeeSky(rivalPos)) continue;

            BlockState spawnBlock = level.getBlockState(rivalPos);
            if (!spawnBlock.isAir() && !spawnBlock.canBeReplaced()) continue;

            BlockState blockBelow = level.getBlockState(rivalPos.below());
            if (!isValidSpawnBlock(blockBelow)) continue;
            
            // Spawn the rival patrol
            int rivalSize = 3 + random.nextInt(4);
            for (int i = 0; i < rivalSize; i++) {
                BlockPos spawnPos = rivalPos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
                spawnPos = new BlockPos(spawnPos.getX(), y, spawnPos.getZ());

                // Skip individual member positions that are under cover
                if (!level.canSeeSky(spawnPos)) continue;
                
                if (i == 0) {
                    spawnPatrolLeader(level, spawnPos, random, rivalClan);
                } else {
                    boolean isAshigaru = random.nextFloat() < 0.7f;
                    spawnPatrolMember(level, spawnPos, random, rivalClan, isAshigaru);
                }
            }
            return; // Successfully spawned rival
        }
    }
    
    /**
     * Force spawn a clan patrol at the given position (for testing/commands).
     */
    public int forceSpawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.spawnPatrol(level, pos, random);
    }

    private int spawnPatrol(ServerLevel level, BlockPos pos, RandomSource random) {
        // Choose a random clan
        Clan clan = Clan.values()[random.nextInt(Clan.values().length)];
        lastSpawnedClan = clan; // Track for rival spawn system
        
        // Patrol size: 3-6 members
        int patrolSize = 3 + random.nextInt(4);
        int spawned = 0;
        
        for (int i = 0; i < patrolSize; i++) {
            BlockPos spawnPos = pos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
            spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());

            // Skip members if their individual position isn't exposed to the sky
            if (!level.canSeeSky(spawnPos)) continue;
            
            // First member is the leader (always a samurai)
            if (i == 0) {
                if (this.spawnPatrolLeader(level, spawnPos, random, clan)) {
                    spawned++;
                }
            } else {
                // 70% ashigaru, 30% samurai for regular patrol members
                boolean isAshigaru = random.nextFloat() < 0.7f;
                if (this.spawnPatrolMember(level, spawnPos, random, clan, isAshigaru)) {
                    spawned++;
                }
            }
        }
        
        return spawned;
    }

    private boolean spawnPatrolLeader(ServerLevel level, BlockPos pos, RandomSource random, Clan clan) {
        // Leaders are always samurai
        EntityType<?> entityType = clan.getSamuraiType();
        var entity = entityType.create(level);
        
        if (!(entity instanceof net.minecraft.world.entity.Mob leader)) {
            return false;
        }

        leader.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        
        // Give leader clan's banner
        leader.setItemSlot(EquipmentSlot.HEAD, new ItemStack(clan.getBanner()));
        
        // Only spawn a horse for the leader if the spawn biome allows cavalry
        boolean canSpawnHorse = false;
        try {
            TagKey<Biome> calveryTag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("sengoku", "calvery_spawnable"));
            canSpawnHorse = level.getBiome(pos).is(calveryTag);
        } catch (Throwable ignored) {}

        // Spawn a horse for the leader when allowed
        Horse horse = null;
        if (canSpawnHorse) {
            horse = EntityType.HORSE.create(level);
        }

        if (horse != null) {
            horse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            horse.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
            horse.setTamed(true);
            horse.setOwnerUUID(leader.getUUID());
            
            // Boost horse movement speed for patrol
            horse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                .setBaseValue(0.35);
            
            // Add horse armor based on clan prestige
            // 80% chance for iron armor, 20% for diamond (these are prestigious clans)
            if (random.nextFloat() < 0.8f) {
                horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.IRON_HORSE_ARMOR));
            } else {
                horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
            }
            
            // Mark horse to indicate it should be despawned unless claimed.
            // Prefer mixin interface if available, otherwise add an entity tag as a fallback.
            if (horse instanceof PatrolHorseAccess pha) {
                pha.sengoku$setNeedsDespawn(true, level.getGameTime());
            } else {
                horse.addTag("sengoku_needs_despawn");
            }

            // Register with central manager for despawn handling
            com.shioh.sengoku.fabric.HorsePatrolManager.track(horse, level.getGameTime());
            sengokuFabric.LOGGER.info("Registered patrol horse with manager (leader)");

            // Spawn horse to world
            level.addFreshEntity(horse);
            leader.startRiding(horse);
        }
        
        level.addFreshEntity(leader);
        // Mark leader as spawned by our patrol system
        leader.addTag("sengoku_patrol");
        return true;
    }

    private boolean spawnPatrolMember(ServerLevel level, BlockPos pos, RandomSource random, Clan clan, boolean isAshigaru) {
        EntityType<?> entityType = isAshigaru ? clan.getAshigaruType() : clan.getSamuraiType();
        var entity = entityType.create(level);
        
        if (!(entity instanceof net.minecraft.world.entity.Mob member)) {
            return false;
        }

        member.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        member.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        
        // 40% chance to spawn mounted samurai (not ashigaru - they're foot soldiers)
        // Only allow member mounts in calvery spawnable biomes
        boolean canSpawnMemberHorse = false;
        try {
            TagKey<Biome> calveryTag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("sengoku", "calvery_spawnable"));
            canSpawnMemberHorse = level.getBiome(pos).is(calveryTag);
        } catch (Throwable ignored) {}

        if (!isAshigaru && canSpawnMemberHorse && random.nextFloat() < 0.4f) {
            Horse horse = EntityType.HORSE.create(level);
            if (horse != null) {
                horse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                horse.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
                horse.setTamed(true);
                horse.setOwnerUUID(member.getUUID());
                
                // Boost horse movement speed
                horse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .setBaseValue(0.35);
                
                // Add horse armor (60% iron, 30% leather, 10% diamond)
                float armorRoll = random.nextFloat();
                if (armorRoll < 0.6f) {
                    horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.IRON_HORSE_ARMOR));
                } else if (armorRoll < 0.9f) {
                    horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.LEATHER_HORSE_ARMOR));
                } else {
                    horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
                }
                
                // Mark horse to indicate it should be despawned unless claimed.

                if (horse instanceof PatrolHorseAccess pha) {
                    pha.sengoku$setNeedsDespawn(true, level.getGameTime());
                } else {
                    horse.addTag("sengoku_needs_despawn");
                }

                // Register with central manager for despawn handling
                com.shioh.sengoku.fabric.HorsePatrolManager.track(horse, level.getGameTime());
                sengokuFabric.LOGGER.info("Registered patrol horse with manager (member)");

                // Spawn horse to world
                level.addFreshEntity(horse);
                
                member.startRiding(horse);
            }
        }
        
        level.addFreshEntity(member);
        // Mark member as spawned by our patrol system
        member.addTag("sengoku_patrol");
        return true;
    }
    
    /**
     * Record of a recent clan patrol spawn for rival spawn tracking.
     */
    private static class ClanSpawnRecord {
        final BlockPos pos;
        final Clan clan;
        final long time;
        
        ClanSpawnRecord(BlockPos pos, Clan clan, long time) {
            this.pos = pos;
            this.clan = clan;
            this.time = time;
        }
    }
    
    /**
     * Enum representing the three clans with their entity types and banners.
     */
    private enum Clan {
        TAKEDA(ModEntities.TAKEDA_ASHIGARU, ModEntities.TAKEDA_SAMURAI, Items.RED_BANNER),
        KOBAYAKAWA(ModEntities.KOBAYAKAWA_ASHIGARU, ModEntities.KOBAYAKAWA_SAMURAI, Items.YELLOW_BANNER),
        SATOMI(ModEntities.SATOMI_ASHIGARU, ModEntities.SATOMI_SAMURAI, Items.BLUE_BANNER);
        
        private final EntityType<?> ashigaruType;
        private final EntityType<?> samuraiType;
        private final net.minecraft.world.item.Item banner;
        
        Clan(EntityType<?> ashigaruType, EntityType<?> samuraiType, net.minecraft.world.item.Item banner) {
            this.ashigaruType = ashigaruType;
            this.samuraiType = samuraiType;
            this.banner = banner;
        }
        
        public EntityType<?> getAshigaruType() {
            return ashigaruType;
        }
        
        public EntityType<?> getSamuraiType() {
            return samuraiType;
        }
        
        public net.minecraft.world.item.Item getBanner() {
            return banner;
        }
    }
    
    /**
     * Check if the block is valid for patrol spawning.
     * Allows: dirt, grass_block, coarse_dirt, sand, gravel, rooted_dirt, podzol, snow, snow_block
     */
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
