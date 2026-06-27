package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.util.PlayerNoiseTracker;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class GiantChasePlayerGoal extends Goal {
    private final Giant giant;
    private Player targetPlayer;

    // Max chase distance in blocks
    private static final double CHASE_RADIUS = 60.0;
    private static final double CHASE_RADIUS_SQR = CHASE_RADIUS * CHASE_RADIUS;

    // Field of view for detection (120 degrees = wide peripheral vision)
    private static final double FOV_DEGREES = 120.0;
    private static final double FOV_COS = Math.cos(Math.toRadians(FOV_DEGREES / 2.0));

    public GiantChasePlayerGoal(Giant giant) {
        this.giant = giant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Find nearest valid player within CHASE_RADIUS, applying concealment and gamemode checks
        this.targetPlayer = giant.level().getNearestPlayer(
            giant.getX(),
            giant.getY(),
            giant.getZ(),
            CHASE_RADIUS,
            (entity) -> {
                if (!(entity instanceof Player)) return false;
                Player p = (Player) entity;
                // Exclude creative and spectator players
                if (p.isCreative() || p.isSpectator()) return false;
                // Exclude invisible players (TargetingConditionsMixin also handles this for other paths)
                if (p.isInvisible()) return false;

                // Get noise multiplier for detection
                float noiseMultiplier = PlayerNoiseTracker.getInstance().getDetectionMultiplier(p);
                boolean isNoisy = PlayerNoiseTracker.getInstance().isNoisy(p);

                // Concealment: if player stands in tall foliage or medium concealment, reduce detection radius
                // Noise reduces effectiveness of concealment
                double concealSq = getConcealmentRadiusSq(p) * noiseMultiplier * noiseMultiplier;
                if (concealSq > 0.0D) {
                    double distSq = giant.distanceToSqr(p);
                    if (distSq > concealSq) return false;
                }

                // Field of view check: player must be in front of giant to be detected
                // Unless player is making noise - noise alerts from all directions
                if (!isNoisy && !isInFieldOfView(giant, p)) {
                    return false;
                }

                return true;
            }
        );

        return this.targetPlayer != null;
    }

    @Override
    public void tick() {
        if (targetPlayer != null) {
            giant.getLookControl().setLookAt(targetPlayer, 30.0f, 30.0f);
            giant.getNavigation().moveTo(targetPlayer, 1.2); // Movement speed
        }
    }

    @Override
    public boolean canContinueToUse() {
        // Keep chasing while target is alive, in a valid gamemode, not invisible, and within CHASE_RADIUS
        if (targetPlayer == null || !targetPlayer.isAlive()) return false;
        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return false;
        if (targetPlayer.isInvisible()) return false;

        // Get noise multiplier
        float noiseMultiplier = PlayerNoiseTracker.getInstance().getDetectionMultiplier(targetPlayer);

        // Concealment check (with noise multiplier)
        double concealSq = getConcealmentRadiusSq(targetPlayer) * noiseMultiplier * noiseMultiplier;
        if (concealSq > 0.0D) {
            if (giant.distanceToSqr(targetPlayer) > concealSq) return false;
        }

        // Once locked on, ignore FOV (so giant doesn't lose target mid-chase)
        // But still respect distance limit
        return giant.distanceToSqr(targetPlayer) < CHASE_RADIUS_SQR;
    }

    private double getConcealmentRadiusSq(Player p) {
        // Duplicate of TargetingConditionsMixin logic: returns squared allowed detection radius
        net.minecraft.core.BlockPos feet = net.minecraft.core.BlockPos.containing(p.getX(), p.getY(), p.getZ());
        net.minecraft.core.BlockPos head = feet.above();
        net.minecraft.world.level.block.Block bFeet = p.level().getBlockState(feet).getBlock();
        net.minecraft.world.level.block.Block bHead = p.level().getBlockState(head).getBlock();

        if (isTallConcealmentBlock(bFeet) || isTallConcealmentBlock(bHead)) {
            try { PlayerNoiseTracker.getInstance().markTallConcealment(p); } catch (Throwable ignored) {}
            return 4.0D; // 2^2
        }

        // Allow a short linger period after leaving tall concealment (0.5s = 10 ticks)
        try {
            if (PlayerNoiseTracker.getInstance().wasRecentlyInTallConcealment(p, 10L)) {
                return 4.0D;
            }
        } catch (Throwable ignored) {}
        if (isMediumConcealmentBlock(bFeet) || isMediumConcealmentBlock(bHead)) {
            return 25.0D; // 5^2
        }
        return 0.0D;
    }

    private boolean isTallConcealmentBlock(net.minecraft.world.level.block.Block b) {
        return b == net.minecraft.world.level.block.Blocks.LILAC ||
               b == net.minecraft.world.level.block.Blocks.PEONY ||
               b == net.minecraft.world.level.block.Blocks.ROSE_BUSH ||
               b == net.minecraft.world.level.block.Blocks.SUNFLOWER ||
               b == net.minecraft.world.level.block.Blocks.TALL_GRASS ||
               b == net.minecraft.world.level.block.Blocks.LARGE_FERN;
    }

    private boolean isMediumConcealmentBlock(net.minecraft.world.level.block.Block b) {
        return b == net.minecraft.world.level.block.Blocks.LILY_OF_THE_VALLEY ||
               b == net.minecraft.world.level.block.Blocks.BLUE_ORCHID;
    }

    /**
     * Check if player is within giant's horizontal field of view.
     * Uses horizontal-only angle for practical stealth gameplay.
     */
    private boolean isInFieldOfView(Giant giant, Player player) {
        Vec3 lookVec = giant.getViewVector(1.0F);
        Vec3 lookHorizontal = new Vec3(lookVec.x, 0.0, lookVec.z).normalize();

        Vec3 toPlayer = new Vec3(
            player.getX() - giant.getX(),
            0.0,
            player.getZ() - giant.getZ()
        );

        double distSq = toPlayer.lengthSqr();
        // If player is very close (< 2 blocks), always detect (giant is huge, hard to hide that close)
        if (distSq < 4.0) {
            return true;
        }

        Vec3 toPlayerNorm = toPlayer.normalize();
        double dot = lookHorizontal.dot(toPlayerNorm);

        return dot >= FOV_COS;
    }
}
