package com.shioh.sengoku.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * A custom projection that fills in terrain irregularities before placing structures.
 * This projection works like terrain_matching but preprocesses the area by filling
 * holes and gaps with appropriate terrain blocks (dirt, stone) to create a smoother
 * foundation for structure placement.
 */
public class TerrainFillingProjection {
    private static final boolean ENABLE_TERRAIN_ADAPTATION = true;
    
    /**
     * Fills terrain irregularities in the area before structure placement.
     * 
     * @param level The level/world
     * @param template The structure template
     * @param settings The placement settings
     * @param pos The placement position
     * @param boundingBox The bounding box of the structure
     * @param random Random source for variation
     */
    public static void fillTerrainBeforePlacement(
            LevelAccessor level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos pos,
            BoundingBox boundingBox,
            RandomSource random) {

        if (!ENABLE_TERRAIN_ADAPTATION) {
            return;
        }
        
        int chunkMinX = (pos.getX() >> 4) << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = (pos.getZ() >> 4) << 4;
        int chunkMaxZ = chunkMinZ + 15;

        // Get the structure's footprint at ground level
        int minX = boundingBox.minX();
        int maxX = boundingBox.maxX();
        int minZ = boundingBox.minZ();
        int maxZ = boundingBox.maxZ();

        int passMinX = Math.max(minX, chunkMinX);
        int passMaxX = Math.min(maxX, chunkMaxX);
        int passMinZ = Math.max(minZ, chunkMinZ);
        int passMaxZ = Math.min(maxZ, chunkMaxZ);
        if (passMinX > passMaxX || passMinZ > passMaxZ) {
            return;
        }
        
        // TERRAIN MATCHING LOGIC: Sample multiple points to find where structure will place
        // This mimics how terrain_matching projection determines the base height
        int lowestY = Integer.MAX_VALUE;
        
        // Sample the entire footprint to find the lowest point
        // This ensures we match how terrain_matching actually works
        for (int x = passMinX; x <= passMaxX; x++) {
            for (int z = passMinZ; z <= passMaxZ; z++) {
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (surfaceY < lowestY) {
                    lowestY = surfaceY;
                }
            }
        }
        
        // The structure will place at the lowest point (terrain_matching behavior)
        // So we want to fill UP TO that level, making everything else match
        int structureBaseY = lowestY;
        
        // Fill terrain irregularities to match the base level
        for (int x = passMinX; x <= passMaxX; x++) {
            for (int z = passMinZ; z <= passMaxZ; z++) {
                BlockPos columnPos = new BlockPos(x, 0, z);
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                
                // Fill only if this column is lower than the structure base
                // Leave a small gap (1 block) to avoid interfering with structure placement
                if (surfaceY < structureBaseY - 1) {
                    // Fill up to one block below the structure base
                    fillColumn(level, columnPos, surfaceY, structureBaseY - 1, random);
                }
            }
        }
    }
    
    /**
     * Fills a column from bottom to target height with appropriate blocks.
     */
    private static void fillColumn(LevelAccessor level, BlockPos basePos, int startY, int targetY, RandomSource random) {
        // Fill from the starting height to target
        for (int y = startY; y < targetY; y++) {
            BlockPos fillPos = basePos.atY(y);
            BlockState currentState = level.getBlockState(fillPos);
            
            // Only fill air and similar non-solid blocks
            if (currentState.isAir() || currentState.is(Blocks.CAVE_AIR) || 
                currentState.is(Blocks.VOID_AIR) || currentState.liquid()) {
                
                BlockState fillBlock = getFillBlock(level, fillPos, y, targetY, random);
                level.setBlock(fillPos, fillBlock, 2);
            }
        }
    }
    
    /**
     * Fills just the top layer with appropriate surface block.
     */
    private static void fillColumnTop(LevelAccessor level, BlockPos basePos, int startY, int targetY, RandomSource random) {
        BlockPos topPos = basePos.atY(targetY - 1);
        BlockState currentState = level.getBlockState(topPos);
        
        if (currentState.isAir() || currentState.is(Blocks.CAVE_AIR)) {
            // Look at the block below to match the terrain
            BlockState belowState = level.getBlockState(basePos.atY(startY - 1));
            BlockState fillBlock;
            
            if (belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.DIRT) || 
                belowState.is(Blocks.PODZOL) || belowState.is(Blocks.MYCELIUM)) {
                fillBlock = Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (belowState.is(Blocks.SAND) || belowState.is(Blocks.SANDSTONE)) {
                fillBlock = Blocks.SAND.defaultBlockState();
            } else if (belowState.is(Blocks.GRAVEL)) {
                fillBlock = Blocks.GRAVEL.defaultBlockState();
            } else {
                fillBlock = Blocks.DIRT.defaultBlockState();
            }
            
            level.setBlock(topPos, fillBlock, 2);
        }
    }
    
    /**
     * Determines the appropriate fill block based on depth and surrounding terrain.
     */
    private static BlockState getFillBlock(LevelAccessor level, BlockPos pos, int currentY, int targetY, RandomSource random) {
        int depthFromTarget = targetY - currentY;
        
        // Check surrounding blocks for context
        BlockState northState = level.getBlockState(pos.relative(Direction.NORTH));
        BlockState southState = level.getBlockState(pos.relative(Direction.SOUTH));
        BlockState eastState = level.getBlockState(pos.relative(Direction.EAST));
        BlockState westState = level.getBlockState(pos.relative(Direction.WEST));
        BlockState belowState = level.getBlockState(pos.below());
        
        // If we're near the surface (within 3 blocks)
        if (depthFromTarget <= 3) {
            // Match nearby terrain
            if (isSandyTerrain(northState) || isSandyTerrain(southState) || 
                isSandyTerrain(eastState) || isSandyTerrain(westState) || isSandyTerrain(belowState)) {
                return random.nextFloat() < 0.8f ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
            }
            
            // Top layer should be dirt/grass
            if (depthFromTarget == 1) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            
            // Near-surface layers are dirt
            return Blocks.DIRT.defaultBlockState();
        }
        
        // Deeper fills use stone
        return Blocks.STONE.defaultBlockState();
    }
    
    /**
     * Checks if a block state represents sandy terrain.
     */
    private static boolean isSandyTerrain(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || 
               state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE);
    }
}
