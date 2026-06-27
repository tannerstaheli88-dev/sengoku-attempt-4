package com.shioh.sengoku.block;

import com.shioh.sengoku.util.IBedShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor; 
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static com.shioh.sengoku.util.BedShapeState.*;
import static net.minecraft.world.level.block.state.properties.BedPart.HEAD;

public class GrassBedBlock extends BedBlock implements IBedShape {
    public final String bedWoodType;

    // Only pass wood type now
    public GrassBedBlock(String bedWoodType) {
        super(DyeColor.WHITE, Properties.ofFullCopy(Blocks.WHITE_BED).mapColor(blockState -> MapColor.PLANT));
        this.bedWoodType = bedWoodType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
                .setValue(mBedV$NORTH, false)
                .setValue(mBedV$EAST, false)
                .setValue(mBedV$SOUTH, false)
                .setValue(mBedV$WEST, false));
    }

    public GrassBedBlock(SoundType sound, String bedWoodType) {
        super(DyeColor.WHITE, Properties.ofFullCopy(Blocks.WHITE_BED)
                .mapColor(blockState -> MapColor.PLANT)
                .sound(sound));
        this.bedWoodType = bedWoodType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
                .setValue(mBedV$NORTH, false)
                .setValue(mBedV$EAST, false)
                .setValue(mBedV$SOUTH, false)
                .setValue(mBedV$WEST, false));
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (this.bedWoodType.contains("grass")) {
            return new BoundBambooBedBlockEntity(pos, state);
        } else {
            return new GrassBedBlockEntity(pos, state);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState neighborState, Direction face) {
        boolean isHeightMatch;
        boolean isBoundBamboo = state.getBlock() instanceof GrassBedBlock block && block.bedWoodType.contains("grass");
        boolean isNeighbourBoundBamboo = neighborState.getBlock() instanceof GrassBedBlock neighbor && neighbor.bedWoodType.contains("grass");
        if (neighborState.getBlock() instanceof BedBlock) {
            if ((isPillowedPackActive || isPillowedConnectedPackActive) && (isBoundBamboo == isNeighbourBoundBamboo)) {
                if (state.getValue(PART) == BedPart.FOOT) {
                    isHeightMatch = true;
                } else {
                    isHeightMatch = neighborState.getValue(PART) == HEAD && (state.getValue(FACING) == neighborState.getValue(FACING));
                }
            } else {
                isHeightMatch = (isBoundBamboo == isNeighbourBoundBamboo);
            }
        } else {
            isHeightMatch = false;
        }
        return face.getAxis() != Direction.Axis.Y && isHeightMatch;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (this.bedWoodType.contains("grass")) {
            if (needsToBeChecked) {
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    checkClientResourcepacks();
                }
            }
            Direction direction = getConnectedDirection(state).getOpposite();
            if (!isPillowedConnectedPackActive) {
                switch (direction) {
                    case NORTH -> { return (isPillowedPackActive && state.getValue(PART) == HEAD) ? BB_NORTH_PILLOWED : BB_BASE; }
                    case EAST -> { return (isPillowedPackActive && state.getValue(PART) == HEAD) ? BB_EAST_PILLOWED : BB_BASE; }
                    case SOUTH -> { return (isPillowedPackActive && state.getValue(PART) == HEAD) ? BB_SOUTH_PILLOWED : BB_BASE; }
                    default -> { return (isPillowedPackActive && state.getValue(PART) == HEAD) ? BB_WEST_PILLOWED : BB_BASE; }
                }
            } else {
                boolean n = state.getValue(mBedV$NORTH);
                boolean e = state.getValue(mBedV$EAST);
                boolean s = state.getValue(mBedV$SOUTH);
                boolean w = state.getValue(mBedV$WEST);
                if (state.getValue(PART) == HEAD) {
                    switch (direction) {
                        case NORTH -> { return e && w ? BB_NORTH_PILLOWED_EW : e ? BB_NORTH_PILLOWED_E : w ? BB_NORTH_PILLOWED_W : BB_NORTH_PILLOWED; }
                        case EAST -> { return n && s ? BB_EAST_PILLOWED_NS : n ? BB_EAST_PILLOWED_N : s ? BB_EAST_PILLOWED_S : BB_EAST_PILLOWED; }
                        case SOUTH -> { return e && w ? BB_SOUTH_PILLOWED_EW : e ? BB_SOUTH_PILLOWED_E : w ? BB_SOUTH_PILLOWED_W : BB_SOUTH_PILLOWED; }
                        default -> { return n && s ? BB_WEST_PILLOWED_NS : n ? BB_WEST_PILLOWED_N : s ? BB_WEST_PILLOWED_S : BB_WEST_PILLOWED; }
                    }
                } else return BB_BASE;
            }
        } else {
            return super.getShape(state, level, pos, context);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        
        // Get the correct bed position (head part) - match vanilla BedBlock behavior
        if (state.getValue(PART) != BedPart.HEAD) {
            pos = pos.relative(state.getValue(FACING));
            state = level.getBlockState(pos);
            if (!state.is(this)) {
                return InteractionResult.CONSUME;
            }
        }

        // Check if can sleep (right dimension, time, etc)
        if (!BedBlock.canSetSpawn(level)) {
            // Bed explodes in wrong dimension
            level.removeBlock(pos, false);
            BlockPos otherPos = pos.relative(((Direction)state.getValue(FACING)).getOpposite());
            if (level.getBlockState(otherPos).is(this)) {
                level.removeBlock(otherPos, false);
            }
            level.explode(null, level.damageSources().badRespawnPointExplosion(pos.getCenter()), null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0F, true, Level.ExplosionInteraction.BLOCK);
            return InteractionResult.SUCCESS;
        }

        // Check if occupied
        if (state.getValue(OCCUPIED)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("block.minecraft.bed.occupied"), true);
            return InteractionResult.SUCCESS;
        }

        // Start sleeping normally - grass beds now work like regular beds
        player.startSleepInBed(pos).ifLeft(problem -> {
            player.displayClientMessage(problem.getMessage(), true);
        });

        return InteractionResult.SUCCESS;
    }
}
