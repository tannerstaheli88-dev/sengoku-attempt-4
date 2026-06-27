package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
// BooleanProperty import not required for WIDE removal
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class DoubleDoorBlock extends Block {
    public static final MapCodec<DoubleDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoubleDoorBlock::type), propertiesCodec()).apply(instance, DoubleDoorBlock::new)
    );
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty SIDE = BooleanProperty.create("side"); // false=left column, true=right column
    // WIDE property removed — this block is single-column (no auto-fusion)
    protected static final float AABB_DOOR_THICKNESS = 3.0F;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
    private final BlockSetType type;

    @Override
    public MapCodec<? extends DoubleDoorBlock> codec() {
        return CODEC;
    }

    public DoubleDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
        super(properties.sound(type.soundType()));
        this.type = type;
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(POWERED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SIDE, false)
        );
    }

    public BlockSetType type() {
        return this.type;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean bl = !(Boolean)state.getValue(OPEN);
        boolean bl2 = state.getValue(HINGE) == DoorHingeSide.RIGHT;

        return switch (direction) {
            case SOUTH -> bl ? SOUTH_AABB : (bl2 ? EAST_AABB : WEST_AABB);
            case WEST -> bl ? WEST_AABB : (bl2 ? SOUTH_AABB : NORTH_AABB);
            case NORTH -> bl ? NORTH_AABB : (bl2 ? WEST_AABB : EAST_AABB);
            default -> bl ? EAST_AABB : (bl2 ? NORTH_AABB : SOUTH_AABB);
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // When open, no collision for anyone (players and mobs can pass freely)
        if (this.isOpen(state)) {
            return Shapes.empty();
        }
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
        if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP)) {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        } else {
            return neighborState.getBlock() instanceof DoubleDoorBlock && neighborState.getValue(HALF) != doubleBlockHalf
                ? neighborState.setValue(HALF, doubleBlockHalf)
                : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks() && state.getValue(HALF) == DoubleBlockHalf.LOWER && this.type.canOpenByWindCharge() && !(Boolean)state.getValue(POWERED)) {
            this.setOpen(null, level, state, pos, !this.isOpen(state));
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> state.getValue(OPEN);
            case WATER -> false;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        // For a 2x2 wide door we need to ensure the adjacent column on the hinge-opposite side is replaceable
        Direction facing = context.getHorizontalDirection();
        DoorHingeSide hinge = this.getHinge(context);
        Direction otherSide = hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos otherLowerPos = blockPos.relative(otherSide);
        BlockPos otherUpperPos = otherLowerPos.above();
        if (blockPos.getY() < level.getMaxBuildHeight() - 1
            && level.getBlockState(blockPos.above()).canBeReplaced(context)
            && level.getBlockState(otherLowerPos).canBeReplaced(context)
            && level.getBlockState(otherUpperPos).canBeReplaced(context)) {
            // Prevent placement next to any existing door to avoid accidental fusion
            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();
            BlockPos leftPos = blockPos.relative(left);
            BlockPos rightPos = blockPos.relative(right);
            if (level.getBlockState(leftPos).getBlock() instanceof DoorBlock || level.getBlockState(rightPos).getBlock() instanceof DoorBlock) {
                return null;
            }
            boolean powered = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
            // Determine side column: we want the player-placed column (the hinge side)
            // to be marked as SIDE=false so resourcepacks can treat SIDE=true as the extension.
            // Set the placed column's SIDE to false; the paired column will be set to the opposite when created.
            boolean side = false; // placed column is SIDE=false (hinge/placed)
            return this.defaultBlockState()
            .setValue(FACING, facing)
            .setValue(HINGE, hinge)
            .setValue(POWERED, powered)
            .setValue(OPEN, powered)
            .setValue(HALF, DoubleBlockHalf.LOWER)
            .setValue(SIDE, side);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // Place the upper half for this column
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);

    // Attempt to place the paired column on the opposite side of the hinge to form a 2x2 door
        BlockPos otherLower = this.computeOtherLower(pos, state);
        BlockPos otherUpper = otherLower.above();
        // Only place the other column if both lower and upper positions are replaceable/empty
        if (level.isEmptyBlock(otherLower) && level.isEmptyBlock(otherUpper)) {
            // Create the other column state: same facing/powered/open but opposite SIDE.
            // Use the SAME hinge side so both panels move toward the hinge together (don't split apart).
            DoorHingeSide sameHinge = state.getValue(HINGE);
            BlockState otherLowerState = state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(SIDE, !state.getValue(SIDE)).setValue(HINGE, sameHinge);
            BlockState otherUpperState = otherLowerState.setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(otherLower, otherLowerState, 3);
            level.setBlock(otherUpper, otherUpperState, 3);
        }
    }

    /**
     * Compute the deterministic paired lower position based on hinge and facing.
     */
    private BlockPos computeOtherLower(BlockPos lower, BlockState lowerState) {
        // Combine SIDE and HINGE to deterministically compute the paired column.
        // If this block is the placed column (SIDE == false), the paired column should be
        // on the hinge-opposite side (same rule used by getStateForPlacement). If this block
        // is the extension (SIDE == true), compute the partner symmetrically so computeOtherLower(A)->B and computeOtherLower(B)->A.
        Direction facing = lowerState.getValue(FACING);
        DoorHingeSide hinge = lowerState.getValue(HINGE);
        if (!lowerState.getValue(SIDE)) {
            // Placed column: partner is on hinge-opposite side
            Direction otherSide = hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
            return lower.relative(otherSide);
        } else {
            // Extension column: partner is on hinge-side (inverse of above)
            Direction otherSide = hinge == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
            return lower.relative(otherSide);
        }
    }

    @Nullable
    private BlockPos findPairedLower(Level level, BlockPos lower, BlockState lowerState) {
        if (!(lowerState.getBlock() instanceof DoubleDoorBlock)) {
            return null;
        }
        BlockPos expected = computeOtherLower(lower, lowerState);
        BlockState s = level.getBlockState(expected);
        if (s.getBlock() instanceof DoubleDoorBlock && s.getValue(HALF) == DoubleBlockHalf.LOWER) {
            // Accept as the pair only if it's the opposite SIDE (left vs right) and the hinge matches.
            if (s.getValue(SIDE) != lowerState.getValue(SIDE) && s.getValue(HINGE) == lowerState.getValue(HINGE)) {
                return expected;
            }
        }
        return null;
    }

    /**
     * Find the paired (other) column lower-block position for the given lower column.
     * This scans both horizontal sides relative to the facing and prefers a neighbor that has WIDE=true.
     * Returns null if no paired column is present.
     */
    // Paired-column / fusion logic removed — this block behaves as a single-column door

    private DoorHingeSide getHinge(BlockPlaceContext context) {
        BlockGetter blockGetter = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockPos2 = blockPos.above();
        Direction direction2 = direction.getCounterClockWise();
        BlockPos blockPos3 = blockPos.relative(direction2);
        BlockState blockState = blockGetter.getBlockState(blockPos3);
        BlockPos blockPos4 = blockPos2.relative(direction2);
        BlockState blockState2 = blockGetter.getBlockState(blockPos4);
        Direction direction3 = direction.getClockWise();
        BlockPos blockPos5 = blockPos.relative(direction3);
        BlockState blockState3 = blockGetter.getBlockState(blockPos5);
        BlockPos blockPos6 = blockPos2.relative(direction3);
        BlockState blockState4 = blockGetter.getBlockState(blockPos6);
        int i = (blockState.isCollisionShapeFullBlock(blockGetter, blockPos3) ? -1 : 0)
            + (blockState2.isCollisionShapeFullBlock(blockGetter, blockPos4) ? -1 : 0)
            + (blockState3.isCollisionShapeFullBlock(blockGetter, blockPos5) ? 1 : 0)
            + (blockState4.isCollisionShapeFullBlock(blockGetter, blockPos6) ? 1 : 0);
        boolean bl = blockState.getBlock() instanceof DoubleDoorBlock && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean bl2 = blockState3.getBlock() instanceof DoubleDoorBlock && blockState3.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((!bl || bl2) && i <= 0) {
            if ((!bl2 || bl) && i >= 0) {
                int j = direction.getStepX();
                int k = direction.getStepZ();
                Vec3 vec3 = context.getClickLocation();
                double d = vec3.x - blockPos.getX();
                double e = vec3.z - blockPos.getZ();
                return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            } else {
                return DoorHingeSide.LEFT;
            }
        } else {
            return DoorHingeSide.RIGHT;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!this.type.canOpenByHand()) {
            return InteractionResult.PASS;
        } else {
            boolean open = !this.isOpen(state);
            this.setOpen(player, level, state, pos, open);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    public boolean isOpen(BlockState state) {
        return (Boolean)state.getValue(OPEN);
    }

    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this) || (Boolean)state.getValue(OPEN) == open) {
            return;
        }

        // Determine the lower half of this column
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();

        // Update this column (lower + upper)
        BlockState lowerState = level.getBlockState(lower);
        if (lowerState.getBlock() instanceof DoubleDoorBlock) {
            level.setBlock(lower, lowerState.setValue(OPEN, open), 10);
        }
        BlockPos upper = lower.above();
        BlockState upperState = level.getBlockState(upper);
        if (upperState.getBlock() instanceof DoubleDoorBlock) {
            level.setBlock(upper, upperState.setValue(OPEN, open), 10);
        }

        // Re-fetch the lower state after updating so we use the authoritative, current state
        BlockState currentLowerState = level.getBlockState(lower);

        // Update paired column if present
        BlockPos otherLower = findPairedLower(level, lower, currentLowerState);
        if (otherLower != null) {
            BlockPos otherUpper = otherLower.above();
            BlockState otherLowerState = level.getBlockState(otherLower);
            if (otherLowerState.getBlock() instanceof DoubleDoorBlock) {
                // Ensure paired column keeps the same hinge and opposite SIDE so the whole frame moves toward the hinge
                DoorHingeSide sameHinge = currentLowerState.getValue(HINGE);
                BlockState newOtherLower = otherLowerState
                    .setValue(HINGE, sameHinge)
                    .setValue(FACING, currentLowerState.getValue(FACING))
                    .setValue(SIDE, !currentLowerState.getValue(SIDE))
                    .setValue(OPEN, open);
                level.setBlock(otherLower, newOtherLower, 10);
            }
            BlockState otherUpperState = level.getBlockState(otherUpper);
            if (otherUpperState.getBlock() instanceof DoubleDoorBlock) {
                DoorHingeSide sameHinge = currentLowerState.getValue(HINGE);
                BlockState newOtherUpper = otherUpperState
                    .setValue(HINGE, sameHinge)
                    .setValue(FACING, currentLowerState.getValue(FACING))
                    .setValue(SIDE, !currentLowerState.getValue(SIDE))
                    .setValue(OPEN, open);
                level.setBlock(otherUpper, newOtherUpper, 10);
            }
        }

        // One sound/event for the whole frame
        this.playSound(entity, level, lower, open);
        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, lower);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // When a part is destroyed, also remove the corresponding parts in the other column
        if (!level.isClientSide) {
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            // Remove this column's upper/lower as appropriate
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                level.setBlock(lower.above(), Blocks.AIR.defaultBlockState(), 35);
            } else {
                level.setBlock(lower, Blocks.AIR.defaultBlockState(), 35);
            }

            // Remove paired column if present
            BlockPos otherLower = findPairedLower(level, lower, state);
            if (otherLower != null) {
                BlockPos otherUpper = otherLower.above();
                BlockState otherLowerState = level.getBlockState(otherLower);
                if (otherLowerState.getBlock() instanceof DoubleDoorBlock) {
                    level.setBlock(otherUpper, Blocks.AIR.defaultBlockState(), 35);
                    level.setBlock(otherLower, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        // First: ensure the whole frame is removed if either supporting block (under either lower) is gone
        if (!level.isClientSide) {
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockState lowerState = level.getBlockState(lower);
            if (lowerState.getBlock() instanceof DoubleDoorBlock) {
                boolean thisSupport = isSupportIntact(level, lower);
                BlockPos otherLower = findPairedLower(level, lower, lowerState);
                boolean otherSupport = otherLower == null || isSupportIntact(level, otherLower);
                if (!thisSupport || !otherSupport) {
                    breakWholeDoor(level, lower, otherLower);
                    return; // stop further processing; the frame was removed
                }
            }
        }

        boolean bl = level.hasNeighborSignal(pos)
            || level.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (!this.defaultBlockState().is(neighborBlock) && bl != (Boolean)state.getValue(POWERED)) {
            // Operate on lower half of this column
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();

            if (bl != (Boolean)state.getValue(OPEN)) {
                this.playSound(null, level, lower, bl);
                level.gameEvent(null, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, lower);
            }

            // Update this column (lower + upper) only
            BlockState lowerState = level.getBlockState(lower);
            if (lowerState.getBlock() instanceof DoubleDoorBlock) {
                level.setBlock(lower, lowerState.setValue(POWERED, bl).setValue(OPEN, bl), 2);
            }
            BlockPos upper = lower.above();
            BlockState upperState = level.getBlockState(upper);
            if (upperState.getBlock() instanceof DoubleDoorBlock) {
                level.setBlock(upper, upperState.setValue(POWERED, bl).setValue(OPEN, bl), 2);
            }
            // synchronize paired column if present
            BlockPos otherLower = findPairedLower(level, lower, lowerState);
            if (otherLower != null) {
                BlockPos otherUpper = otherLower.above();
                BlockState otherLowerState = level.getBlockState(otherLower);
                if (otherLowerState.getBlock() instanceof DoubleDoorBlock) {
                    level.setBlock(otherLower, otherLowerState.setValue(POWERED, bl).setValue(OPEN, bl), 2);
                }
                BlockState otherUpperState = level.getBlockState(otherUpper);
                if (otherUpperState.getBlock() instanceof DoubleDoorBlock) {
                    level.setBlock(otherUpper, otherUpperState.setValue(POWERED, bl).setValue(OPEN, bl), 2);
                }
            }
        }
    }

    /** Returns true if the block directly beneath the given lower position is sturdy on top. */
    private boolean isSupportIntact(LevelReader level, BlockPos lower) {
        BlockPos below = lower.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    /** Removes the entire 2x2 double door frame. Drops from one lower block only to avoid duplicate drops. */
    private void breakWholeDoor(Level level, BlockPos lower, @Nullable BlockPos otherLower) {
        // Identify which lower should drop: prefer SIDE=false if present
        BlockPos dropLower = lower;
        BlockState lowerState = level.getBlockState(lower);
        if (lowerState.getBlock() instanceof DoubleDoorBlock) {
            boolean thisIsPlacedSide = !lowerState.getValue(SIDE);
            if (!thisIsPlacedSide && otherLower != null) {
                BlockState otherState = level.getBlockState(otherLower);
                if (otherState.getBlock() instanceof DoubleDoorBlock && !otherState.getValue(SIDE)) {
                    dropLower = otherLower;
                }
            }
        }

        // Collect all door block positions to clear
        BlockPos[] toClear;
        if (otherLower != null) {
            toClear = new BlockPos[] { lower, lower.above(), otherLower, otherLower.above() };
        } else {
            toClear = new BlockPos[] { lower, lower.above() };
        }

        // Destroy the chosen lower with drops, clear the rest without drops
        if (level.getBlockState(dropLower).getBlock() instanceof DoubleDoorBlock) {
            level.destroyBlock(dropLower, true);
        }
        for (BlockPos p : toClear) {
            if (p.equals(dropLower)) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof DoubleDoorBlock) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        // Use the incoming state for the lower half if this state is the lower half (placement time),
        // otherwise read the existing lower block from the world.
        BlockState lowerColumnState = state.getValue(HALF) == DoubleBlockHalf.LOWER ? state : level.getBlockState(lower);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            // Single-column: ensure the block directly below the lower half is sturdy
            BlockPos below = lower.below();
            BlockState belowState = level.getBlockState(below);
            return belowState.isFaceSturdy(level, below, Direction.UP);
        } else {
            // Upper half survives if the lower half is present and matches
            return lowerColumnState.getBlock() instanceof DoubleDoorBlock && lowerColumnState.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    private void playSound(@Nullable Entity source, Level level, BlockPos pos, boolean isOpening) {
        level.playSound(source, pos, isOpening ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        // Mirror rotates facing and swaps hinge; also flip SIDE so left/right columns swap
        BlockState rotated = state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
        return rotated.setValue(SIDE, !rotated.getValue(SIDE));
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), pos.below(state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN, HINGE, POWERED, SIDE);
    }

    public static boolean isWoodenDoor(Level level, BlockPos pos) {
        return isWoodenDoor(level.getBlockState(pos));
    }

    public static boolean isWoodenDoor(BlockState state) {
        if (state.getBlock() instanceof DoubleDoorBlock) {
            DoubleDoorBlock doorBlock = (DoubleDoorBlock) state.getBlock();
            return doorBlock.type().canOpenByHand();
        }
        return false;
    }
}
