package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GravityProcessor.class)
public class GravityProcessorMixin {

    @Shadow
    private Heightmap.Types heightmap;

    @Redirect(
        method = "processBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelReader;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
        )
    )
    private int fixStaleWGHeightmap(LevelReader level, Heightmap.Types type, int x, int z) {
        // Get whatever Y vanilla would have used
        int y = level.getHeight(type, x, z);

        // Only correct _WG heightmap types
        if (type != Heightmap.Types.WORLD_SURFACE_WG && type != Heightmap.Types.OCEAN_FLOOR_WG) {
            return y;
        }

        // Scan downward from y - 1 to skip vegetation
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos(x, y - 1, z);
        while (scanPos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(scanPos);
if (!state.getFluidState().isEmpty()) {
    break;  // treat water/lava as solid ground
} else if (state.isAir()
    || state.is(net.minecraft.tags.BlockTags.REPLACEABLE)
    || state.is(net.minecraft.tags.BlockTags.LOGS)
    || state.is(net.minecraft.tags.BlockTags.FLOWERS)
    || state.is(net.minecraft.tags.BlockTags.TALL_FLOWERS)
    || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
    scanPos.move(0, -1, 0);
            } else {
                break;
            }
        }

        return scanPos.getY() + 1;
    }
}