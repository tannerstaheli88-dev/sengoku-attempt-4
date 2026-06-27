package com.shioh.sengoku.network;

import com.shioh.sengoku.util.HurtTracker;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side manager: detects recently-hurt pigs (via HurtTracker) and gives them
 * a transient melee goal and alerts nearby pigs to retaliate. Runs on server tick.
 */
public class PigRetaliationManager {
    private static final Map<UUID, Integer> REMAINING_TICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, net.minecraft.world.entity.ai.goal.Goal> ACTIVE_GOALS = new ConcurrentHashMap<>();
    private static final Map<UUID, WeakReference<Pig>> ACTIVE_PIGS = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        // Run every 5 ticks (0.25s) to reduce cost
        if ((tickCounter = (tickCounter + 1) % 5) != 0) return;

        try {
            com.shioh.sengoku.config.SengokuConfig cfg = com.shioh.sengoku.config.SengokuConfig.getInstance();
            if (cfg == null || !cfg.pigBoarBehaviorEnabled) return;

            // For each player, check nearby pigs for recent hurt records and trigger retaliation
            for (ServerPlayer player : level.players()) {
                try {
                    AABB box = player.getBoundingBox().inflate(cfg.pigCallForHelpRadius);
                    for (Pig pig : level.getEntitiesOfClass(Pig.class, box)) {
                        try {
                            if (pig.level().isClientSide()) continue;
                            UUID lastPlayer = HurtTracker.getLastPlayer(pig.getUUID());
                            Long lastTime = HurtTracker.getLastTime(pig.getUUID());
                            if (lastPlayer == null || lastTime == null) continue;
                            if (!lastPlayer.equals(player.getUUID())) continue;
                            long now = level.getGameTime();
                            if (now - lastTime > cfg.pigRetaliationDurationTicks) continue;

                            UUID pigId = pig.getUUID();
                            // Ensure the pig targets the player
                            try {
                                if (pig.getTarget() == null || !pig.getTarget().getUUID().equals(player.getUUID())) {
                                    pig.setTarget(player);
                                }
                            } catch (Throwable ignored) {}

                            // Add transient melee goal if not already active
                            if (!ACTIVE_GOALS.containsKey(pigId)) {
                                try {
                                    net.minecraft.world.entity.ai.goal.Goal goal = new AdvancedMeleeAttackGoal(pig, 1.0D, false);
                                    ((com.shioh.sengoku.mixin.MobAccessor) pig).getGoalSelector().addGoal(3, goal);
                                    ACTIVE_GOALS.put(pigId, goal);
                                    ACTIVE_PIGS.put(pigId, new WeakReference<>(pig));
                                    REMAINING_TICKS.put(pigId, cfg.pigRetaliationDurationTicks);
                                } catch (Throwable t) {
                                    try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("[PigRetaliationManager] failed to add goal: {}", t.getMessage()); } catch (Throwable ignored) {}
                                }
                            } else {
                                // refresh duration
                                REMAINING_TICKS.put(pigId, cfg.pigRetaliationDurationTicks);
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }

            // Decrement timers and cleanup expired goals
            try {
                Iterator<Map.Entry<UUID, Integer>> it = REMAINING_TICKS.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Integer> e = it.next();
                    int left = e.getValue() - 5; // we've advanced 5 ticks
                    if (left <= 0) {
                        UUID id = e.getKey();
                        it.remove();
                        net.minecraft.world.entity.ai.goal.Goal g = ACTIVE_GOALS.remove(id);
                        WeakReference<Pig> ref = ACTIVE_PIGS.remove(id);
                        Pig p = ref == null ? null : ref.get();
                        if (g != null && p != null) {
                            try { ((com.shioh.sengoku.mixin.MobAccessor)p).getGoalSelector().removeGoal(g); } catch (Throwable ignored) {}
                        }
                        try { HurtTracker.clear(id); } catch (Throwable ignored) {}
                    } else {
                        e.setValue(left);
                    }
                }
            } catch (Throwable ignored) {}

        } catch (Throwable ignored) {}
    }
}
