package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PaperLanternBlock extends LanternBlock {

    public PaperLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Placement and break sounds are handled via the block's SoundType (configured during registration)

    @Override
    public BlockState playerWillDestroy(Level level, net.minecraft.core.BlockPos pos, BlockState state, Player player) {
        // Call super to preserve default behavior
        BlockState result = super.playerWillDestroy(level, pos, state, player);

        // If this is server-side and the player is not in creative, ensure the lantern drops its item.
        // Some attachment / neighbor removal code can cause drops only when the supporting block is removed;
        // this ensures a direct player break will still drop the item.
        if (!level.isClientSide && !player.isCreative()) {
            // spawn the item form of this block
            this.popResource(level, pos, new ItemStack(this.asItem()));
        }

        return result;
    }
}
