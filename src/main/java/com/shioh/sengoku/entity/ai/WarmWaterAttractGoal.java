package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import com.shioh.sengoku.system.WarmWaterSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.particles.ParticleTypes;
import com.shioh.sengoku.sengokuFabric;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.List;
import java.util.ArrayList;
// BlockPathTypes is referenced via reflection below to remain mapping-agnostic

/**
 * Goal: Attract macaques to 'warm water' — defined as water blocks that have magma nearby.
 * This is a simple, lightweight search + navigate goal. It intentionally keeps a low
 * activation frequency and a low priority so macaques remain primarily lazy/sitting.
 * idk if this even works
 */
public class WarmWaterAttractGoal extends Goal {
    private final MacaqueEntity macaque;
    private BlockPos targetPos = null;
    private int cooldown = 0;
    private int targetRetry = 0;
    private static final int MAX_RETRIES = 3;
    private float prevWaterMalus = Float.NaN;

    public WarmWaterAttractGoal(MacaqueEntity macaque) {
        this.macaque = macaque;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private Optional<BlockPos> findNearestWarmWater(Level level, BlockPos start, int distance) {
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy <= distance; dy = dy > 0 ? -dy : 1 - dy) {
            for (int dx = 0; dx < distance; dx++) {
                for (int dz = 0; dz <= dx; dz = dz > 0 ? -dz : 1 - dz) {
                    for (int l = dz < dx && dz > -dx ? dx : 0; l <= dx; l = l > 0 ? -l : 1 - l) {
                        mutable.setWithOffset(start, dz, dy - 1, l);
                        if (start.closerThan(mutable, distance)) {
                            if (level.getFluidState(mutable).is(FluidTags.WATER)) {
                                if (WarmWaterSystem.isNearMagmaBlock(level, mutable)) {
                                    // prefer edge candidates similar to bee logic
                                    BlockPos above = mutable.above();
                                    if (!level.getFluidState(above).is(FluidTags.WATER) && level.getBlockState(above.below()).isFaceSturdy(level, above.below(), Direction.UP)) {
                                        candidates.add(above);
                                        continue;
                                    }
                                    BlockPos[] neigh = new BlockPos[] { mutable.north(), mutable.south(), mutable.east(), mutable.west() };
                                    for (BlockPos npos : neigh) {
                                        if (!level.getFluidState(npos).is(FluidTags.WATER) && level.getBlockState(npos.below()).isFaceSturdy(level, npos.below(), Direction.UP)) {
                                            candidates.add(npos);
                                        }
                                    }
                                    // fallback to above if no other edge
                                    candidates.add(above);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty()) return Optional.empty();
        // return the nearest candidate by squared distance
        BlockPos best = null;
        double bestSq = Double.MAX_VALUE;
        for (BlockPos c : candidates) {
            double dx = c.getX() - start.getX();
            double dy = c.getY() - start.getY();
            double dz = c.getZ() - start.getZ();
            double sq = dx*dx + dy*dy + dz*dz;
            if (sq < bestSq) { bestSq = sq; best = c; }
        }
        return Optional.ofNullable(best);
    }

    private List<BlockPos> findWarmWaterCandidates(Level level, BlockPos start, int distance) {
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy <= distance; dy = dy > 0 ? -dy : 1 - dy) {
            for (int dx = 0; dx < distance; dx++) {
                for (int dz = 0; dz <= dx; dz = dz > 0 ? -dz : 1 - dz) {
                    for (int l = dz < dx && dz > -dx ? dx : 0; l <= dx; l = l > 0 ? -l : 1 - l) {
                        mutable.setWithOffset(start, dz, dy - 1, l);
                        if (start.closerThan(mutable, distance)) {
                            if (level.getFluidState(mutable).is(FluidTags.WATER)) {
                                if (WarmWaterSystem.isNearMagmaBlock(level, mutable)) {
                                    BlockPos above = mutable.above();
                                    if (!level.getFluidState(above).is(FluidTags.WATER) && level.getBlockState(above.below()).isFaceSturdy(level, above.below(), Direction.UP)) {
                                        candidates.add(above);
                                        continue;
                                    }
                                    BlockPos[] neigh = new BlockPos[] { mutable.north(), mutable.south(), mutable.east(), mutable.west() };
                                    for (BlockPos npos : neigh) {
                                        if (!level.getFluidState(npos).is(FluidTags.WATER) && level.getBlockState(npos.below()).isFaceSturdy(level, npos.below(), Direction.UP)) {
                                            candidates.add(npos);
                                        }
                                    }
                                    candidates.add(above);
                                }
                            }
                        }
                    }
                }
            }
        }
        return candidates;
    }

    @Override
    public boolean canUse() {
        if (this.macaque.isSitting()) return false; // prefer sitting first
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        // Occasionally scan for warm water nearby
        BlockPos start = this.macaque.blockPosition();
        int searchRadius = 12;
        double bestDistSq = Double.MAX_VALUE;
        BlockPos best = null;

        Level level = this.macaque.level();
        try {
            List<BlockPos> candidates = this.findWarmWaterCandidates(level, start, searchRadius);
            if (!candidates.isEmpty()) {
                // sort implicitly by distance by scanning in order of nearest-first
                candidates.sort((a, b) -> {
                    double adx = a.getX()-start.getX(); double ady = a.getY()-start.getY(); double adz = a.getZ()-start.getZ();
                    double bdx = b.getX()-start.getX(); double bdy = b.getY()-start.getY(); double bdz = b.getZ()-start.getZ();
                    double asq = adx*adx + ady*ady + adz*adz;
                    double bsq = bdx*bdx + bdy*bdy + bdz*bdz;
                    return Double.compare(asq, bsq);
                });

                PathNavigation nav = this.macaque.getNavigation();
                // Prefer a candidate we can create a path to and canReach()
                for (BlockPos candidate : candidates) {
                    try {
                        Path p = null;
                        try { p = nav.createPath(candidate, 0); } catch (Throwable t) {}
                        if (p != null && p.canReach()) {
                            this.targetPos = candidate;
                            this.targetRetry = 0;
                            try { sengokuFabric.LOGGER.info("Macaque {} targeting warm water candidate {} (direct path)", this.macaque.getId(), candidate); } catch (Throwable ex) {}
                            try { if (!level.isClientSide) level.addParticle(ParticleTypes.HAPPY_VILLAGER, candidate.getX()+0.5D, candidate.getY()+0.5D, candidate.getZ()+0.5D, 0.0D, 0.1D, 0.0D); } catch (Throwable ex) {}
                            return true;
                        }
                    } catch (Throwable ex) { /* ignore per-try */ }
                }

                // No reachable path for any candidate — attempt a coordinate fallback to the nearest candidate
                BlockPos nearest = candidates.get(0);
                try {
                    nav.moveTo(nearest.getX() + 0.5D, nearest.getY() + 0.5D, nearest.getZ() + 0.5D, 1.0D);
                    this.targetPos = nearest;
                    this.targetRetry = 0;
                    try { sengokuFabric.LOGGER.info("Macaque {} targeting warm water candidate {} (coordinate fallback)", this.macaque.getId(), nearest); } catch (Throwable ex) {}
                    try { if (!level.isClientSide) level.addParticle(ParticleTypes.HAPPY_VILLAGER, nearest.getX()+0.5D, nearest.getY()+0.5D, nearest.getZ()+0.5D, 0.0D, 0.1D, 0.0D); } catch (Throwable ex) {}
                    return true;
                } catch (Throwable ex2) {
                    this.cooldown = 10 + this.macaque.getRandom().nextInt(20);
                    try { sengokuFabric.LOGGER.info("Macaque {} candidate unreachable: {}", this.macaque.getId(), nearest); } catch (Throwable ex) {}
                    return false;
                }
            }
        } catch (Throwable ex) {}

        // no target found; set a longer cooldown before next scan
        this.cooldown = 100 + this.macaque.getRandom().nextInt(200);
        return false;
    }

    @Override
    public void start() {
        if (this.targetPos != null) {
            this.targetRetry = 0;
            // temporarily reduce water malus so pathfinder will consider near-water nodes
            try {
                // try a few likely class names for the path-type enum/holder
                String[] candidates = new String[] {
                    "net.minecraft.world.level.pathfinder.PathNodeType",
                    "net.minecraft.world.level.pathfinder.BlockPathTypes",
                    "net.minecraft.world.level.pathfinder.PathNodeTypes"
                };
                boolean applied = false;
                for (String cn : candidates) {
                    try {
                        Class<?> clazz = Class.forName(cn);
                        java.lang.reflect.Field f = clazz.getField("WATER");
                        Object waterConst = f.get(null);
                        java.lang.reflect.Method getM = this.macaque.getClass().getMethod("getPathfindingMalus", clazz);
                        java.lang.reflect.Method setM = this.macaque.getClass().getMethod("setPathfindingMalus", clazz, float.class);
                        Object prev = getM.invoke(this.macaque, waterConst);
                        if (prev instanceof Float) this.prevWaterMalus = (Float) prev;
                        else if (prev instanceof Double) this.prevWaterMalus = ((Double) prev).floatValue();
                        setM.invoke(this.macaque, waterConst, 0.0F);
                        applied = true;
                        break;
                    } catch (Throwable t) {
                        // try next candidate
                    }
                }
                if (!applied) this.prevWaterMalus = Float.NaN;
            } catch (Throwable t) {}
            // try to path to the candidate target (usually the block above water)
            try {
                PathNavigation nav = this.macaque.getNavigation();
                Path p = nav.createPath(this.targetPos, 0);
                if (p != null) nav.moveTo(p, 1.0D);
                else nav.moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, 1.0D);
            } catch (Throwable exStart) {
                this.macaque.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, 1.0D);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPos == null) return false;
        // Keep the goal active while we still have a target so that tick()
        // can observe the navigation state and attempt replanning if the
        // navigation finishes prematurely. Also stop if the macaque already
        // started sitting.
        if (this.macaque.isSitting()) return false;
        return true;
    }

    @Override
    public void tick() {
        if (this.targetPos == null) return;
        // If reached target area, sit and rest for a bit
        double dx = this.macaque.getX() - (this.targetPos.getX() + 0.5D);
        double dz = this.macaque.getZ() - (this.targetPos.getZ() + 0.5D);
        double dy = this.macaque.getY() - (this.targetPos.getY());
        double distSq = dx * dx + dz * dz + dy * dy;
        // if close to the target (within ~2 blocks), sit
        if (distSq < 4.0D) {
            this.macaque.getNavigation().stop();
            this.macaque.setSitting(true);
            // after finishing, set cooldown
            this.cooldown = 200 + this.macaque.getRandom().nextInt(400);
            this.targetPos = null;
        }
        // If navigation reports done but we're not close enough, treat as a failed approach
            else if (this.macaque.getNavigation().isDone() && distSq > 16.0D) {
                // If navigation reports done but we're not close enough, try replanning to adjacent edges
                Level level = this.macaque.level();
                boolean replanned = false;
                try {
                    PathNavigation nav = this.macaque.getNavigation();
                    // Try re-attempting path to the same target a few times before giving up
                    if (this.targetRetry < MAX_RETRIES) {
                        this.targetRetry++;
                        try {
                            Path p = null;
                            try { p = nav.createPath(this.targetPos, 0); } catch (Throwable t) {}
                            if (p != null && p.canReach()) {
                                try { sengokuFabric.LOGGER.info("Macaque {} retrying path to {} (attempt {})", this.macaque.getId(), this.targetPos, this.targetRetry); } catch (Throwable ex) {}
                                nav.moveTo(p, 1.0D);
                                return;
                            } else {
                                // try coordinate jitter to nudge the pathfinder
                                try { nav.moveTo(this.targetPos.getX()+0.5D, this.targetPos.getY()+0.5D, this.targetPos.getZ()+0.5D, 1.0D); } catch (Throwable t) {}
                            }
                        } catch (Throwable t) {}
                    }
                    BlockPos[] alt = new BlockPos[] { this.targetPos.above(), this.targetPos.north(), this.targetPos.south(), this.targetPos.east(), this.targetPos.west() };
                        for (BlockPos cand : alt) {
                        if (!level.getFluidState(cand).is(FluidTags.WATER) && level.getBlockState(cand.below()).isFaceSturdy(level, cand.below(), Direction.UP)) {
                            Path p = nav.createPath(cand, 0);
                            if (p != null && p.canReach()) {
                                this.targetPos = cand;
                                try { sengokuFabric.LOGGER.info("Macaque {} replanned to {}", this.macaque.getId(), cand); } catch (Throwable exReplanLog) {}
                                nav.moveTo(p, 1.0D);
                                replanned = true;
                                try { if (!level.isClientSide) level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, cand.getX()+0.5D, cand.getY()+0.5D, cand.getZ()+0.5D, 0.0D, 0.05D, 0.0D); } catch (Throwable exReplanParticle) {}
                                break;
                            }
                        }
                    }
                } catch (Throwable exAlt) {}

                if (!replanned) {
                    try { if (!level.isClientSide) level.addParticle(ParticleTypes.SMOKE, this.targetPos.getX()+0.5D, this.targetPos.getY()+0.5D, this.targetPos.getZ()+0.5D, 0.0D, 0.05D, 0.0D); } catch (Throwable exSmoke) {}
                    // restore malus before clearing
                    try {
                        if (!Float.isNaN(this.prevWaterMalus)) {
                            // restore via reflection to avoid mapping/class-name issues
                            String[] candidatesRestore = new String[] {
                                "net.minecraft.world.level.pathfinder.PathNodeType",
                                "net.minecraft.world.level.pathfinder.BlockPathTypes",
                                "net.minecraft.world.level.pathfinder.PathNodeTypes"
                            };
                            for (String cn2 : candidatesRestore) {
                                try {
                                    Class<?> clazz2 = Class.forName(cn2);
                                    java.lang.reflect.Field f2 = clazz2.getField("WATER");
                                    Object waterConst2 = f2.get(null);
                                    java.lang.reflect.Method setM2 = this.macaque.getClass().getMethod("setPathfindingMalus", clazz2, float.class);
                                    setM2.invoke(this.macaque, waterConst2, this.prevWaterMalus);
                                    break;
                                } catch (Throwable t2) {
                                    // try next
                                }
                            }
                        }
                    } catch (Throwable t) {}
                    this.prevWaterMalus = Float.NaN;
                    this.targetPos = null;
                    this.cooldown = 10 + this.macaque.getRandom().nextInt(30);
                }
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        // restore water malus if we changed it
        try {
            // restore via reflection; mirror the class-name candidates used at start()
            String[] candidates = new String[] {
                "net.minecraft.world.level.pathfinder.PathNodeType",
                "net.minecraft.world.level.pathfinder.BlockPathTypes",
                "net.minecraft.world.level.pathfinder.PathNodeTypes"
            };
            for (String cn : candidates) {
                try {
                    Class<?> clazz = Class.forName(cn);
                    java.lang.reflect.Field f = clazz.getField("WATER");
                    Object waterConst = f.get(null);
                    java.lang.reflect.Method setM = this.macaque.getClass().getMethod("setPathfindingMalus", clazz, float.class);
                    if (!Float.isNaN(this.prevWaterMalus)) setM.invoke(this.macaque, waterConst, this.prevWaterMalus);
                    break;
                } catch (Throwable t) {
                }
            }
        } catch (Throwable t) {}
        this.prevWaterMalus = Float.NaN;
        // small cooldown to avoid immediate re-trigger
        if (this.cooldown <= 0) this.cooldown = 60;
    }
}
