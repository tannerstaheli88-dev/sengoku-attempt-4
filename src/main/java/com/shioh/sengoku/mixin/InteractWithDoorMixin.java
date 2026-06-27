package com.shioh.sengoku.mixin;

import com.mojang.datafixers.kinds.OptionalBox.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoubleDoorBlock;
import net.minecraft.world.level.block.TripleDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
 

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Mixin(InteractWithDoor.class)
public abstract class InteractWithDoorMixin {

    @Shadow
    private static Optional<Set<GlobalPos>> rememberDoorToClose(
            MemoryAccessor<Mu, Set<GlobalPos>> doorsToClose,
            Optional<Set<GlobalPos>> doorPositions,
            ServerLevel level,
            BlockPos pos
    ) { throw new AssertionError(); }

    private static boolean isMobInteractableDoor(BlockState state) {
        Block b = state.getBlock();
        // Accept if:
        // - in the tag, AND is any known door type; OR
        // - is one of our custom door types regardless of tag (failsafe)
        return (state.is(BlockTags.MOB_INTERACTABLE_DOORS) && (b instanceof DoorBlock || b instanceof DoubleDoorBlock || b instanceof TripleDoorBlock))
                || (b instanceof DoubleDoorBlock || b instanceof TripleDoorBlock);
    }

    private static boolean isOpenGeneric(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof DoorBlock door) return door.isOpen(state);
        if (b instanceof DoubleDoorBlock dd) return dd.isOpen(state);
        if (b instanceof TripleDoorBlock) return state.getValue(TripleDoorBlock.OPEN);
        return false;
    }

    private static boolean isOpenAt(ServerLevel level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        if (b instanceof TripleDoorBlock) {
            // At canonical middle-lower, check left/middle/right lower blocks; if any true, treat open
            BlockPos middleLower = normalizeDoorPos(level, pos, state);
            if (middleLower == null) return isOpenGeneric(state);
            BlockState middle = level.getBlockState(middleLower);
            net.minecraft.core.Direction facing = middle.getValue(TripleDoorBlock.FACING);
            BlockPos leftLower = middleLower.relative(facing.getCounterClockWise());
            BlockPos rightLower = middleLower.relative(facing.getClockWise());
            BlockState left = level.getBlockState(leftLower);
            BlockState right = level.getBlockState(rightLower);
            boolean any = (middle.getBlock() instanceof TripleDoorBlock && middle.getValue(TripleDoorBlock.OPEN))
                    || (left.getBlock() instanceof TripleDoorBlock && left.getValue(TripleDoorBlock.OPEN))
                    || (right.getBlock() instanceof TripleDoorBlock && right.getValue(TripleDoorBlock.OPEN));
            return any;
        }
        return isOpenGeneric(state);
    }

    private static void setOpenGeneric(@Nullable LivingEntity actor, ServerLevel level, BlockState state, BlockPos pos, boolean open) {
        Block b = state.getBlock();
        if (b instanceof DoorBlock door) {
            door.setOpen(actor, level, state, pos, open);
        } else if (b instanceof DoubleDoorBlock dd) {
            dd.setOpen(actor, level, state, pos, open);
        } else if (b instanceof TripleDoorBlock td) {
            td.setOpenAll(actor, level, pos, state, open);
        }
    }

    /**
     * Compute a canonical control position for a door block so we don't spam open/close for neighbor/upper parts.
     * - DoubleDoorBlock: use the lower half of the column.
     * - TripleDoorBlock: use the middle-lower block; if not directly, search 4-neighbors.
     */
    @Nullable
    private static BlockPos normalizeDoorPos(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoubleDoorBlock) {
            // ensure we operate on the lower block of the column
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf half = state.getValue(DoubleDoorBlock.HALF);
            BlockPos lower = half == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockState lowerState = level.getBlockState(lower);
            // Prefer the extension/side column if it exists (SIDE == true)
            try {
                if (lowerState.getBlock() instanceof DoubleDoorBlock && lowerState.getValue(DoubleDoorBlock.SIDE)) {
                    return lower;
                }
            } catch (Exception ignored) {}

            // If this column isn't SIDE==true, check the two horizontal neighbors
            // (left/right relative to facing) for a DoubleDoorBlock lower with SIDE==true
            try {
                net.minecraft.core.Direction facing = lowerState.getValue(DoubleDoorBlock.FACING);
                BlockPos left = lower.relative(facing.getCounterClockWise());
                BlockPos right = lower.relative(facing.getClockWise());
                BlockState ls = level.getBlockState(left);
                BlockState rs = level.getBlockState(right);
                if (ls.getBlock() instanceof DoubleDoorBlock && ls.getValue(DoubleDoorBlock.SIDE)) return left;
                if (rs.getBlock() instanceof DoubleDoorBlock && rs.getValue(DoubleDoorBlock.SIDE)) return right;
            } catch (Exception ignored) {}

            return lower;
        }
        if (state.getBlock() instanceof TripleDoorBlock) {
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf half = state.getValue(TripleDoorBlock.HALF);
            BlockPos candidate = half == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockState candState = level.getBlockState(candidate);
            if (candState.getBlock() instanceof TripleDoorBlock && candState.getValue(TripleDoorBlock.PART) == net.minecraft.world.level.block.TripleDoorBlock.Part.MIDDLE && candState.getValue(TripleDoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                return candidate;
            }
            // try orthogonal neighbors
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos p = candidate.relative(d);
                BlockState s = level.getBlockState(p);
                if (s.getBlock() instanceof TripleDoorBlock && s.getValue(TripleDoorBlock.PART) == net.minecraft.world.level.block.TripleDoorBlock.Part.MIDDLE && s.getValue(TripleDoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                    return p;
                }
            }
            return null;
        }
        return pos;
    }

    @Overwrite
    public static BehaviorControl<LivingEntity> create() {
        MutableObject<Node> lastNode = new MutableObject<>(null);
        MutableInt cooldown = new MutableInt(0);
        return BehaviorBuilder.create(
                instance -> instance.group(
                        instance.present(MemoryModuleType.PATH),
                        instance.registered(MemoryModuleType.DOORS_TO_CLOSE),
                        instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                ).apply(instance, (pathMem, doorsToCloseMem, nearestMem) -> (serverLevel, living, time) -> {
                    Path path = instance.get(pathMem);
                    final Optional<Set<GlobalPos>>[] doorsToCloseRef = new Optional[]{instance.tryGet(doorsToCloseMem)};
                    if (!path.notStarted() && !path.isDone()) {
                        if (Objects.equals(lastNode.getValue(), path.getNextNode())) {
                            cooldown.setValue(20);
                        } else if (cooldown.decrementAndGet() > 0) {
                            return false;
                        }

                        lastNode.setValue(path.getNextNode());
                        Node prev = path.getPreviousNode();
                        Node next = path.getNextNode();

                        // Pre-compute canonical prev/next control positions (if any)
                        java.util.function.Function<BlockPos, BlockPos> toCanonical = (p) -> {
                            BlockState s = serverLevel.getBlockState(p);
                            if (!isMobInteractableDoor(s)) return null;
                            return normalizeDoorPos(serverLevel, p, s);
                        };

                        BlockPos canonicalPrev = toCanonical.apply(prev.asBlockPos());
                        BlockPos canonicalNext = toCanonical.apply(next.asBlockPos());
                        // debug logs removed

                        // Track doors we already tried to open this tick to avoid duplicate attempts
                        java.util.Set<BlockPos> openedThisTick = new java.util.HashSet<>();

                        // Priority pass: if any TripleDoorBlock exists near the previous
                        // or next node, resolve its canonical middle and open that
                        // first. This forces villagers to focus the middle part
                        // regardless of which side they approach from.
                        try {
                            java.util.List<BlockPos> scanCenters = java.util.List.of(prev.asBlockPos(), next.asBlockPos());
                            for (BlockPos center : scanCenters) {
                                for (int dx = -1; dx <= 1; dx++) {
                                    for (int dz = -1; dz <= 1; dz++) {
                                        for (int dy = 0; dy >= -1; dy--) {
                                            BlockPos check = center.offset(dx, dy, dz);
                                            BlockState st0 = serverLevel.getBlockState(check);
                                            if (!(st0.getBlock() instanceof TripleDoorBlock)) continue;
                                            BlockPos canon = normalizeDoorPos(serverLevel, check, st0);
                                            if (canon == null) {
                                                // fallback we added earlier: search neighbors
                                                for (int sx = -1; sx <= 1 && canon == null; sx++) {
                                                    for (int sz = -1; sz <= 1 && canon == null; sz++) {
                                                        for (int sy = 0; sy >= -1 && canon == null; sy--) {
                                                            BlockPos c2 = check.offset(sx, sy, sz);
                                                            BlockState s2 = serverLevel.getBlockState(c2);
                                                            if (s2.getBlock() instanceof TripleDoorBlock) {
                                                                try {
                                                                    if (s2.getValue(TripleDoorBlock.PART) == TripleDoorBlock.Part.MIDDLE && s2.getValue(TripleDoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                                                                        canon = c2;
                                                                    }
                                                                } catch (Exception ignored) {}
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (canon == null) continue;
                                            if (!openedThisTick.add(canon)) continue;
                                            BlockState st = serverLevel.getBlockState(canon);
                                            boolean wasOpen = isOpenAt(serverLevel, canon, st);
                                            if (!wasOpen) {
                                                setOpenGeneric(living, serverLevel, st, canon, true);
                                                BlockState stAfter = serverLevel.getBlockState(canon);
                                                boolean nowOpen = isOpenAt(serverLevel, canon, stAfter);
                                                // debug logs removed
                                            }
                                            if (canon.equals(canonicalPrev) || canon.equals(canonicalNext)) {
                                                doorsToCloseRef[0] = rememberDoorToClose(doorsToCloseMem, doorsToCloseRef[0], serverLevel, canon);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}

                        // Check the door at the node, its above block, and their 4-neighbors to handle multi-column doors
                        java.util.function.BiConsumer<BlockPos, Boolean> consider = (pos, allowRemember) -> {
                            BlockState st0 = serverLevel.getBlockState(pos);
                            if (!isMobInteractableDoor(st0)) return;
                            BlockPos canon = normalizeDoorPos(serverLevel, pos, st0);
                            // If normalization failed for a TripleDoorBlock, expand the
                            // search to the surrounding 3x3 area (current Y and one
                            // block below) to find the canonical middle-lower block.
                            if (canon == null && st0.getBlock() instanceof TripleDoorBlock) {
                                try {
                                    for (int dx = -1; dx <= 1 && canon == null; dx++) {
                                        for (int dz = -1; dz <= 1 && canon == null; dz++) {
                                            for (int dy = 0; dy >= -1 && canon == null; dy--) {
                                                BlockPos check = pos.offset(dx, dy, dz);
                                                BlockState s2 = serverLevel.getBlockState(check);
                                                if (s2.getBlock() instanceof TripleDoorBlock) {
                                                    try {
                                                        if (s2.getValue(TripleDoorBlock.PART) == TripleDoorBlock.Part.MIDDLE && s2.getValue(TripleDoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                                                            canon = check;
                                                        }
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    }
                                    // debug logs removed
                                } catch (Exception ignored) {}
                            }
                            if (canon == null) return;
                            if (!openedThisTick.add(canon)) return; // already attempted this canonical door this tick
                            BlockState st = serverLevel.getBlockState(canon);
                            boolean wasOpen = isOpenAt(serverLevel, canon, st);
                            if (!wasOpen) {
                                // debug logs removed
                                setOpenGeneric(living, serverLevel, st, canon, true);
                                // Verify state after open attempt
                                BlockState stAfter = serverLevel.getBlockState(canon);
                                boolean nowOpen = isOpenAt(serverLevel, canon, stAfter);
                                // debug logs removed
                            }
                            // Only remember to close if this canonical position matches the canonical prev/next
                            if (allowRemember && (canon.equals(canonicalPrev) || canon.equals(canonicalNext))) {
                                // debug logs removed
                                doorsToCloseRef[0] = rememberDoorToClose(doorsToCloseMem, doorsToCloseRef[0], serverLevel, canon);
                            }
                        };

                        BlockPos prevPos = prev.asBlockPos();
                        consider.accept(prevPos, true);
                        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                            consider.accept(prevPos.relative(d), false);
                        }
                        consider.accept(prevPos.above(), false);
                        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                            consider.accept(prevPos.above().relative(d), false);
                        }

                        BlockPos nextPos = next.asBlockPos();
                        consider.accept(nextPos, true);
                        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                            consider.accept(nextPos.relative(d), false);
                        }
                        consider.accept(nextPos.above(), false);
                        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                            consider.accept(nextPos.above().relative(d), false);
                        }

                        doorsToCloseRef[0].ifPresent(set -> InteractWithDoorMixin.closeDoorsThatIHaveOpenedOrPassedThrough(serverLevel, living, prev, next, set, instance.tryGet(nearestMem)));
                        return true;
                    } else {
                        return false;
                    }
                })
        );
    }

    @Overwrite
    public static void closeDoorsThatIHaveOpenedOrPassedThrough(
            ServerLevel level,
            LivingEntity entity,
            @Nullable Node previous,
            @Nullable Node next,
            Set<GlobalPos> doorPositions,
            Optional<List<LivingEntity>> nearestLivingEntities
    ) {
        // Compute canonical control positions near the path nodes so we don't close the very doors we just opened
        java.util.function.Function<BlockPos, BlockPos> findCanonicalNear = (p) -> {
            // check p and its 4-neighbors for a door, return its canonical control position
            BlockPos[] checks = new BlockPos[]{ p, p.north(), p.south(), p.east(), p.west(), p.above(), p.above().north(), p.above().south(), p.above().east(), p.above().west() };
            for (BlockPos c : checks) {
                BlockState s = level.getBlockState(c);
                if (isMobInteractableDoor(s)) {
                    BlockPos canon = normalizeDoorPos(level, c, s);
                    if (canon != null) return canon;
                }
            }
            return null;
        };

        BlockPos canonicalPrev = previous != null ? findCanonicalNear.apply(previous.asBlockPos()) : null;
        BlockPos canonicalNext = next != null ? findCanonicalNear.apply(next.asBlockPos()) : null;

        Iterator<GlobalPos> iterator = doorPositions.iterator();
        while (iterator.hasNext()) {
            GlobalPos g = iterator.next();
            BlockPos doorPos = g.pos();
            // Skip closing if this door is the canonical one for either the previous or next node
            boolean isCurrent = (canonicalPrev != null && canonicalPrev.equals(doorPos)) || (canonicalNext != null && canonicalNext.equals(doorPos));
                    if (!isCurrent) {
                if (g.dimension() != level.dimension() || !doorPos.closerToCenterThan(entity.position(), 3.0)) {
                    iterator.remove();
                        } else {
                            BlockState state = level.getBlockState(doorPos);
                    if (!isMobInteractableDoor(state)) {
                        iterator.remove();
                    } else {
                        if (!isOpenGeneric(state)) {
                            iterator.remove();
                        } else if (areOtherMobsComingThroughDoor(entity, doorPos, nearestLivingEntities)) {
                            iterator.remove();
                            } else {
                                // debug logs removed
                                setOpenGeneric(entity, level, state, doorPos, false);
                                iterator.remove();
                            }
                    }
                }
            }
        }
    }

    private static boolean areOtherMobsComingThroughDoor(LivingEntity entity, BlockPos pos, Optional<List<LivingEntity>> nearest) {
        if (nearest.isEmpty()) return false;
        return nearest.get().stream()
                .filter(other -> other.getType() == entity.getType())
                .filter(other -> pos.closerToCenterThan(other.position(), 2.0))
                .anyMatch(other -> isMobComingThroughDoor(other.getBrain(), pos));
    }

    private static boolean isMobComingThroughDoor(Brain<?> brain, BlockPos pos) {
        if (!brain.hasMemoryValue(MemoryModuleType.PATH)) return false;
        Path path = brain.getMemory(MemoryModuleType.PATH).get();
        if (path.isDone()) return false;
        Node prev = path.getPreviousNode();
        if (prev == null) return false;
        Node next = path.getNextNode();
        return pos.equals(prev.asBlockPos()) || pos.equals(next.asBlockPos());
    }
}
