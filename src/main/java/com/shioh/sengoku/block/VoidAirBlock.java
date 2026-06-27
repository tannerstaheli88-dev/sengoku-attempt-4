package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A special air block that behaves exactly like normal air in-game but gets treated
 * as a structure void during structure NBT conversion for world generation datapacks.
 * 
 * This block:
 * - Acts exactly like air (completely empty space, not replaceable)
 * - Cannot be placed or broken in survival or creative mode
 * - Has no collision, no rendering
 * - Is treated as a "structure void" marker during structure generation
 * - Intended to be placed via WorldEdit for structure editing
 */
public class VoidAirBlock extends Block {
    public static final MapCodec<VoidAirBlock> CODEC = simpleCodec(VoidAirBlock::new);

    public VoidAirBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // Allow player block placement to replace this block directly
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        // Behave like air for placement: any non-empty placement can replace this
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
