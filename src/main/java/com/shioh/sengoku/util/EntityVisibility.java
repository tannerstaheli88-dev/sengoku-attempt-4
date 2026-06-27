package com.shioh.sengoku.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

public final class EntityVisibility {

    private EntityVisibility() {}

    /**
     * Return true if {@code viewer} can realistically detect {@code target} via sight/sound heuristics.
     * Uses entity sensing (if available) and a block raytrace between eye positions as final occlusion check.
     * Returns false if target is invisible or in another dimension.
     */
    public static boolean canDetect(Mob viewer, LivingEntity target) {
        if (viewer == null || target == null) return false;
        if (viewer.level() != target.level()) return false;
        if (target.isInvisible()) return false;

        // If sensing reports line of sight, trust it.
        try {
            if (viewer.getSensing().hasLineOfSight(target)) return true;
        } catch (Throwable ignored) {}

        // Raytrace between eye positions to check for solid occlusion.
        try {
            Vec3 from = viewer.getEyePosition(1.0F);
            Vec3 to = target.getEyePosition(1.0F);
            ClipContext ctx = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer);
            BlockHitResult hit = viewer.level().clip(ctx);
            if (hit.getType() == HitResult.Type.MISS) {
                return true;
            }
        } catch (Throwable ignored) {}

        // Stealth heuristic: players who are crouching are harder to detect at range.
        try {
            double distSq = viewer.distanceToSqr(target);
            if (target instanceof Player && ((Player) target).isCrouching()) {
                // Creepers ignore stealth: don't allow crouching players to be undetectable to creepers.
                if (!(viewer instanceof Creeper)) {
                    // If crouching and beyond ~4 blocks, consider undetectable
                    if (distSq > 16.0D) return false;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }
}
