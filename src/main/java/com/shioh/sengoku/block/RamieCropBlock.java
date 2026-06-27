package com.shioh.sengoku.block;

import com.shioh.sengoku.init.TansuItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RamieCropBlock extends CropBlock {

    public RamieCropBlock(Properties properties) {
        super(properties);
    }

    // Tell Minecraft this crop can be planted on farmland
    protected boolean mayPlaceOn(BlockState state, BlockGetter world, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    // Check if this crop can survive at this position
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).is(Blocks.FARMLAND);
    }

    public void growCrops(ServerLevel world, BlockPos pos, BlockState state) {
        int newAge = Math.min(this.getAge(state) + 1, this.getMaxAge());
        BlockState newState = state.setValue(this.getAgeProperty(), newAge);
        world.setBlock(pos, newState, 3);
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);
        if (!aboveState.isAir()) {
            world.destroyBlock(pos, true);
            return;
        }

        if (this.getAge(state) < this.getMaxAge() && random.nextInt(2) == 0) {
            this.growCrops(world, pos, state);
        }
    }

    // --- Ensure creative pick block gives RAMIE item ---
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(TansuItemReg.RAMIE_I);
    }

    // Optional: for asItem() calls in code that convert block to item
    public Item asItem() {
        return TansuItemReg.RAMIE_I;
    }

    // --- Base seed for CropBlock (used by vanilla pick-block internally) ---
    public Item getBaseSeedId() {
        return TansuItemReg.RAMIE_I;
    }
}
