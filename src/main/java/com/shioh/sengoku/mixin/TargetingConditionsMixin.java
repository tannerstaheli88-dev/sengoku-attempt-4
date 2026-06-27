package com.shioh.sengoku.mixin;

import com.shioh.sengoku.util.PlayerNoiseTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Make all targeting checks fail for invisible players so mobs do not
 * acquire them as targets via standard TargetingConditions-based queries.
 * Also enforces field-of-view based detection for stealth gameplay.
 */
@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {

    // FOV angle in degrees (120° = wide peripheral vision, realistic for most mobs)
    private static final double FOV_DEGREES = 120.0;
    private static final double FOV_COS = Math.cos(Math.toRadians(FOV_DEGREES / 2.0));

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void sengoku$blockInvisibleOrConcealedTargets(LivingEntity attacker, LivingEntity candidate, CallbackInfoReturnable<Boolean> cir) {
        // Creepers ignore the custom stealth system (concealment/FOV/invisibility suppression).
        // Let vanilla targeting logic run unchanged for them.
        if (attacker instanceof Creeper) return;

        if (!(candidate instanceof Player)) return;
        Player p = (Player) candidate;

        // Invisibility: unconditional block
        if (p.isInvisible()) {
            cir.setReturnValue(false);
            return;
        }

        // Check noise level - noisy players are easier to detect
        float noiseMultiplier = PlayerNoiseTracker.getInstance().getDetectionMultiplier(p);
        boolean isNoisy = PlayerNoiseTracker.getInstance().isNoisy(p);

        // Concealment: some plants give stronger concealment than others.
        // Noise reduces effectiveness of concealment
        double concealSq = sengoku$getConcealmentRadiusSq(p) * noiseMultiplier * noiseMultiplier;
        if (concealSq > 0.0D && attacker != null) {
            double distSq = attacker.distanceToSqr(p);
            if (distSq > concealSq) {
                cir.setReturnValue(false);
                return;
            }
        }

        // Field of View: mobs can only detect what's in front of them (not 360°)
        // Exception 1: if the mob already has this entity as its target, skip FOV check
        // (so they don't lose sight mid-combat when the target moves behind them)
        // Exception 2: if player is making significant noise, skip FOV check
        // (noise alerts mobs in all directions)
        if (attacker != null && !isNoisy) {
            // Check if attacker is a Mob and already has this candidate as target
            boolean hasExistingTarget = false;
            if (attacker instanceof Mob) {
                Mob mob = (Mob) attacker;
                hasExistingTarget = (mob.getTarget() == candidate);
            }
            
            // Only apply FOV check if this is a new target acquisition and player is quiet
            if (!hasExistingTarget && !sengoku$isInFieldOfView(attacker, candidate)) {
                cir.setReturnValue(false);
            }
        }
    }

    private double sengoku$getConcealmentRadiusSq(Player p) {
        // Returns squared detection radius allowed when the player is standing
        // inside specific concealment blocks. 0.0 = no concealment.
        BlockPos feet = BlockPos.containing(p.getX(), p.getY(), p.getZ());
        BlockPos head = feet.above();
        Block bFeet = p.level().getBlockState(feet).getBlock();
        Block bHead = p.level().getBlockState(head).getBlock();

        // Strong concealment: tall foliage -> 2 blocks
        if (sengoku$isTallConcealmentBlock(bFeet) || sengoku$isTallConcealmentBlock(bHead)) {
            // Mark player as being in tall concealment so leaving it can linger briefly
            try { PlayerNoiseTracker.getInstance().markTallConcealment(p); } catch (Throwable ignored) {}
            return 4.0D; // 2^2
        }

        // Allow a short linger period after leaving tall concealment (0.5s = 10 ticks)
        try {
            if (PlayerNoiseTracker.getInstance().wasRecentlyInTallConcealment(p, 10L)) {
                return 4.0D;
            }
        } catch (Throwable ignored) {}

        // Medium concealment: small flowers like lily_of_the_valley and blue_orchid -> 5 blocks
        if (sengoku$isMediumConcealmentBlock(bFeet) || sengoku$isMediumConcealmentBlock(bHead)) {
            return 25.0D; // 5^2
        }

        return 0.0D;
    }

    private boolean sengoku$isTallConcealmentBlock(Block b) {
        return b == Blocks.LILAC ||
               b == Blocks.PEONY ||
               b == Blocks.ROSE_BUSH ||
               b == Blocks.SUNFLOWER ||
               b == Blocks.TALL_GRASS ||
               b == Blocks.LARGE_FERN;
    }

    private boolean sengoku$isMediumConcealmentBlock(Block b) {
        return b == Blocks.LILY_OF_THE_VALLEY ||
               b == Blocks.BLUE_ORCHID;
    }

    /**
     * Check if candidate is within attacker's horizontal field of view.
     * Uses horizontal-only angle (ignoring Y-axis) for practical stealth gameplay.
     * @return true if candidate is within FOV or if attacker has no clear facing direction
     */
    private boolean sengoku$isInFieldOfView(LivingEntity attacker, LivingEntity candidate) {
        // Get attacker's look direction (horizontal only for practical gameplay)
        Vec3 lookVec = attacker.getViewVector(1.0F);
        Vec3 lookHorizontal = new Vec3(lookVec.x, 0.0, lookVec.z).normalize();

        // Vector from attacker to candidate (horizontal only)
        Vec3 toCandidate = new Vec3(
            candidate.getX() - attacker.getX(),
            0.0,
            candidate.getZ() - attacker.getZ()
        );

        double distSq = toCandidate.lengthSqr();
        // If candidate is extremely close (< 1 block), always detect (too close to hide)
        if (distSq < 1.0) {
            return true;
        }

        Vec3 toCandidateNorm = toCandidate.normalize();

        // Dot product gives cosine of angle between vectors
        double dot = lookHorizontal.dot(toCandidateNorm);

        // If dot >= FOV_COS, candidate is within the field of view
        return dot >= FOV_COS;
    }
}
