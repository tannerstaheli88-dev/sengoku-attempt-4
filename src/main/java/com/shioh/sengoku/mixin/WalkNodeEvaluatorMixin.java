package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.block.TripleDoorBlock;
import net.minecraft.world.level.block.DoubleDoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Teaches the pathfinding system about custom door blocks by flagging their
 * canonical control blocks as door nodes. This is the CORRECT way to integrate
 * custom doors - at the pathfinding layer, not at the DoorInteractGoal layer.
 *
 * By returning DOOR_WOOD_CLOSED or DOOR_OPEN for the middle/control column,
 * vanilla navigation will automatically:
 * - Path directly to the correct column
 * - Set doorPos correctly in DoorInteractGoal
 * - Handle steering without custom logic
 * - Work perfectly with villager AI
 */
@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {

    @Inject(
        method = "getPathType(Lnet/minecraft/world/level/pathfinder/PathfindingContext;III)Lnet/minecraft/world/level/pathfinder/PathType;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void sengoku$customDoorPathTypes(PathfindingContext context, int x, int y, int z, CallbackInfoReturnable<PathType> cir) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.level().getBlockState(pos);

        // TrapDoors: Both open and closed trapdoors should block pathfinding
        // Open trapdoors flip vertically and act as barriers/gates
        // Closed trapdoors are horizontal barriers in their half
        if (state.getBlock() instanceof TrapDoorBlock) {
            cir.setReturnValue(PathType.BLOCKED);
            return;
        }

        // TripleDoorBlock: Only flag the MIDDLE column, LOWER half as a door node
        if (state.getBlock() instanceof TripleDoorBlock) {
            if (state.getValue(TripleDoorBlock.PART) == TripleDoorBlock.Part.MIDDLE &&
                state.getValue(TripleDoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                cir.setReturnValue(state.getValue(TripleDoorBlock.OPEN)
                    ? PathType.DOOR_OPEN
                    : PathType.DOOR_WOOD_CLOSED);
            }
        }

        // DoubleDoorBlock: Only flag the control column (SIDE=true), LOWER half as a door node
        if (state.getBlock() instanceof DoubleDoorBlock) {
            if (state.getValue(DoubleDoorBlock.SIDE) &&
                state.getValue(DoubleDoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                cir.setReturnValue(state.getValue(DoubleDoorBlock.OPEN)
                    ? PathType.DOOR_OPEN
                    : PathType.DOOR_WOOD_CLOSED);
            }
        }
    }
}
