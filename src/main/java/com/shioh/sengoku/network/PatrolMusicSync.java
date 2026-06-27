package com.shioh.sengoku.network;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.structure.Structure;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class PatrolMusicSync {
    private static final TagKey<EntityType<?>> RONIN_TAG =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.parse("minecraft:initiate_ronin_music"));
    private static final TagKey<Structure> CASTLE_TAG =
            TagKey.create(Registries.STRUCTURE,
                    ResourceLocation.parse("minecraft:castle_music"));
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final Map<UUID, Integer> AGGRO_HOLD_TICKS = new HashMap<>();
    private static final int AGGRO_HOLD_DURATION = 60; // 3 seconds grace window
    private static int tickCounter = 0;
    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;
        for (ServerPlayer player : level.players()) {
            int aggroCount = 0;
            for (Mob mob : level.getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(96.0D))) {
                if (aggroCount >= 1) break;
                if (mob.isRemoved()) continue;
                if (!mob.getType().is(RONIN_TAG)) continue;
                if (mob.getTarget() == player
                        || mob.getLastHurtByMob() == player) {
                    aggroCount++;
                }
            }
            boolean detectedActive = aggroCount >= 1;
            // Hold the active flag briefly after aggro drops so short
            // line-of-sight breaks don't restart the track
            int holdTicks = AGGRO_HOLD_TICKS.getOrDefault(player.getUUID(), 0);
            if (detectedActive) {
                holdTicks = AGGRO_HOLD_DURATION;
            } else if (holdTicks > 0) {
                holdTicks -= 20; // subtract one check interval (20 ticks)
            }
            AGGRO_HOLD_TICKS.put(player.getUUID(), Math.max(0, holdTicks));
            boolean active = detectedActive || holdTicks > 0;
            // Block patrol music if inside a castle
            boolean insideCastle =
                    level.structureManager()
                         .getStructureWithPieceAt(player.blockPosition(), CASTLE_TAG)
                         .isValid();
            if (insideCastle) {
                active = false;
            }
            Boolean last = LAST_SENT.get(player.getUUID());
            if (last == null || last != active) {
                LAST_SENT.put(player.getUUID(), active);
                ServerPlayNetworking.send(player, new PatrolMusicPayload(active));
            }
        }
        // Clean up data for players who have disconnected
        AGGRO_HOLD_TICKS.keySet().removeIf(uuid ->
                level.getServer().getPlayerList().getPlayer(uuid) == null);
        LAST_SENT.keySet().removeIf(uuid ->
                level.getServer().getPlayerList().getPlayer(uuid) == null);
    }
}