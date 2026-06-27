package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import static com.shioh.sengoku.init.GrassBedBlockReg.BOUND_BAMBOO_BED_BLOCK_ENTITY;

public class BoundBambooBedBlockEntity extends BlockEntity {
    public String woodType;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BoundBambooBedBlockEntity(BlockPos pos, BlockState blockState) {
        super(BOUND_BAMBOO_BED_BLOCK_ENTITY, pos, blockState);
        this.woodType = ((GrassBedBlock)blockState.getBlock()).bedWoodType;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
