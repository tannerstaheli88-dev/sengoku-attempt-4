package com.shioh.sengoku.commands;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.system.PostureHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Toggleable debug overlay/chat output for Sengoku internals.
 * Usage: /sengokudebug
 */
public class SengokuDebugCommand {
    private static final Set<java.util.UUID> ENABLED = ConcurrentHashMap.newKeySet();
    private static boolean tickRegistered = false;
    // network payloads are sent as CustomPacketPayload records

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sengokudebug")
            .requires(src -> src.hasPermission(0))
            .executes(ctx -> {
                CommandSourceStack src = ctx.getSource();
                if (!(src.getEntity() instanceof ServerPlayer player)) {
                    src.sendFailure(Component.literal("This command must be run by a player"));
                    return 0;
                }
                boolean enabled = toggle(player);
                src.sendSuccess(() -> Component.literal("Sengoku debug " + (enabled ? "enabled" : "disabled")), true);
                return enabled ? 1 : 0;
            })
        );

        // Register periodic tick once
        if (!tickRegistered) {
            tickRegistered = true;
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                // send updates every 20 ticks
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    try {
                        if (!ENABLED.contains(player.getUUID())) continue;
                        if (player.tickCount % 5 != 0) continue;
                        sendDebugForPlayer(player);
                    } catch (Throwable ignored) {}
                }
            });
        }
    }

    private static boolean toggle(ServerPlayer player) {
        if (ENABLED.contains(player.getUUID())) {
            ENABLED.remove(player.getUUID());
            try { ServerPlayNetworking.send(player, new com.shioh.sengoku.network.DebugTogglePayload(false)); } catch (Throwable ignored) {}
            return false;
        } else {
            ENABLED.add(player.getUUID());
            try { ServerPlayNetworking.send(player, new com.shioh.sengoku.network.DebugTogglePayload(true)); } catch (Throwable ignored) {}
            return true;
        }
    }

    private static void sendDebugForPlayer(ServerPlayer player) {
        try {
            var level = player.serverLevel();
            if (level == null) return;
            BlockPos pos = player.blockPosition();
            SengokuConfig cfg = SengokuConfig.getInstance();


            // Build debug lines and send to client HUD
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add("§6=== Sengoku Debug ===");

            // 1) Spawn Eligibility (approximate)
            StringBuilder spawnLine = new StringBuilder();
            long dayTime = level.getDayTime() % 24000L;
            boolean isDay = dayTime < 13000L;
            boolean isNight = dayTime >= 13000L && dayTime <= 23000L;
            boolean doPatrolSpawning = level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING);

            boolean yukiBiome = level.getBiome(pos).is(TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("sengoku", "yuki_onna_spawnable")));
            boolean yukiRaining = level.isRaining() || level.isThundering();
            double weatherMultiplier = level.isThundering() ? 1.0D : (level.isRaining() ? 0.5D : 0.0D);
            double yukiChance = Math.min(cfg.yukiOnnaSpawnChance * weatherMultiplier, 1.0D);
            boolean yukiReady = com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.isReadyToAttemptSpawn(level);
            long yukiTicksUntil = com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.getTicksUntilNextAttempt(level);
            double yukiSeconds = yukiTicksUntil <= 0 ? 0.0 : (yukiTicksUntil / 20.0);
            boolean yukiEligible = cfg.yukiOnnaEnabled && yukiBiome && yukiRaining && yukiReady;
            spawnLine.append(String.format("YukiOnna: %s (chance=%.0f%%, weather=%.0f%%, ready=%s, ticks=%d, %.1fs) | ", yukiEligible, yukiChance * 100.0, weatherMultiplier * 100.0, yukiReady, yukiTicksUntil, yukiSeconds));

            boolean kuchiEligible = cfg.kuchisakaOnnaSpawnChance > 0 && isNight && doPatrolSpawning;
            spawnLine.append(String.format("KuchisakeOnna: %s (%.0f%%) | ", kuchiEligible, cfg.kuchisakaOnnaSpawnChance * 100.0));

            boolean oniBruteUnlocked = playerHasObtainArmorAdvancement(player);
            boolean nightZombieEligible = isNight && cfg.nightZombiePatrolSpawnChance > 0 && doPatrolSpawning;
            spawnLine.append(String.format("NightOniPatrol: %s (%.0f%%, OniBruteLeader=%s)", nightZombieEligible, cfg.nightZombiePatrolSpawnChance * 100.0, oniBruteUnlocked));
            lines.add(spawnLine.toString());

            StringBuilder more = new StringBuilder();
            boolean nearRespawnBed = isNearAnyRespawnBed(level, pos);
            boolean banditEligible = cfg.banditPatrolsEnabled && doPatrolSpawning && isDay;
            boolean roninUnlocked = hasSmeltIronUnlockNearby(level, pos, player);
            boolean roninIronWeapons = hasMineDiamondUnlockNearby(level, pos, player);
            more.append(String.format("BanditPatrol: %s (chance=%.0f%%, nearBed=%s) | ", banditEligible, nearRespawnBed ? 40.0D : 100.0D, nearRespawnBed));
            more.append(String.format("RoninUnlocked: %s | TamahaganeWeapons: %s | ", roninUnlocked, roninIronWeapons));

            boolean clanEligible = cfg.clanPatrolsEnabled && doPatrolSpawning && isDay && level.canSeeSky(pos) && !nearRespawnBed;
            more.append(String.format("ClanPatrol: %s (%.0f%%, nearBed=%s) | ", clanEligible, cfg.clanPatrolSpawnChance * 100.0, nearRespawnBed));

            TagKey<Block> shinobiGroundTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sengoku", "shinobi_spawnable_on"));
            boolean shinobiGroundOk = level.getBlockState(pos.below()).is(shinobiGroundTag);
            boolean shinobiEligible = cfg.shinobiPatrolsEnabled && doPatrolSpawning && isNight && shinobiGroundOk;
            more.append(String.format("ShinobiPatrol: %s (%.0f%%) | ", shinobiEligible, cfg.shinobiPatrolSpawnChance * 100.0));

            boolean omukadeUnlocked = hasNearbyAdvancement(level, pos, player, ResourceLocation.fromNamespaceAndPath("minecraft", "story/obtain_armor"));
            boolean omukadeEligible = omukadeUnlocked;
            more.append(String.format("Omukade: %s (unlock=%s) | ", omukadeEligible, omukadeUnlocked));

            boolean ravagerAdvUnlocked = playerHasObtainArmorAdvancement(player);
            boolean ravagerBeachNearby = ravagerAdvUnlocked && hasNearbyRavagerBeachCandidate(level, player);
            more.append(String.format("Ushi Oni: %s | BeachNearby: %s", ravagerAdvUnlocked, ravagerBeachNearby));
            lines.add(more.toString());

            // 2) Music state is rendered client-side from the actual client music controller

            // 3) Parry Type Detection
            String parry = "Not blocking";
            try {
                boolean blocking = player.isBlocking();
                if (blocking) {
                    net.minecraft.world.item.ItemStack blockingItem = player.getUseItem();
                    if (blockingItem == null || blockingItem.isEmpty()) blockingItem = player.getMainHandItem();
                    final int BASE_PERFECT_WINDOW = 3;
                    int perfectWindow = PostureHandler.getPerfectParryWindowTicks(blockingItem, BASE_PERFECT_WINDOW);
                    int ticksUsing = 0;
                    try { ticksUsing = player.getTicksUsingItem(); } catch (Throwable ignored) {}
                    final int PARTIAL_DURATION = 12;
                    if (ticksUsing > 0 && ticksUsing <= perfectWindow) parry = "Perfect Parry";
                    else if (ticksUsing > 0 && ticksUsing <= perfectWindow + PARTIAL_DURATION) parry = "Partial Parry";
                    else parry = "Regular Block";
                }
            } catch (Throwable ignored) {}
            lines.add("Parry: " + parry);

            // send packet to client with the lines
            try { ServerPlayNetworking.send(player, new com.shioh.sengoku.network.DebugDataPayload(lines)); } catch (Throwable ignored) {}

        } catch (Throwable ignored) {}
    }

    private static boolean hasNearbyRavagerBeachCandidate(net.minecraft.server.level.ServerLevel level, ServerPlayer player) {
        for (int angle = 0; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            for (int dist = 24; dist <= 128; dist += 16) {
                int x = player.blockPosition().getX() + (int) Math.floor(Math.cos(radians) * dist);
                int z = player.blockPosition().getZ() + (int) Math.floor(Math.sin(radians) * dist);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos candidate = new BlockPos(x, y, z);
                if (!level.isLoaded(candidate)) continue;
                if (!level.getBiome(candidate).is(BiomeTags.IS_BEACH)) continue;
                BlockPos below = candidate.below();
                BlockState ground = level.getBlockState(below);
                if (!ground.isSolidRender(level, below)) continue;
                if (!level.getBlockState(candidate).isAir()) continue;
                if (!level.getBlockState(candidate.above()).isAir()) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean hasSmeltIronUnlockNearby(net.minecraft.server.level.ServerLevel level, BlockPos pos, ServerPlayer nearPlayer) {
        return hasNearbyAdvancement(level, pos, nearPlayer, ResourceLocation.fromNamespaceAndPath("minecraft", "story/smelt_iron"));
    }

    private static boolean hasMineDiamondUnlockNearby(net.minecraft.server.level.ServerLevel level, BlockPos pos, ServerPlayer nearPlayer) {
        return hasNearbyAdvancement(level, pos, nearPlayer, ResourceLocation.fromNamespaceAndPath("minecraft", "story/mine_diamond"));
    }

    private static boolean playerHasObtainArmorAdvancement(ServerPlayer player) {
        return playerHasAdvancement(player, ResourceLocation.fromNamespaceAndPath("minecraft", "story/obtain_armor"));
    }

    private static boolean hasNearbyAdvancement(net.minecraft.server.level.ServerLevel level, BlockPos pos, ServerPlayer nearPlayer, ResourceLocation advancementId) {
        try {
            if (nearPlayer != null && playerHasAdvancement(nearPlayer, advancementId)) {
                return true;
            }
            for (ServerPlayer p : level.players()) {
                try {
                    if (p.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 64.0 * 64.0) {
                        if (playerHasAdvancement(p, advancementId)) {
                            return true;
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean isNearAnyRespawnBed(net.minecraft.server.level.ServerLevel level, BlockPos spawnPos) {
        try {
            int spawnChunkX = spawnPos.getX() >> 4;
            int spawnChunkZ = spawnPos.getZ() >> 4;
            for (ServerPlayer sp : level.players()) {
                try {
                    Object resp = null;
                    try {
                        resp = sp.getRespawnPosition();
                    } catch (Throwable t) {
                        try {
                            java.util.Optional<?> opt = (java.util.Optional<?>) (Object) sp.getClass().getMethod("getRespawnPosition").invoke(sp);
                            if (opt != null && opt.isPresent()) resp = opt.get();
                        } catch (Throwable ignored) {}
                    }
                    if (resp instanceof BlockPos bedPos) {
                        int dx = Math.abs((bedPos.getX() >> 4) - spawnChunkX);
                        int dz = Math.abs((bedPos.getZ() >> 4) - spawnChunkZ);
                        if (Math.max(dx, dz) <= 5) return true;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean playerHasAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        try {
            var advancements = player.getAdvancements();
            var progress = advancements.getOrStartProgress(player.server.getAdvancements().get(advancementId));
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
