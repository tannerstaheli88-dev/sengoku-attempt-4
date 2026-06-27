package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import com.shioh.sengoku.registry.ParticleRegistry;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds phase 2 special events when Ender Dragon is at <= 50% health.
 * Events include: ambient cherry petal particles, lightning strikes, and occasional Umi Nyobo spawns.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonPhase2EventsMixin extends Mob {

    @Unique
    private int sengoku$phase2EventTicker = 0;

    protected EnderDragonPhase2EventsMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"), require = 1)
    private void sengoku$phase2TickEvents(CallbackInfo ci) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!this.isAlive()) return;
        
        // Check dimension - use resource location comparison
        try {
            var dimKey = serverLevel.dimension();
            var endKey = net.minecraft.world.level.Level.END;
            if (!dimKey.equals(endKey)) {
                return;
            }
        } catch (Throwable ignored) {
            return;
        }

        // Check if dragon is in phase 2 (health <= 50%)
        float health = this.getHealth();
        float maxHealth = this.getMaxHealth();
        if (health > maxHealth * 0.5f) return; // Not in phase 2

        sengoku$phase2EventTicker++;

        // Petal particles every tick (~3-5 petals per second, around player)
        if (sengoku$phase2EventTicker % 4 == 0) {
            sengoku$spawnCherryPetals(serverLevel);
        }

        // Lightning strikes every 60-100 ticks (~1 strike every 3-5 seconds, around End center)
        if (sengoku$phase2EventTicker % Mth.nextInt(serverLevel.random, 60, 100) == 0) {
            sengoku$spawnLightningStrikes(serverLevel);
        }

        // Spawn Umi Nyobo occasionally every 200-300 ticks (~10-15 seconds, around End center)
        if (sengoku$phase2EventTicker % Mth.nextInt(serverLevel.random, 200, 300) == 0) {
            sengoku$spawnUmiNyobo(serverLevel);
        }
    }

    @Unique
    private void sengoku$spawnCherryPetals(ServerLevel serverLevel) {
        // Send cherry leaf particles around ALL players in the End (like weather)
        // This creates an ambient mist effect during phase 2
        
        var players = serverLevel.players();
        if (players.isEmpty()) return;
        
        for (var player : players) {
            double playerX = player.getX();
            double playerY = player.getY() + 11.5D;
            double playerZ = player.getZ();

            // Spawn 3-5 petals in a cloud around the player
            int petalCount = Mth.nextInt(serverLevel.random, 5, 8);
            for (int i = 0; i < petalCount; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5D) * 62.0D;
                double offsetY = (serverLevel.random.nextDouble() - 0.5D) * 8.0D;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5D) * 62.0D;

double velocityX = (serverLevel.random.nextDouble() - 0.5D) * 1.25D;
double velocityY = -0.08D - serverLevel.random.nextDouble() * 1.08D; // fall faster
double velocityZ = (serverLevel.random.nextDouble() - 0.5D) * 1.25D;

                // Use cherry leaves particle if available, otherwise use white ash
                // Attempt to spawn both the custom flowing leaves and vanilla cherry leaves.
                // Each is wrapped so one failing won't prevent the other.
                try {
                    serverLevel.sendParticles(
                        ParticleRegistry.FLOWING_LEAVES,
                        playerX + offsetX, playerY + offsetY, playerZ + offsetZ,
                        1, velocityX, velocityY, velocityZ, 0.08D
                    );
                } catch (Exception e) {
                    try {
                        serverLevel.sendParticles(
                            ParticleTypes.WHITE_ASH,
                            playerX + offsetX, playerY + offsetY, playerZ + offsetZ,
                            1, velocityX, velocityY, velocityZ, 0.08D
                        );
                    } catch (Exception ignored) {}
                }

                try {
                    serverLevel.sendParticles(
                        ParticleTypes.CHERRY_LEAVES,
                        playerX + offsetX, playerY + offsetY, playerZ + offsetZ,
                        1, velocityX, velocityY, velocityZ, 0.08D
                    );
                } catch (Exception e) {
                    try {
                        serverLevel.sendParticles(
                            ParticleTypes.WHITE_ASH,
                            playerX + offsetX, playerY + offsetY, playerZ + offsetZ,
                            1, velocityX, velocityY, velocityZ, 0.08D
                        );
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Unique
    private void sengoku$spawnLightningStrikes(ServerLevel serverLevel) {
        // Spawn lightning around the End center (0, 100, 0), not the dragon
        double centerX = 0.0D;
        double centerZ = 0.0D;

        // Spawn 1-3 lightning bolts in a radius around End center
        int strikeCount = Mth.nextInt(serverLevel.random, 1, 3);
        
        for (int i = 0; i < strikeCount; i++) {
            // Random angle and distance
            double angle = serverLevel.random.nextDouble() * Math.PI * 2.0D;
            double distance = 50.0D + serverLevel.random.nextDouble() * 50.0D;
            double strikeX = centerX + Math.cos(angle) * distance;
            double strikeZ = centerZ + Math.sin(angle) * distance;

            // Find a safe height
            BlockPos groundPos = serverLevel.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(Mth.floor(strikeX), 120, Mth.floor(strikeZ))
            );
            double strikeY = Math.max(groundPos.getY() + 1.0D, 64.0D);

            // Spawn lightning bolt
            try {
                LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                bolt.setPos(strikeX, strikeY, strikeZ);
                serverLevel.addFreshEntity(bolt);
            } catch (Exception ignored) {
                // Silently fail if lightning creation fails
            }
        }
    }

    @Unique
    private void sengoku$spawnUmiNyobo(ServerLevel serverLevel) {
        // 50% chance to not spawn on this trigger
        if (serverLevel.random.nextBoolean()) {
            return;
        }

        // Spawn around the End center (0, 64-120, 0), not the dragon
        double centerX = 0.0D;
        double centerZ = 0.0D;

        // Spawn near End center but at ground level
        double angle = serverLevel.random.nextDouble() * Math.PI * 2.0D;
        double distance = 20.0D + serverLevel.random.nextDouble() * 40.0D;
        double spawnX = centerX + Math.cos(angle) * distance;
        double spawnZ = centerZ + Math.sin(angle) * distance;

        BlockPos groundPos = serverLevel.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING,
            new BlockPos(Mth.floor(spawnX), 120, Mth.floor(spawnZ))
        );
        double spawnY = groundPos.getY();

        try {
            var nyobo = ModEntities.UMI_NYOBO.create(serverLevel);
            if (nyobo != null) {
                nyobo.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);
                // Use current difficulty for creative mode compatibility
                var difficulty = serverLevel.getCurrentDifficultyAt(groundPos);
                nyobo.finalizeSpawn(serverLevel, difficulty, MobSpawnType.EVENT, null);
                serverLevel.addFreshEntity(nyobo);
            }
        } catch (Exception ignored) {
            // Silently fail if entity creation fails
        }
    }
}
