package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;

public abstract class AbstractShikiRakedBlock extends FallingBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    protected AbstractShikiRakedBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(LOCKED, false));
    }

    protected abstract BlockState getBaseState();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, LOCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        Direction facing = player != null ? player.getDirection() : ctx.getHorizontalDirection();
        RailShape initialShape = (facing == Direction.EAST || facing == Direction.WEST)
            ? RailShape.EAST_WEST
            : RailShape.NORTH_SOUTH;
        BlockState placedState = this.defaultBlockState().setValue(SHAPE, initialShape);
        return this.computeShape(ctx.getLevel(), ctx.getClickedPos(), placedState);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (!world.isClientSide) {
            BlockState updated = this.computeShape(world, pos, state);
            if (updated != state) {
                world.setBlock(pos, updated, 3);
            }
            this.refreshNeighbors(world, pos);
        }
        super.onPlace(state, world, pos, oldState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClientSide) {
            BlockState updated = this.computeShape(world, pos, state);
            if (updated != state) {
                world.setBlock(pos, updated, 3);
            }
            this.refreshNeighbors(world, pos);
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClientSide && entity instanceof Player player && !player.isShiftKeyDown()) {
            world.levelEvent(2001, pos, Block.getId(state));
            world.setBlockAndUpdate(pos, this.getBaseState());
        }
        super.stepOn(world, pos, state, entity);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(world, pos, state, player);
        if (!world.isClientSide && !player.isCreative()) {
            this.popResource(world, pos, new ItemStack(this.asItem()));
        }
        return result;
    }

    private void refreshNeighbors(LevelAccessor world, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.getBlock() == this) {
                BlockState updated = this.computeShape(world, neighborPos, neighborState);
                if (updated != neighborState) {
                    world.setBlock(neighborPos, updated, 3);
                }
            }
        }
    }

    private BlockState computeShape(LevelAccessor world, BlockPos pos, BlockState state) {
        RailShape currentShape = state.getValue(SHAPE);
        boolean locked = state.getValue(LOCKED);

        if (locked) {
            return state;
        }

        if (this.isFullyConnected(world, pos, currentShape)) {
            return state;
        }

        RailShape preferredShape = this.computePreferredShape(world, pos, currentShape);
        return state.setValue(SHAPE, preferredShape);
    }

    private RailShape computePreferredShape(LevelAccessor world, BlockPos pos, RailShape fallbackShape) {
        boolean north = this.canConnectTo(world, pos, Direction.NORTH);
        boolean east = this.canConnectTo(world, pos, Direction.EAST);
        boolean south = this.canConnectTo(world, pos, Direction.SOUTH);
        boolean west = this.canConnectTo(world, pos, Direction.WEST);

        return this.computePreferredShapeFromConnections(north, east, south, west, fallbackShape);
    }

    private RailShape computePreferredShapeFromConnections(boolean north, boolean east, boolean south, boolean west, RailShape fallbackShape) {
        RailShape shape = fallbackShape;

        if ((north || south) && !(east || west)) {
            shape = RailShape.NORTH_SOUTH;
        } else if ((east || west) && !(north || south)) {
            shape = RailShape.EAST_WEST;
        } else if (north && east && !south && !west) {
            shape = RailShape.NORTH_EAST;
        } else if (north && west && !south && !east) {
            shape = RailShape.NORTH_WEST;
        } else if (south && east && !north && !west) {
            shape = RailShape.SOUTH_EAST;
        } else if (south && west && !north && !east) {
            shape = RailShape.SOUTH_WEST;
        } else if (north && south) {
            shape = RailShape.NORTH_SOUTH;
        } else if (east && west) {
            shape = RailShape.EAST_WEST;
        } else if (north || south) {
            shape = RailShape.NORTH_SOUTH;
        } else if (east || west) {
            shape = RailShape.EAST_WEST;
        }

        return shape;
    }

    // A valid shiki connection is rail-like: reciprocal endpoint alignment only.
    // Parallel side-adjacent pieces do not connect.
    private boolean canConnectTo(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        if (!this.isSameShikiBlock(world, neighborPos)) {
            return false;
        }

        Direction towardMe = direction.getOpposite();
        Direction[] occupied = this.getOccupiedNeighborDirections(world, neighborPos, pos);

        // Already saturated (or impossible graph) -> no new link.
        if (occupied.length >= 2) {
            return false;
        }

        boolean north = towardMe == Direction.NORTH;
        boolean east = towardMe == Direction.EAST;
        boolean south = towardMe == Direction.SOUTH;
        boolean west = towardMe == Direction.WEST;

        for (Direction occupiedDirection : occupied) {
            north |= occupiedDirection == Direction.NORTH;
            east |= occupiedDirection == Direction.EAST;
            south |= occupiedDirection == Direction.SOUTH;
            west |= occupiedDirection == Direction.WEST;
        }

        RailShape neighborFallbackShape = neighborState.getValue(SHAPE);
        RailShape neighborPreferredShape = this.computePreferredShapeFromConnections(north, east, south, west, neighborFallbackShape);
        return this.containsDirection(this.getConnectors(neighborPreferredShape), towardMe);
    }

    private Direction[] getOccupiedNeighborDirections(LevelAccessor world, BlockPos pos, BlockPos ignorePos) {
        Direction[] occupied = new Direction[4];
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(direction);
            if (adjacent.equals(ignorePos)) {
                continue;
            }
            if (this.hasCurrentConnection(world, pos, direction)) {
                occupied[count++] = direction;
            }
        }

        Direction[] result = new Direction[count];
        for (int i = 0; i < count; i++) {
            result[i] = occupied[i];
        }
        return result;
    }

    private boolean containsDirection(Direction[] connectors, Direction direction) {
        for (Direction connector : connectors) {
            if (connector == direction) {
                return true;
            }
        }
        return false;
    }

    private Direction[] getConnectors(RailShape shape) {
        if (shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST) {
            return new Direction[] { Direction.EAST, Direction.WEST };
        }
        if (shape == RailShape.NORTH_EAST) {
            return new Direction[] { Direction.NORTH, Direction.EAST };
        }
        if (shape == RailShape.NORTH_WEST) {
            return new Direction[] { Direction.NORTH, Direction.WEST };
        }
        if (shape == RailShape.SOUTH_EAST) {
            return new Direction[] { Direction.SOUTH, Direction.EAST };
        }
        if (shape == RailShape.SOUTH_WEST) {
            return new Direction[] { Direction.SOUTH, Direction.WEST };
        }
        return new Direction[] { Direction.NORTH, Direction.SOUTH };
    }

    private boolean isFullyConnected(LevelAccessor world, BlockPos pos, RailShape shape) {
        Direction[] connectors = this.getConnectors(shape);
        int connectedEnds = 0;

        for (Direction connector : connectors) {
            if (this.hasCurrentConnection(world, pos, connector)) {
                connectedEnds++;
            }
        }

        return connectedEnds >= 2;
    }

    private boolean hasCurrentConnection(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return false;
        }

        if (!this.containsDirection(this.getConnectors(state.getValue(SHAPE)), direction)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        if (neighborState.getBlock() != this) {
            return false;
        }

        return this.containsDirection(this.getConnectors(neighborState.getValue(SHAPE)), direction.getOpposite());
    }

    private boolean isSameShikiBlock(LevelAccessor world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == this;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        RailShape shape = state.getValue(SHAPE);
        RailShape rotatedShape = switch (rotation) {
            case CLOCKWISE_90 -> switch (shape) {
                case NORTH_SOUTH -> RailShape.EAST_WEST;
                case EAST_WEST -> RailShape.NORTH_SOUTH;
                case ASCENDING_NORTH -> RailShape.ASCENDING_EAST;
                case ASCENDING_EAST -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_NORTH;
                case SOUTH_EAST -> RailShape.SOUTH_WEST;
                case SOUTH_WEST -> RailShape.NORTH_WEST;
                case NORTH_WEST -> RailShape.NORTH_EAST;
                case NORTH_EAST -> RailShape.SOUTH_EAST;
            };
            case COUNTERCLOCKWISE_90 -> switch (shape) {
                case NORTH_SOUTH -> RailShape.EAST_WEST;
                case EAST_WEST -> RailShape.NORTH_SOUTH;
                case ASCENDING_NORTH -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_EAST;
                case ASCENDING_EAST -> RailShape.ASCENDING_NORTH;
                case SOUTH_EAST -> RailShape.NORTH_EAST;
                case NORTH_EAST -> RailShape.NORTH_WEST;
                case NORTH_WEST -> RailShape.SOUTH_WEST;
                case SOUTH_WEST -> RailShape.SOUTH_EAST;
            };
            case CLOCKWISE_180 -> switch (shape) {
                case ASCENDING_NORTH -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_NORTH;
                case ASCENDING_EAST -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_EAST;
                case SOUTH_EAST -> RailShape.NORTH_WEST;
                case NORTH_WEST -> RailShape.SOUTH_EAST;
                case NORTH_EAST -> RailShape.SOUTH_WEST;
                case SOUTH_WEST -> RailShape.NORTH_EAST;
                default -> shape;
            };
            default -> shape;
        };
        return state.setValue(SHAPE, rotatedShape);
    }
}
