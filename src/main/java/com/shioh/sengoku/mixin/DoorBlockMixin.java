package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.TripleDoorBlock;
import net.minecraft.world.level.block.DoubleDoorBlock;
import com.shioh.sengoku.Constants;
import com.shioh.sengoku.sengokuFabric;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {
    private static final double SLIDE = 13.0;

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void hookShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        // Resolve to lower half
        BlockState actualState = state;
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            if (lower.getBlock() instanceof DoorBlock) {
                actualState = lower;
            }
        }

// Ignore copper doors entirely (keep vanilla behavior)
// Ignore all copper doors (use vanilla behavior)
if (actualState.is(Blocks.COPPER_DOOR)
    || actualState.is(Blocks.EXPOSED_COPPER_DOOR)
    || actualState.is(Blocks.WEATHERED_COPPER_DOOR)
    || actualState.is(Blocks.OXIDIZED_COPPER_DOOR)
    || actualState.is(Blocks.WAXED_COPPER_DOOR)
    || actualState.is(Blocks.WAXED_EXPOSED_COPPER_DOOR)
    || actualState.is(Blocks.WAXED_WEATHERED_COPPER_DOOR)
    || actualState.is(Blocks.WAXED_OXIDIZED_COPPER_DOOR)) {
    return;
}

boolean open = actualState.getValue(DoorBlock.OPEN);
if (!open) return;

        Direction facing = actualState.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = actualState.getValue(DoorBlock.HINGE);

        VoxelShape shape;

        switch (facing) {
            case NORTH:
                shape = (hinge == DoorHingeSide.LEFT) ? DoorBlock.box(-SLIDE,0,14,16-SLIDE,16,16)
                                                      : DoorBlock.box(SLIDE,0,14,16+SLIDE,16,16);
                break;
            case SOUTH:
                shape = (hinge == DoorHingeSide.LEFT) ? DoorBlock.box(SLIDE,0,0,16+SLIDE,16,2)
                                                      : DoorBlock.box(-SLIDE,0,0,16-SLIDE,16,2);
                break;
            case WEST:
                shape = (hinge == DoorHingeSide.LEFT) ? DoorBlock.box(14,0,SLIDE,16,16,16+SLIDE)
                                                      : DoorBlock.box(14,0,-SLIDE,16,16,16-SLIDE);
                break;
            case EAST:
            default:
                shape = (hinge == DoorHingeSide.LEFT) ? DoorBlock.box(0,0,-SLIDE,2,16,16-SLIDE)
                                                      : DoorBlock.box(0,0,SLIDE,2,16,16+SLIDE);
                break;
        }

        cir.setReturnValue(shape);
    }

    @Inject(method = "isWoodenDoor(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void sengoku$isWoodenTripleDoor(net.minecraft.world.level.block.state.BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // If this isn't a vanilla DoorBlock but a TripleDoorBlock, treat it as a wooden door when its BlockSetType allows hand opening.
        if (state.getBlock() instanceof TripleDoorBlock) {
            TripleDoorBlock t = (TripleDoorBlock) state.getBlock();
            if (Constants.isDoorAiDebugEnabled()) sengokuFabric.LOGGER.info("DoorAI: isWoodenDoor(BlockState) TripleDoorBlock -> {}", t.type().canOpenByHand());
            cir.setReturnValue(t.type().canOpenByHand());
            return;
        }
        if (state.getBlock() instanceof DoubleDoorBlock) {
            DoubleDoorBlock d = (DoubleDoorBlock) state.getBlock();
            if (Constants.isDoorAiDebugEnabled()) sengokuFabric.LOGGER.info("DoorAI: isWoodenDoor(BlockState) DoubleDoorBlock -> {}", d.type().canOpenByHand());
            cir.setReturnValue(d.type().canOpenByHand());
        }
    }

    @Inject(method = "isWoodenDoor(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private static void sengoku$isWoodenDoorByPos(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TripleDoorBlock) {
            TripleDoorBlock t = (TripleDoorBlock) state.getBlock();
            if (Constants.isDoorAiDebugEnabled()) sengokuFabric.LOGGER.info("DoorAI: isWoodenDoor(Level,Pos) TripleDoorBlock at {} -> {}", pos, t.type().canOpenByHand());
            cir.setReturnValue(t.type().canOpenByHand());
            return;
        }
        if (state.getBlock() instanceof DoubleDoorBlock) {
            DoubleDoorBlock d = (DoubleDoorBlock) state.getBlock();
            if (Constants.isDoorAiDebugEnabled()) sengokuFabric.LOGGER.info("DoorAI: isWoodenDoor(Level,Pos) DoubleDoorBlock at {} -> {}", pos, d.type().canOpenByHand());
            cir.setReturnValue(d.type().canOpenByHand());
        }
    }
}
