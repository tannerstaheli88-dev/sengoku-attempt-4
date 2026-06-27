package com.shioh.sengoku.ai;

import com.shioh.sengoku.registry.SengokuBlocks;
import com.shioh.sengoku.config.SengokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AI goal that makes yokai mobs avoid shide blocks (Shinto paper streamers).
 * Similar to how piglins avoid soul fire/torches.
 * 
 * Performance optimizations:
 * - Only checks every 10 ticks instead of every tick
 * - Uses distance-squared for cheaper calculations
 * - Checks closer blocks first (early exit)
 * - Reduced Y-axis search range
 */
public class AvoidShideGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final int searchRange;
    private final int searchRangeSquared;
    private Vec3 wantedPos;
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 1; 
    private int cooldown = 0;
    private static final int COOLDOWN_TICKS = 40; // ticks to wait after fleeing
    private static final int SAMPLE_ATTEMPTS = 20; // random samples per check

    public AvoidShideGoal(PathfinderMob mob, double speedModifier, int searchRange) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRange = searchRange;
        this.searchRangeSquared = searchRange * searchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Respect config toggle
        if (!SengokuConfig.getInstance().monstersAvoidShideEnabled) return false;
        // Only check every 10 ticks to reduce performance impact
        // could be whats causing all that stuttering for some players.
        if (++tickCounter < CHECK_INTERVAL) {
            return false;
        }
        tickCounter = 0;
        // Cooldown to avoid repeated repathing
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        return this.isShideNearby();
    }

    // Randomly sample nearby positions instead of scanning the whole area.
    private boolean isShideNearby() {
        BlockPos mobPos = this.mob.blockPosition();
        // First, check a small guaranteed radius around the mob for reliability.
        int immediateRadius = Math.min(3, this.searchRange);
        int yRange = 2;
        BlockPos from = mobPos.offset(-immediateRadius, -yRange, -immediateRadius);
        BlockPos to = mobPos.offset(immediateRadius, yRange, immediateRadius);

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (this.mob.level().getBlockState(pos).getBlock() == SengokuBlocks.SHIDE) {
                Vec3 awayPos = DefaultRandomPos.getPosAway(this.mob, 16, 7, Vec3.atBottomCenterOf(pos));
                if (awayPos != null) {
                    this.wantedPos = awayPos;
                    this.cooldown = COOLDOWN_TICKS;
                    return true;
                }
            }
        }

        // Then do biased random sampling (closer positions more likely).
        int closeSampleMax = Math.min(this.searchRange, 4);
        for (int i = 0; i < SAMPLE_ATTEMPTS; i++) {
            int rx, rz;
            // Most samples are near the mob; occasionally sample farther out
            if (i % 5 == 0) {
                rx = this.mob.getRandom().nextInt(this.searchRange * 2 + 1) - this.searchRange;
                rz = this.mob.getRandom().nextInt(this.searchRange * 2 + 1) - this.searchRange;
            } else {
                rx = this.mob.getRandom().nextInt(closeSampleMax * 2 + 1) - closeSampleMax;
                rz = this.mob.getRandom().nextInt(closeSampleMax * 2 + 1) - closeSampleMax;
            }
            int ry = this.mob.getRandom().nextInt(5) - 2;

            BlockPos samplePos = mobPos.offset(rx, ry, rz);
            if (this.mob.level().getBlockState(samplePos).getBlock() == SengokuBlocks.SHIDE) {
                Vec3 awayPos = DefaultRandomPos.getPosAway(this.mob, 16, 7, Vec3.atBottomCenterOf(samplePos));
                if (awayPos != null) {
                    this.wantedPos = awayPos;
                    this.cooldown = COOLDOWN_TICKS;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedPos.x, this.wantedPos.y, this.wantedPos.z, this.speedModifier);
    }

    @Override
    public void stop() {
        this.wantedPos = null;
    }
}
