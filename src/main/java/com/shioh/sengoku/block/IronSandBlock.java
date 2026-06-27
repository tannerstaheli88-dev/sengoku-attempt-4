package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.Collections;
import java.util.List;

/**
 * Iron Sand - a dark sand block containing iron particles.
 * Falls like regular sand and can be smelted into Tamahagane.
 */
public class IronSandBlock extends FallingBlock {
    
    public static final MapCodec<IronSandBlock> CODEC = simpleCodec(IronSandBlock::new);
    
    public IronSandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    
    @Override
    public MapCodec<IronSandBlock> codec() {
        return CODEC;
    }
    
    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        // Dark gray color for the falling particles (RGB: 64, 64, 64)
        return 0x404040;
    }

    @Override
    public BlockState playerWillDestroy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);

        // Server-side: only drop the block item when mined with a shovel of stone tier or above
        if (!level.isClientSide && !player.isCreative()) {
            try {
                ItemStack tool = player.getMainHandItem();
                if (tool != null && !tool.isEmpty() && tool.getItem() instanceof ShovelItem) {
                    Item item = tool.getItem();
                    if (item instanceof TieredItem) {
                        TieredItem tiered = (TieredItem) item;
                        if (tiered.getTier() == Tiers.STONE || tiered.getTier() == Tiers.IRON ||
                            tiered.getTier() == Tiers.DIAMOND || tiered.getTier() == Tiers.NETHERITE) {
                            this.popResource(level, pos, new ItemStack(this.asItem()));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return result;
    }
}
