package com.shioh.sengoku.mixin;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrapDoorBlock.class)
public abstract class TrapDoorStiffStateMixin extends HorizontalDirectionalBlock {

    protected TrapDoorStiffStateMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(CallbackInfo ci) {
        try {
            // Use the block's existing default state and only add the STIFF value so we don't overwrite
            // other defaults (open, half, facing) which would cause newly placed trapdoors to appear open.
            this.registerDefaultState(this.defaultBlockState().setValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF, false));
        } catch (Exception ignored) {}
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    private void onCreateBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF);
    }
}
