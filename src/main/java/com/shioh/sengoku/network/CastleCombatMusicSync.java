package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.AbstractIllager;
import com.shioh.sengoku.entity.WarlordEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side scanner which detects if one or more hostile mobs are targeting a player
 * while the player is inside a castle structure. This triggers castle combat music.
 * It will not activate if a Warlord music is active nearby (so Warlord music keeps priority).
 */
public class CastleCombatMusicSync {
    private static final TagKey<Structure> CASTLE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:castle_music"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final TagKey<EntityType<?>> TAKEDA_TAG = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:takeda_clan"));
    private static final TagKey<EntityType<?>> SATOMI_TAG = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:satomi_clan"));
    private static final TagKey<EntityType<?>> KOBAYAKAWA_TAG = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:kobayakawa_clan"));
    private static int tickCounter = 0;
    private static final int INTERVAL = 20; // once per second
    private static final double SEARCH_RADIUS = 48.0D;

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % INTERVAL) != 0) return;
        try {
            Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager structureManager = level.structureManager();

            for (ServerPlayer player : level.players()) {
                boolean insideCastle = false;
                try {
                    for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(CASTLE_TAG)) {
                        Structure structure = holder.value();
                        StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                        if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                            insideCastle = true;
                            break;
                        }
                    }
                } catch (Throwable ignored) {}

boolean combatActive = false;
if (insideCastle) {
    try {
        boolean warlordActive = false;
        for (WarlordEntity w : level.getEntitiesOfClass(WarlordEntity.class, player.getBoundingBox().inflate(96.0D))) {
            try {
                if (!w.isRemoved() && w.isMusicActive()) {
                    warlordActive = true;
                    break;
                }
            } catch (Throwable ignored) {}
        }

        if (!warlordActive) {
            int hostileCount = 0;
            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(SEARCH_RADIUS))) {
                try {
                    if (!(mob instanceof Monster)) continue;
                    if (mob.isAlliedTo(player)) continue;

                    boolean isAllowed = false;
                    if (mob instanceof AbstractIllager) {
                        isAllowed = true;
                    } else {
                        EntityType<?> et = mob.getType();
                        try {
                            if (et.is(TAKEDA_TAG) || et.is(SATOMI_TAG) || et.is(KOBAYAKAWA_TAG)) {
                                isAllowed = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (!isAllowed) continue;

                    if (mob.getTarget() == player || mob.getLastHurtByMob() == player) {
                        hostileCount++;
                    }
                } catch (Throwable ignored) {}
            }

            Boolean last = LAST_SENT.get(player.getUUID());
            boolean currentlyActive = last != null && last.booleanValue();

            if (currentlyActive) {
                // Music is playing — only stop when zero enemies remain
                combatActive = hostileCount > 0;
            } else {
                // Music is not playing — only start when 2+ enemies engaged
                combatActive = hostileCount >= 2;
            }
        }
    } catch (Throwable ignored) {}
}

                Boolean last = LAST_SENT.get(player.getUUID());
                if (last == null || last.booleanValue() != combatActive) {
                    LAST_SENT.put(player.getUUID(), combatActive);
                    ServerPlayNetworking.send(player, new CastleCombatMusicPayload(combatActive));
                }
            }
        } catch (Throwable t) {
            // non-critical
        }
    }
}
