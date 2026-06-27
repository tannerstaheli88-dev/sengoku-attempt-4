package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import com.shioh.sengoku.util.ShojiProperties;
import net.minecraft.world.level.block.TripleDoorBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ShojiTripleDoorBlock extends TripleDoorBlock {
    private static final double SLIDE_BLOCK = 0.5D;

    public ShojiTripleDoorBlock(BlockSetType setType, BlockBehaviour.Properties properties) {
        super(setType, properties);
        // ensure HANDLE has a default value
        this.registerDefaultState(this.defaultBlockState().setValue(ShojiProperties.HANDLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ShojiProperties.HANDLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // For left/right parts make the pick/outline a full cube so players can hover/click them reliably.
        BlockState actual = state;
        if (state.getValue(TripleDoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            if (lower.getBlock() instanceof ShojiTripleDoorBlock) actual = lower;
        }

        TripleDoorBlock.Part part = actual.getValue(TripleDoorBlock.PART);
        Direction facing = actual.getValue(TripleDoorBlock.FACING);

        // For side parts, return a simple 3-pixel thin panel outline so hovering shows a thin frame
        if (part == TripleDoorBlock.Part.LEFT || part == TripleDoorBlock.Part.RIGHT) {
            return switch (facing) {
                case SOUTH -> SOUTH_AABB;
                case WEST -> WEST_AABB;
                case NORTH -> NORTH_AABB;
                default -> EAST_AABB;
            };
        }

        // Middle part: return the detailed thin-panel visual shape (sliding behavior)
        return buildDetailedShape(actual, level, pos, context);
    }

    /**
     * Build the detailed thin-panel shape used for collisions and the middle-part visual.
     */
    private VoxelShape buildDetailedShape(BlockState actual, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = actual.getValue(TripleDoorBlock.FACING);
        TripleDoorBlock.Part part = actual.getValue(TripleDoorBlock.PART);
        boolean open = actual.getValue(TripleDoorBlock.OPEN);

        // If the door is closed, return the simple thin-panel AABB for physics so
        // closed shoji panels block reliably for all facings.
        if (!open) {
            return switch (facing) {
                case SOUTH -> SOUTH_AABB;
                case WEST -> WEST_AABB;
                case NORTH -> NORTH_AABB;
                default -> EAST_AABB;
            };
        }

        final int DOOR_UNITS = 24;
        final int BLOCK_UNITS = 16;
        final int THICKNESS = 3;
        final int SLIDE_UNITS = 8;

        int leftDoorStart, leftDoorEnd, rightDoorStart, rightDoorEnd;
        // The numeric door intervals are left-to-right in door-local coords. For some facings
        // (south and west) the numeric ordering is reversed relative to world X/Z, so swap
        // the intervals so "left" and "right" mean the same relative side for all facings.
        if (facing == Direction.NORTH || facing == Direction.EAST) {
            leftDoorStart = 0; leftDoorEnd = DOOR_UNITS;
            rightDoorStart = DOOR_UNITS; rightDoorEnd = DOOR_UNITS * 2;
        } else {
            leftDoorStart = DOOR_UNITS; leftDoorEnd = DOOR_UNITS * 2;
            rightDoorStart = 0; rightDoorEnd = DOOR_UNITS;
        }
        boolean widthIsX = (facing == Direction.NORTH || facing == Direction.SOUTH);

        List<int[]> slices = new ArrayList<>();
        int blockIndex;
        // Map logical parts to numeric block indices depending on facing.
        // For NORTH/EAST: LEFT=0, MIDDLE=1, RIGHT=2. For SOUTH/WEST the numeric
        // ordering is reversed so LEFT should map to 2 and RIGHT to 0.
        if (facing == Direction.NORTH || facing == Direction.EAST) {
            switch (part) {
                case LEFT -> blockIndex = 0;
                case MIDDLE -> blockIndex = 1;
                default -> blockIndex = 2;
            }
        } else {
            switch (part) {
                case LEFT -> blockIndex = 2;
                case MIDDLE -> blockIndex = 1;
                default -> blockIndex = 0;
            }
        }
        int blockGlobalStart = blockIndex * BLOCK_UNITS;
        int blockGlobalEnd = blockGlobalStart + BLOCK_UNITS;

        BiConsumer<Integer, Integer> addIntersection = (ds, de) -> {
            int s = Math.max(ds, blockGlobalStart);
            int e = Math.min(de, blockGlobalEnd);
            if (e > s) slices.add(new int[]{s - blockGlobalStart, e - blockGlobalStart});
        };

        if (part == TripleDoorBlock.Part.LEFT) {
            addIntersection.accept(leftDoorStart, leftDoorEnd);
        } else if (part == TripleDoorBlock.Part.RIGHT) {
            addIntersection.accept(rightDoorStart, rightDoorEnd);
        } else {
            // Middle block: split into left/right slices
            int middleStart = blockGlobalStart;
            int middleEnd = blockGlobalEnd;

            int leftS = Math.max(leftDoorStart, middleStart);
            int leftE = Math.min(leftDoorEnd, middleEnd);
            if (leftE > leftS) slices.add(new int[]{leftS - middleStart, leftE - middleStart});

            int rightS = Math.max(rightDoorStart, middleStart);
            int rightE = Math.min(rightDoorEnd, middleEnd);
            if (rightE > rightS) slices.add(new int[]{rightS - middleStart, rightE - middleStart});
        }

        double y0 = 0.0D, y1 = 16.0D;
        VoxelShape result = Shapes.empty();

        for (int[] seg : slices) {
            int localStart = seg[0], localEnd = seg[1];
            int globalSegStart = blockGlobalStart + localStart;
            int globalSegEnd = blockGlobalStart + localEnd;

            int slideX = 0, slideZ = 0;
            boolean isLeftSlice = globalSegStart < leftDoorEnd && globalSegEnd > leftDoorStart;
            boolean isRightSlice = globalSegStart < rightDoorEnd && globalSegEnd > rightDoorStart;

            if (part == TripleDoorBlock.Part.LEFT && open) {
                Direction leftDir = facing.getCounterClockWise();
                slideX += leftDir.getStepX() * SLIDE_UNITS;
                slideZ += leftDir.getStepZ() * SLIDE_UNITS;
            } else if (part == TripleDoorBlock.Part.RIGHT && open) {
                Direction rightDir = facing.getClockWise();
                slideX += rightDir.getStepX() * SLIDE_UNITS;
                slideZ += rightDir.getStepZ() * SLIDE_UNITS;
            } else if (part == TripleDoorBlock.Part.MIDDLE) {
                if (isLeftSlice && open) {
                    Direction leftDir = facing.getCounterClockWise();
                    slideX += leftDir.getStepX() * SLIDE_UNITS;
                    slideZ += leftDir.getStepZ() * SLIDE_UNITS;
                }
                if (isRightSlice && open) {
                    Direction rightDir = facing.getClockWise();
                    slideX += rightDir.getStepX() * SLIDE_UNITS;
                    slideZ += rightDir.getStepZ() * SLIDE_UNITS;
                }
            }

            if (widthIsX) {
                double minX = localStart + slideX;
                double maxX = localEnd + slideX;
                double minY = y0, maxY = y1;
                double minZ = (facing == Direction.NORTH ? 13.0D : 0.0D) + (open && (isLeftSlice || isRightSlice) ? slideZ : 0);
                double maxZ = (facing == Direction.NORTH ? 16.0D : THICKNESS) + (open && (isLeftSlice || isRightSlice) ? slideZ : 0);
                result = Shapes.or(result, Shapes.box(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0));
            } else {
                double minZ = localStart + slideZ;
                double maxZ = localEnd + slideZ;
                double minY = y0, maxY = y1;
                double minX = (facing == Direction.WEST ? 13.0D : 0.0D) + (open && (isLeftSlice || isRightSlice) ? slideX : 0);
                double maxX = (facing == Direction.WEST ? 16.0D : THICKNESS) + (open && (isLeftSlice || isRightSlice) ? slideX : 0);
                result = Shapes.or(result, Shapes.box(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0));
            }
        }

        return result;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.type().canOpenByHand()) return InteractionResult.PASS;

        boolean leftSide;
        TripleDoorBlock.Part part = state.getValue(TripleDoorBlock.PART);
        if (part == TripleDoorBlock.Part.LEFT) leftSide = true;
        else if (part == TripleDoorBlock.Part.RIGHT) leftSide = false;
        else {
            Vec3 loc = hit.getLocation();
            double rel;
            Direction facing = state.getValue(TripleDoorBlock.FACING);
            if (facing.getAxis() == Direction.Axis.X) rel = loc.z - pos.getZ();
            else rel = loc.x - pos.getX();
            Direction leftDir = facing.getCounterClockWise();
            boolean leftIsNegative = (leftDir == Direction.WEST || leftDir == Direction.NORTH);
            leftSide = leftIsNegative ? rel < 0.5D : rel > 0.5D;
        }

        BlockPos lower = state.getValue(TripleDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lower);
        boolean currentlyOpen = lowerState.getValue(TripleDoorBlock.OPEN);

        this.setOpenAll(player, level, pos, state, !currentlyOpen);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // When open, no collision for anyone (players and mobs can pass freely)
        BlockState actual = state;
        if (state.getValue(TripleDoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            if (lower.getBlock() instanceof ShojiTripleDoorBlock) actual = lower;
        }
        if (actual.getValue(TripleDoorBlock.OPEN)) {
            return Shapes.empty();
        }
        // Closed: use the detailed thin-panel shape
        return buildDetailedShape(actual, level, pos, context);
    }

    @Override
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open, boolean leftSide) {
        this.setOpenAll(entity, level, pos, state, open);
    }

    // Some AI code calls the 5-argument setOpen signature (Entity, Level, BlockState, BlockPos, boolean).
    // Provide a compatibility method that delegates to setOpenAll so AI can open triple doors
    // using the simpler call when present in the runtime. Do not use @Override because the
    // signature isn't declared on the current superclass in all mappings/versions.
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        this.setOpenAll(entity, level, pos, state, open);
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        // Villagers and other pathfinding mobs expect doors to be pathfindable when open.
            switch (type) {
                case LAND:
                case AIR:
                    return state.getValue(TripleDoorBlock.OPEN);
                case WATER:
                default:
                    return false;
            }
    }
}