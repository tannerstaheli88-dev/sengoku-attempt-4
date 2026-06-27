package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import static com.shioh.sengoku.init.GrassBedBlockReg.MORE_BED_VARIANT_BLOCK_ENTITY;

public class GrassBedBlockEntity extends BlockEntity {
    private DyeColor color;
    public String woodType;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public GrassBedBlockEntity(BlockPos pos, BlockState blockState) {
        super(MORE_BED_VARIANT_BLOCK_ENTITY, pos, blockState);
        this.color = ((BedBlock)blockState.getBlock()).getColor();
        this.woodType = ((GrassBedBlock)blockState.getBlock()).bedWoodType;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public DyeColor getColor() {
        return this.color;
    }
}
