package com.shioh.sengoku.spawn;

import com.shioh.sengoku.registry.ModEntities;
import com.shioh.sengoku.entity.GoryoEntity;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * Server-side periodic spawner for Goryo entities. This bypasses biome JSON and
 * spawns Goryo near players in the Nether on blocks listed in the
 * `sengoku:goryo_spawnable_on` block tag.
 */
public class GoryoSpawner {
    private static final int INTERVAL_TICKS = 840;
    private static final int MIN_SPAWNS_PER_INTERVAL = 1; 
    private static final int MAX_SPAWNS_PER_INTERVAL = 1; 
    private static final int MIN_RADIUS = 16;
    private static final int MAX_RADIUS = 48;
    private static final int MAX_PER_CHUNK = 1;
    private static final int MAX_NEARBY_AROUND_PLAYER = 3;
    private static final int MAX_SCAN_ABOVE_PLAYER = 24;
    private static final int MAX_SCAN_BELOW_PLAYER = 48;

    private static final TagKey<Block> ALLOWED_GROUND = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sengoku", "goryo_spawnable_on"));

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    if (level.dimension() != Level.NETHER) return;
                    ServerLevel slevel = (ServerLevel) level;
                    long time = slevel.getGameTime();
                    if (time % INTERVAL_TICKS != 0) return;

                    // Only consider players who have the required advancement
                    List<ServerPlayer> players = slevel.getPlayers(p -> playerHasGoryoAdvancement(p));
                    if (players.isEmpty()) return;
                    RandomSource rand = slevel.getRandom();
                    // Choose a random player to spawn near
                    ServerPlayer player = players.get(rand.nextInt(players.size()));

                    AABB nearbyBox = player.getBoundingBox().inflate(MAX_RADIUS, 48.0D, MAX_RADIUS);
                    int nearbyExisting = slevel.getEntitiesOfClass(GoryoEntity.class, nearbyBox, e -> true).size();
                    if (nearbyExisting >= MAX_NEARBY_AROUND_PLAYER) return;

                    int attempts = MIN_SPAWNS_PER_INTERVAL + rand.nextInt(MAX_SPAWNS_PER_INTERVAL - MIN_SPAWNS_PER_INTERVAL + 1);
                    for (int attempt = 0; attempt < attempts; attempt++) {
                        double angle = rand.nextDouble() * Math.PI * 2.0;
                        double radius = MIN_RADIUS + rand.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
                        int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
                        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
                        int topY = slevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                        int scanTopY = Math.min(topY, player.blockPosition().getY() + MAX_SCAN_ABOVE_PLAYER);
                        int minY = Math.max(slevel.getMinBuildHeight() + 1, player.blockPosition().getY() - MAX_SCAN_BELOW_PLAYER);

                        boolean spawned = false;
                        // Search near the player's elevation rather than from the Nether ceiling.
                        for (int y = scanTopY; y > minY; y--) {
                            BlockPos groundPos = new BlockPos(x, y, z);
                            if (slevel.getBlockState(groundPos).is(ALLOWED_GROUND)) {
                                BlockPos spawnPos = groundPos.above();
                                boolean posClear = slevel.getBlockState(spawnPos).isAir() && slevel.getBlockState(spawnPos.above()).isAir();
                                if (!posClear) {
                                
                                    // continue searching lower
                                    continue;
                                }
                                
                                // Check per-chunk cap
                                int chunkX = spawnPos.getX() >> 4;
                                int chunkZ = spawnPos.getZ() >> 4;
                                AABB chunkBox = new AABB(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);
                                int existing = slevel.getEntitiesOfClass(GoryoEntity.class, chunkBox, e -> true).size();
                                if (existing >= MAX_PER_CHUNK) {
                                    break;
                                }
                                Entity ent = ModEntities.GORYO.create(slevel);
                                if (!(ent instanceof net.minecraft.world.entity.Mob)) {
                                    break;
                                }
                                net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) ent;
                                mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, (float) (rand.nextDouble() * 360.0), 0.0F);
                                mob.finalizeSpawn(slevel, slevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null);
                                slevel.addFreshEntity(mob);
                                
                                spawned = true;
                                break;
                            }
                        }

                        if (spawned) break;
                    }
                } catch (Throwable ignored) {}
            });
        });
    }

    private static boolean playerHasGoryoAdvancement(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
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
}
