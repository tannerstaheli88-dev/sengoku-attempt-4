package com.shioh.sengoku.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class RavagerSurfaceSpawnHandler {
    private static final Random RANDOM = new Random();
    private static final int CHECK_INTERVAL = 2400; // Check every 2 minutes (2400 ticks)
    private static final double SPAWN_CHANCE = 0.60; // 10% chance when check happens
    private static final int MIN_PLAYER_DISTANCE = 24;
    private static final int MAX_PLAYER_DISTANCE = 128;
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL == 0) {
                server.getAllLevels().forEach(level -> {
                    if (level.dimension() == Level.OVERWORLD) {
                        trySpawnRavager(level);
                    }
                });
            }
        });
    }
    
    private static void trySpawnRavager(ServerLevel level) {
        if (RANDOM.nextDouble() > SPAWN_CHANCE) {
            return;
        }
        
        // Get a random player to spawn near
        if (level.players().isEmpty()) {
            return;
        }
        
        ServerPlayer randomPlayer = level.players().get(RANDOM.nextInt(level.players().size()));
        
        // Only skip spectators, allow creative mode
        if (randomPlayer.isSpectator()) {
            return;
        }

        if (!playerHasObtainArmorAdvancement(randomPlayer)) {
            return;
        }
        
        // Try to find a valid spawn position near the player
        for (int attempt = 0; attempt < 10; attempt++) {
            // Pick random position in range around player
            int offsetX = MIN_PLAYER_DISTANCE + RANDOM.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE);
            int offsetZ = MIN_PLAYER_DISTANCE + RANDOM.nextInt(MAX_PLAYER_DISTANCE - MIN_PLAYER_DISTANCE);
            if (RANDOM.nextBoolean()) offsetX = -offsetX;
            if (RANDOM.nextBoolean()) offsetZ = -offsetZ;
            
            int x = (int) randomPlayer.getX() + offsetX;
            int z = (int) randomPlayer.getZ() + offsetZ;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            
            BlockPos spawnPos = new BlockPos(x, y, z);
            
            // Check if position is valid beach biome
            if (isValidBeachSpawn(level, spawnPos)) {
                spawnRavager(level, spawnPos);
                return;
            }
        }
    }
    
    private static boolean isValidBeachSpawn(ServerLevel level, BlockPos pos) {
        // Check if biome is a beach
        if (!level.getBiome(pos).is(BiomeTags.IS_BEACH)) {
            return false;
        }
        
        // Check if the position has valid spawning conditions
        BlockPos groundPos = pos.below();
        return level.getBlockState(groundPos).isSolidRender(level, groundPos)
            && level.getBlockState(pos).isAir()
            && level.getBlockState(pos.above()).isAir();
    }
    
    private static void spawnRavager(ServerLevel level, BlockPos pos) {
        Ravager ravager = EntityType.RAVAGER.create(level);
        if (ravager != null) {
            ravager.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                RANDOM.nextFloat() * 360.0F, 0.0F);
            ravager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), 
                MobSpawnType.NATURAL, null);
            level.addFreshEntity(ravager);
        }
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
