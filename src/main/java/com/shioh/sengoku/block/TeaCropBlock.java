package com.shioh.sengoku.block;

import com.shioh.sengoku.init.TansuItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TeaCropBlock extends CropBlock {

    public TeaCropBlock(Properties properties) {
        super(properties);
    }

    // Tell Minecraft this crop can be planted on farmland
    protected boolean mayPlaceOn(net.minecraft.world.level.block.state.BlockState state, BlockGetter world, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    // Check if this crop can survive at this position
    public boolean canSurvive(net.minecraft.world.level.block.state.BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).is(Blocks.FARMLAND);
    }

    // --- Ensure creative pick block gives TEA_LEAF item ---
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        return new ItemStack(TansuItemReg.TEA_LEAF);
    }

    // Optional: for asItem() calls in code that convert block to item
    public Item asItem() {
        return TansuItemReg.TEA_SEEDS;
    }

    // --- Base seed for CropBlock (used by vanilla pick-block internally) ---
    public Item getBaseSeedId() {
        return TansuItemReg.TEA_SEEDS;
    }
}
