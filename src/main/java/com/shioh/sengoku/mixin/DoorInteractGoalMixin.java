package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.level.block.TripleDoorBlock;
import net.minecraft.world.level.block.DoubleDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minimal DoorInteractGoal extension to handle custom door blocks.
 * 
 * This mixin ONLY handles the actual door interaction (isOpen/setOpen).
 * All detection and pathfinding is handled by WalkNodeEvaluatorMixin.
 * 
 * No scanning. No hinge math. No distance checks. Just delegation.
 */
@Mixin(DoorInteractGoal.class)
public abstract class DoorInteractGoalMixin {
    @Shadow protected Mob mob;
    @Shadow protected BlockPos doorPos;
    @Shadow protected boolean hasDoor;

    // Intercept isOpen checks to handle custom door blocks
    @Inject(method = "isOpen", at = @At("HEAD"), cancellable = true)
    private void sengoku$isOpen(CallbackInfoReturnable<Boolean> cir) {
        if (!this.hasDoor) return;
        BlockState state = this.mob.level().getBlockState(this.doorPos);
        if (state.getBlock() instanceof TripleDoorBlock) {
            cir.setReturnValue(state.getValue(TripleDoorBlock.OPEN));
            return;
        }
        if (state.getBlock() instanceof DoubleDoorBlock) {
            cir.setReturnValue(state.getValue(DoubleDoorBlock.OPEN));
        }
    }

    // Intercept setOpen to delegate to custom door block methods
    @Inject(method = "setOpen", at = @At("HEAD"), cancellable = true)
    private void sengoku$setOpen(boolean open, CallbackInfo cir) {
        if (!this.hasDoor) return;
        BlockState state = this.mob.level().getBlockState(this.doorPos);
        if (state.getBlock() instanceof TripleDoorBlock) {
            ((TripleDoorBlock)state.getBlock()).setOpenAll(this.mob, this.mob.level(), this.doorPos, state, open);
            cir.cancel();
            return;
        }
        if (state.getBlock() instanceof DoubleDoorBlock) {
            ((DoubleDoorBlock)state.getBlock()).setOpen(this.mob, this.mob.level(), state, this.doorPos, open);
            cir.cancel();
        }
    }
}
