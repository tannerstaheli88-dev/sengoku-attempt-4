package com.shioh.sengoku.mixin;

import com.shioh.sengoku.block.GrassBedBlock;
import com.shioh.sengoku.util.IBedShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.shioh.sengoku.util.BedShapeState.*;
import static net.minecraft.world.level.block.BedBlock.*;
import static net.minecraft.world.level.block.state.properties.BedPart.HEAD;

@Mixin(value = BedBlock.class)
public abstract class BedBlockMixin extends HorizontalDirectionalBlock implements IBedShape {
    @Shadow
    @Final
    public static EnumProperty<BedPart> PART;
    @Shadow
    @Final
    public static BooleanProperty OCCUPIED;

    protected BedBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectedConstructorAtTail(DyeColor color, Properties properties, CallbackInfo ci) {
        if (shouldApply(this.getClass())) {
            this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, false).setValue(mBedV$NORTH, false).setValue(mBedV$EAST, false).setValue(mBedV$SOUTH, false).setValue(mBedV$WEST, false));
        }
    }

    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    public void injectedGetRenderShapeAtHead(BlockState state, CallbackInfoReturnable<RenderShape> cir){
        if (shouldApply(this.getClass())) {
            cir.setReturnValue(RenderShape.MODEL);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState neighborState, Direction face) {
        if (shouldApply(this.getClass())) {
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
        } else { return super.skipRendering(state, neighborState, face); }
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (shouldApply(this.getClass())) {
            builder.add(FACING, PART, OCCUPIED, mBedV$NORTH, mBedV$EAST, mBedV$SOUTH, mBedV$WEST);
        }
        else {
            builder.add(FACING, PART, OCCUPIED);
        }
    }

    @Inject(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true)
    public void injectedUpdateShapeAtReturn(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (shouldApply(this.getClass())) {
            BlockState bed = cir.getReturnValue();
            BlockState north = level.getBlockState(pos.north(1));
            BlockState east = level.getBlockState(pos.east(1));
            BlockState south = level.getBlockState(pos.south(1));
            BlockState west = level.getBlockState(pos.west(1));

            if (bed.getBlock() instanceof BedBlock) {
                String bedWood;
                if ((bed.getBlock() instanceof GrassBedBlock)) {
                    bedWood = ((GrassBedBlock) bed.getBlock()).bedWoodType;
                } else {
                    bedWood = "oak";
                }
                if (north.getBlock() instanceof BedBlock && north.getValue(PART) == bed.getValue(PART) && north.getValue(FACING) == bed.getValue(FACING)) {
                    String nWood;
                    if (north.getBlock() instanceof GrassBedBlock) {
                        nWood = ((GrassBedBlock) north.getBlock()).bedWoodType;
                    } else {
                        nWood = "oak";
                    }
                    bed = bed.setValue(mBedV$NORTH, (bedWood.equals(nWood)));
                } else {
                    bed = bed.setValue(mBedV$NORTH, false);
                }
                if (east.getBlock() instanceof BedBlock && east.getValue(PART) == bed.getValue(PART) && east.getValue(FACING) == bed.getValue(FACING)) {
                    String eWood;
                    if (east.getBlock() instanceof GrassBedBlock) {
                        eWood = ((GrassBedBlock) east.getBlock()).bedWoodType;
                    } else {
                        eWood = "oak";
                    }
                    bed = bed.setValue(mBedV$EAST, (bedWood.equals(eWood)));
                } else {
                    bed = bed.setValue(mBedV$EAST, false);
                }
                if (south.getBlock() instanceof BedBlock && south.getValue(PART) == bed.getValue(PART) && south.getValue(FACING) == bed.getValue(FACING)) {
                    String sWood;
                    if (south.getBlock() instanceof GrassBedBlock) {
                        sWood = ((GrassBedBlock) south.getBlock()).bedWoodType;
                    } else {
                        sWood = "oak";
                    }
                    bed = bed.setValue(mBedV$SOUTH, (bedWood.equals(sWood)));
                } else {
                    bed = bed.setValue(mBedV$SOUTH, false);
                }
                if (west.getBlock() instanceof BedBlock && west.getValue(PART) == bed.getValue(PART) && west.getValue(FACING) == bed.getValue(FACING)) {
                    String wWood;
                    if (west.getBlock() instanceof GrassBedBlock) {
                        wWood = ((GrassBedBlock) west.getBlock()).bedWoodType;
                    } else {
                        wWood = "oak";
                    }
                    bed = bed.setValue(mBedV$WEST, (bedWood.equals(wWood)));
                } else {
                    bed = bed.setValue(mBedV$WEST, false);
                }
                cir.setReturnValue(bed);
            }
        }
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (shouldApply(this.getClass())) {
            if (needsToBeChecked) {
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    checkClientResourcepacks();
                }
            }
            Direction direction = getConnectedDirection(state).getOpposite();
            if (!isPillowedConnectedPackActive) {
                switch (direction) {
                    case NORTH -> {
                        cir.setReturnValue((isPillowedPackActive && state.getValue(PART) == HEAD) ? NORTH_SHAPE_PILLOWED : NORTH_SHAPE);
                    }
                    case EAST -> {
                        cir.setReturnValue((isPillowedPackActive && state.getValue(PART) == HEAD) ? EAST_SHAPE_PILLOWED : EAST_SHAPE);
                    }
                    case SOUTH -> {
                        cir.setReturnValue((isPillowedPackActive && state.getValue(PART) == HEAD) ? SOUTH_SHAPE_PILLOWED : SOUTH_SHAPE);
                    }
                    default -> {
                        cir.setReturnValue((isPillowedPackActive && state.getValue(PART) == HEAD) ? WEST_SHAPE_PILLOWED : WEST_SHAPE);
                    }
                }
            } else {
                boolean n = state.getValue(mBedV$NORTH);
                boolean e = state.getValue(mBedV$EAST);
                boolean s = state.getValue(mBedV$SOUTH);
                boolean w = state.getValue(mBedV$WEST);
                if (state.getValue(PART) == HEAD) {
                    switch (direction) {
                        case NORTH ->
                                cir.setReturnValue(e && w ? NORTH_SHAPE_PILLOWED_EW : e ? NORTH_SHAPE_PILLOWED_E : w ? NORTH_SHAPE_PILLOWED_W : NORTH_SHAPE_PILLOWED);

                        case EAST ->
                                cir.setReturnValue(n && s ? EAST_SHAPE_PILLOWED_NS : n ? EAST_SHAPE_PILLOWED_N : s ? EAST_SHAPE_PILLOWED_S : EAST_SHAPE_PILLOWED);

                        case SOUTH ->
                                cir.setReturnValue(e && w ? SOUTH_SHAPE_PILLOWED_EW : e ? SOUTH_SHAPE_PILLOWED_E : w ? SOUTH_SHAPE_PILLOWED_W : SOUTH_SHAPE_PILLOWED);

                        default ->
                                cir.setReturnValue(n && s ? WEST_SHAPE_PILLOWED_NS : n ? WEST_SHAPE_PILLOWED_N : s ? WEST_SHAPE_PILLOWED_S : WEST_SHAPE_PILLOWED);

                    }
                } else {
                    switch (direction) {
                        case NORTH ->
                                cir.setReturnValue(e && w ? BASE : e ? FLAT_SHAPE_LEG_NW : w ? FLAT_SHAPE_LEG_NE : NORTH_SHAPE);

                        case EAST ->
                                cir.setReturnValue(n && s ? BASE : n ? FLAT_SHAPE_LEG_SE : s ? FLAT_SHAPE_LEG_NE : EAST_SHAPE);

                        case SOUTH ->
                                cir.setReturnValue(e && w ? BASE : e ? FLAT_SHAPE_LEG_SW : w ? FLAT_SHAPE_LEG_SE : SOUTH_SHAPE);

                        default ->
                                cir.setReturnValue(n && s ? BASE : n ? FLAT_SHAPE_LEG_SW : s ? FLAT_SHAPE_LEG_NW : WEST_SHAPE);

                    }
                }
            }
        }
    }

    @Override
    public @NotNull SoundType getSoundType(BlockState state) {
        if (shouldApply(this.getClass())) {
            return state.getValue(PART) == BedPart.HEAD ? SoundType.WOOL : this.soundType;
        } else { return super.getSoundType(state); }
    }

    @Unique
    private static boolean shouldApply(Object thisClass) {
        return thisClass.equals(BedBlock.class) || thisClass.equals(GrassBedBlock.class);
    }
}