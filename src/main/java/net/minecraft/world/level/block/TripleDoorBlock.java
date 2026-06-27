package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class TripleDoorBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty OPEN_LEFT = BooleanProperty.create("open_left");
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

    private final BlockSetType type;

    public TripleDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
        super(properties.sound(type.soundType()));
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(PART, Part.MIDDLE)
                .setValue(POWERED, false)
                .setValue(OPEN, false)
                .setValue(OPEN_LEFT, false));
    }

    public BlockSetType type() { return this.type; }

    public enum Part implements net.minecraft.util.StringRepresentable {
        LEFT("left"), MIDDLE("middle"), RIGHT("right");
        private final String name;
        Part(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    protected static final float AABB_DOOR_THICKNESS = 3.0F;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape FULL_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(HALF, FACING, PART, POWERED, OPEN, OPEN_LEFT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection();

        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        BlockPos leftLower = pos.relative(left);
        BlockPos rightLower = pos.relative(right);
        BlockPos above = pos.above();
        BlockPos leftUpper = leftLower.above();
        BlockPos rightUpper = rightLower.above();

        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(above).canBeReplaced(context)
                && level.getBlockState(leftLower).canBeReplaced(context)
                && level.getBlockState(leftUpper).canBeReplaced(context)
                && level.getBlockState(rightLower).canBeReplaced(context)
                && level.getBlockState(rightUpper).canBeReplaced(context)) {

                boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(above);
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(POWERED, powered);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Middle upper
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);

        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        BlockPos leftLower = pos.relative(left);
        BlockPos leftUpper = leftLower.above();
        BlockPos rightLower = pos.relative(right);
        BlockPos rightUpper = rightLower.above();

        BlockState leftLowerState = this.defaultBlockState().setValue(FACING, facing).setValue(HALF, DoubleBlockHalf.LOWER).setValue(PART, Part.LEFT).setValue(POWERED, state.getValue(POWERED));
        BlockState leftUpperState = leftLowerState.setValue(HALF, DoubleBlockHalf.UPPER);
        BlockState rightLowerState = this.defaultBlockState().setValue(FACING, facing).setValue(HALF, DoubleBlockHalf.LOWER).setValue(PART, Part.RIGHT).setValue(POWERED, state.getValue(POWERED));
        BlockState rightUpperState = rightLowerState.setValue(HALF, DoubleBlockHalf.UPPER);

        if (level.isEmptyBlock(leftLower) && level.isEmptyBlock(leftUpper)) {
            level.setBlock(leftLower, leftLowerState, 3);
            level.setBlock(leftUpper, leftUpperState, 3);
        }
        if (level.isEmptyBlock(rightLower) && level.isEmptyBlock(rightUpper)) {
            level.setBlock(rightLower, rightLowerState, 3);
            level.setBlock(rightUpper, rightUpperState, 3);
        }
    }

    /** Toggles the entire triple door open/closed */
    public void setOpenAll(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, boolean open) {
        if (!state.is(this)) return;
        // Resolve canonical middle-lower block; if missing, abort to avoid NPE/crash.
        BlockPos middleLower = findMiddleLower(level, pos, state);
        if (middleLower == null) return;

        Direction facing = level.getBlockState(middleLower).getValue(FACING);
        BlockPos leftLower = middleLower.relative(facing.getCounterClockWise());
        BlockPos rightLower = middleLower.relative(facing.getClockWise());

        // Update all six blocks
        updatePart(level, middleLower, open);
        updatePart(level, middleLower.above(), open);
        updatePart(level, leftLower, open);
        updatePart(level, leftLower.above(), open);
        updatePart(level, rightLower, open);
        updatePart(level, rightLower.above(), open);

        this.playSound(entity, level, middleLower, open);
        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, middleLower);
    }

    /**
     * Find the canonical middle-lower BlockPos for this triple door relative to the provided pos/state.
     * Returns null if a valid middle-lower block cannot be located.
     */
    @Nullable
    private BlockPos findMiddleLower(Level level, BlockPos pos, BlockState state) {
        BlockPos candidate = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState candidateState = level.getBlockState(candidate);
        if (candidateState.getBlock() instanceof TripleDoorBlock && candidateState.getValue(PART) == Part.MIDDLE && candidateState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return candidate;
        }

        // search 4 orthogonal neighbors for the middle-lower block
        for (Direction d : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST }) {
            BlockPos p = candidate.relative(d);
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof TripleDoorBlock && bs.getValue(PART) == Part.MIDDLE && bs.getValue(HALF) == DoubleBlockHalf.LOWER) {
                return p;
            }
        }

        return null;
    }

    private void updatePart(Level level, BlockPos pos, boolean open) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TripleDoorBlock) {
            level.setBlock(pos, state.setValue(OPEN, open).setValue(OPEN_LEFT, open), 10);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // Use the same thin-panel shape as collision for the outline so the visual
        // hitbox matches the actual door frame (3px thickness) rather than a full cube.
        return this.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // When open, no collision for anyone (players and mobs can pass freely)
        BlockState actualForCollision = state;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = world.getBlockState(pos.below());
            if (lower.getBlock() instanceof TripleDoorBlock) actualForCollision = lower;
        }
        if (actualForCollision.getValue(OPEN)) {
            return Shapes.empty();
        }
        // Resolve to the lower-half block state for consistent facing/part/open values
        BlockState actual = state;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = world.getBlockState(pos.below());
            if (lower.getBlock() instanceof TripleDoorBlock) actual = lower;
        }

        Direction direction = actual.getValue(FACING);
        boolean closed = !actual.getValue(OPEN);
        Part part = actual.getValue(PART);

        // When closed, all parts use the same thin panel collision based on facing
        if (closed) {
            return switch (direction) {
                case SOUTH -> SOUTH_AABB;
                case WEST -> WEST_AABB;
                case NORTH -> NORTH_AABB;
                default -> EAST_AABB; // EAST
            };
        }

        // When open, choose collision per-part: side parts occupy the AABB in the
        // direction they swung to (left -> counter-clockwise, right -> clockwise).
        Direction leftDir = direction.getCounterClockWise();
        Direction rightDir = direction.getClockWise();

        Direction target = switch (part) {
            case LEFT -> leftDir;
            case RIGHT -> rightDir;
            default -> null;
        };

        if (part == Part.MIDDLE) {
            // Middle slides out of the way when open (no blocking collision)
            return Block.box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        // Map the target direction to the corresponding thin AABB
        return switch (target) {
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case NORTH -> NORTH_AABB;
            default -> EAST_AABB;
        };
    }

    private void playSound(@Nullable Entity source, Level level, BlockPos pos, boolean isOpening) {
        level.playSound(source, pos, isOpening ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.type.canOpenByHand()) return InteractionResult.PASS;

        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lower);
        boolean currentlyOpen = lowerState.getValue(OPEN);

        setOpenAll(player, level, pos, state, !currentlyOpen);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Default per-side open handler. Subclasses may override to implement per-side behavior.
     */
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open, boolean leftSide) {
        // Default behavior: toggle entire triple door
        this.setOpenAll(entity, level, pos, state, open);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos below = lower.below();
            BlockState belowState = level.getBlockState(below);
            return belowState.isFaceSturdy(level, below, Direction.UP);
        } else {
            BlockState lowerState = state.getValue(HALF) == DoubleBlockHalf.LOWER ? state : level.getBlockState(lower);
            return lowerState.getBlock() instanceof TripleDoorBlock && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            case WATER -> false;
        };
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        // Whenever neighbors change, ensure the open state is synchronized across the triple door
        syncOpenState(level, pos, state);
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    /**
     * Ensure open flags (left/right) are identical across left/middle/right lower+upper blocks.
    * If any part reports open, set the unified OPEN (and compatibility OPEN_LEFT) on all parts; otherwise clear them.
     */
    private void syncOpenState(Level level, BlockPos pos, BlockState state) {
        if (!state.is(this)) return;
        // Resolve canonical middle-lower block; if missing, abort to avoid NPE/crash.
        BlockPos middleLower = findMiddleLower(level, pos, state);
        if (middleLower == null) return;
        Direction facing = level.getBlockState(middleLower).getValue(FACING);

        BlockPos leftLower = middleLower.relative(facing.getCounterClockWise());
        BlockPos rightLower = middleLower.relative(facing.getClockWise());

        BlockState middleLowerState = level.getBlockState(middleLower);
        BlockState leftLowerState = level.getBlockState(leftLower);
        BlockState rightLowerState = level.getBlockState(rightLower);

    // If any part reports OPEN, treat the whole door as open.
        boolean anyOpen = (middleLowerState.getBlock() instanceof TripleDoorBlock && middleLowerState.getValue(OPEN))
                || (leftLowerState.getBlock() instanceof TripleDoorBlock && leftLowerState.getValue(OPEN))
                || (rightLowerState.getBlock() instanceof TripleDoorBlock && rightLowerState.getValue(OPEN));

    BlockPos[] all = new BlockPos[] { leftLower, leftLower.above(), middleLower, middleLower.above(), rightLower, rightLower.above() };
        for (BlockPos p : all) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof TripleDoorBlock) {
                if (bs.getValue(OPEN) != anyOpen || bs.getValue(OPEN_LEFT) != anyOpen) {
                    level.setBlock(p, bs.setValue(OPEN, anyOpen).setValue(OPEN_LEFT, anyOpen), 10);
                }
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Follow vanilla conventions: if player breaks a block, remove neighboring parts with proper drop handling.
        if (!level.isClientSide) {
            // Only suppress drops when creative or incorrect tool, let super handle creative drop prevention
            BlockPos middleLower = findMiddleLower(level, pos, state);
            if (middleLower == null) return super.playerWillDestroy(level, pos, state, player);

            Direction facing = level.getBlockState(middleLower).getValue(FACING);
            BlockPos leftLower = middleLower.relative(facing.getCounterClockWise());
            BlockPos rightLower = middleLower.relative(facing.getClockWise());

            // Destroy other parts using level.destroyBlock so that drops and events follow vanilla behavior.
            BlockPos[] all = new BlockPos[] { leftLower, leftLower.above(), middleLower, middleLower.above(), rightLower, rightLower.above() };
            for (BlockPos p : all) {
                if (p.equals(pos)) continue; // original pos will be handled by caller
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof TripleDoorBlock) {
                    // destroyBlock(pos, drop) uses the correct drop logic; pass true to drop.
                    level.destroyBlock(p, true);
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // If this block is being replaced by something else (not just a state update) ensure the whole multi-block is cleared
    if (state.getBlock() != newState.getBlock()) {
            if (!level.isClientSide) {
                // Resolve canonical middle lower similar to playerWillDestroy
                BlockPos middleLower = findMiddleLower(level, pos, state);
                if (middleLower == null) return; // nothing to clear

                Direction facing = level.getBlockState(middleLower).getValue(FACING);
                BlockPos leftLower = middleLower.relative(facing.getCounterClockWise());
                BlockPos rightLower = middleLower.relative(facing.getClockWise());

                BlockPos[] all = new BlockPos[] { leftLower, leftLower.above(), middleLower, middleLower.above(), rightLower, rightLower.above() };
                for (BlockPos p : all) {
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof TripleDoorBlock) {
                        level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        // Rotate the facing; parts remain LEFT/MIDDLE/RIGHT relative to the new facing
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        // Mirror rotates the facing via the mirror's rotation and swaps LEFT/RIGHT parts
        BlockState rotated = state.rotate(mirror.getRotation(state.getValue(FACING)));
        Part part = rotated.getValue(PART);
        if (part == Part.LEFT) {
            return rotated.setValue(PART, Part.RIGHT);
        } else if (part == Part.RIGHT) {
            return rotated.setValue(PART, Part.LEFT);
        }
        return rotated; // MIDDLE stays the same
    }
}
