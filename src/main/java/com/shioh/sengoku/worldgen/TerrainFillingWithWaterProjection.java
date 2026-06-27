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
import net.minecraft.world.level.material.Fluids;

/**
 * A custom projection that fills in terrain to prevent floating structures, but also fills in water columns to create support pillars through water.
 * 
 * Unlike TerrainFillingProjection, this version ALSO replaces water blocks, allowing
 * structures to be placed in shallow water areas or lake beds.
 */
public class TerrainFillingWithWaterProjection {
    // How far (in blocks) to extend support outside the structure footprint for a natural slope
    private static final int SUPPORT_MARGIN = 3;
    // Maximum vertical slope depth (1 block drop per horizontal block)
    private static final int MAX_SLOPE_DEPTH = 3;
    // Fade the mound near chunk edges to avoid a flat wall when generation is chunk-bounded
    private static final int CHUNK_EDGE_FADE = 3;
    private static final boolean CLAMP_TO_CHUNK = true;
    // Limit how deep we build supports through water; set to -1 for unlimited to ocean floor
    private static final int MAX_WATER_SUPPORT_DEPTH = -1; // blocks; e.g., 32 to cap, -1 for unlimited
    
    /**
     * Fills terrain irregularities and water in the area before structure placement.
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

        int minX = boundingBox.minX();
        int maxX = boundingBox.maxX();
        int minZ = boundingBox.minZ();
        int maxZ = boundingBox.maxZ();

        double centerX = (minX + maxX) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;
        double halfW = (maxX - minX) / 2.0;
        double halfD = (maxZ - minZ) / 2.0;

        // Find the placement height (water surface for terrain_matching)
        int waterSurfaceY = Integer.MAX_VALUE;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (surfaceY < waterSurfaceY) waterSurfaceY = surfaceY;
            }
        }
        int structureBaseY = waterSurfaceY;

        int expand = SUPPORT_MARGIN;
        for (int x = minX - expand; x <= maxX + expand; x++) {
            for (int z = minZ - expand; z <= maxZ + expand; z++) {

                double nx = (x - centerX) / (halfW + expand);
                double nz = (z - centerZ) / (halfD + expand);
                double dist = Math.sqrt(nx * nx + nz * nz);

                if (dist > 1.0) continue;

                double falloff = 1.0 - dist;
                double noise = (random.nextDouble() * 0.35) - 0.1;
                double effectiveFalloff = falloff + noise;

                if (effectiveFalloff <= 0.05) continue;

                // Inside the footprint always fills fully; taper only applies outside
                boolean insideFootprint = x >= minX && x <= maxX && z >= minZ && z <= maxZ;
                int fillTopY = insideFootprint
                        ? structureBaseY - 1
                        : structureBaseY - 1 - (int)((1.0 - effectiveFalloff) * MAX_SLOPE_DEPTH);

                int solidY = findSolidSurface(level, x, z);
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

                if (surfaceY < fillTopY) {
                    fillColumn(level, new BlockPos(x, 0, z), surfaceY, fillTopY, random);
                }

                if (fillTopY >= solidY) {
                    fillWaterColumnDown(level, new BlockPos(x, 0, z), solidY, fillTopY + 1, random);
                }
            }
        }
    }
    
    /**
     * Finds the solid surface, ignoring water.
     * This allows us to find the ocean/lake floor rather than water surface.
     */
private static int findSolidSurface(LevelAccessor level, int x, int z) {
    int startY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
    
    // Scan downward from the ocean floor heightmap to confirm solid ground
    // OCEAN_FLOOR_WG already ignores water, so this gives us the solid floor
    // We just need to verify it's not cave air or void
    for (int y = startY; y >= level.getMinBuildHeight(); y--) {
        BlockPos checkPos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(checkPos);
        
        if (!state.isAir() && !state.liquid() &&
            !state.is(Blocks.CAVE_AIR) && !state.is(Blocks.VOID_AIR)) {
            return y + 1;
        }
    }
    
    return startY;
}
    
    /**
     * Fills water blocks in a column from ocean floor UP to the structure base.
     * This creates support pillars through water so structures don't float.
     * 
     * @param level The level/world
     * @param basePos The base position (x, z)
     * @param oceanFloorY The Y level of the ocean floor (solid ground)
     * @param structureBaseY The Y level where the structure will place
     * @param random Random source for variation
     */
    private static void fillWaterColumnDown(LevelAccessor level, BlockPos basePos, int oceanFloorY, int structureBaseY, RandomSource random) {
        // Compute start height, optionally capping maximum depth to reduce heavy updates
        int startY = oceanFloorY;
        if (MAX_WATER_SUPPORT_DEPTH > 0) {
            startY = Math.max(startY, (structureBaseY - 1) - MAX_WATER_SUPPORT_DEPTH);
        }
        // Fill from start height up to just below the structure base
        for (int y = startY; y < structureBaseY - 1; y++) {
            BlockPos fillPos = basePos.atY(y);
            BlockState currentState = level.getBlockState(fillPos);
            
            // Fill water blocks and air pockets
            if (currentState.liquid() || currentState.isAir() || 
                currentState.is(Blocks.CAVE_AIR) || currentState.is(Blocks.VOID_AIR)) {
                BlockState fillBlock = getWaterFillBlock(level, fillPos, y, structureBaseY, random);
                if (!fillBlock.equals(currentState)) {
                    level.setBlock(fillPos, fillBlock, 2);
                }
            }
        }
    }

    /**
     * Builds a tapered mound around the structure footprint to blend supports naturally into the terrain.
     * The mound height decreases by 1 block per horizontal block away from the footprint, up to MAX_SLOPE_DEPTH.
     */
    
    /**
     * Fills a column from bottom to target height with appropriate blocks.
     */
    private static void fillColumn(LevelAccessor level, BlockPos basePos, int startY, int targetY, RandomSource random) {
        // Fill from the starting height to target
        for (int y = startY; y < targetY; y++) {
            BlockPos fillPos = basePos.atY(y);
            BlockState currentState = level.getBlockState(fillPos);
            
            // Fill air, water, and similar non-solid blocks
            if (currentState.isAir() || currentState.is(Blocks.CAVE_AIR) || 
                currentState.is(Blocks.VOID_AIR) || currentState.liquid()) {
                
                BlockState fillBlock = getFillBlock(level, fillPos, y, targetY, random);
                if (!fillBlock.equals(currentState)) {
                    level.setBlock(fillPos, fillBlock, 2);
                }
            }
        }
    }
    
    /**
     * Determines the appropriate fill block for water replacement.
     * Uses context-aware filling based on surrounding underwater terrain.
     */
    private static BlockState getWaterFillBlock(LevelAccessor level, BlockPos pos, int currentY, int targetY, RandomSource random) {
        int depthFromTarget = targetY - currentY;
        
        // Check what's below to determine appropriate fill
        BlockState belowState = level.getBlockState(pos.below());
        
        // Match the terrain below
        if (belowState.is(Blocks.SAND) || belowState.is(Blocks.SANDSTONE)) {
            return depthFromTarget <= 2 ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
        }
        
        if (belowState.is(Blocks.GRAVEL)) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        
        if (belowState.is(Blocks.CLAY)) {
            return Blocks.CLAY.defaultBlockState();
        }
        
        // Check surrounding blocks for more context
        BlockState northState = level.getBlockState(pos.relative(Direction.NORTH));
        BlockState southState = level.getBlockState(pos.relative(Direction.SOUTH));
        BlockState eastState = level.getBlockState(pos.relative(Direction.EAST));
        BlockState westState = level.getBlockState(pos.relative(Direction.WEST));
        
        // If surrounded by sandy terrain
        if (isSandyTerrain(northState) || isSandyTerrain(southState) || 
            isSandyTerrain(eastState) || isSandyTerrain(westState)) {
            return Blocks.SAND.defaultBlockState();
        }
        
        // Near surface of water column, use dirt
        if (depthFromTarget <= 3) {
            return Blocks.DIRT.defaultBlockState();
        }
        
        // Default to dirt for water replacement
        return Blocks.DIRT.defaultBlockState();
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
            
            // Top layer: prefer grass only when not adjacent to water; otherwise dirt
            if (depthFromTarget == 1) {
                return isWaterNearby(level, pos) ? Blocks.DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
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

    /**
     * Quick heuristic: if this position or immediate neighbors contain fluid, treat it as water-adjacent.
     */
private static boolean isWaterNearby(LevelAccessor level, BlockPos pos) {
        return !level.getFluidState(pos).isEmpty() ||
               !level.getFluidState(pos.above()).isEmpty() ||
               !level.getFluidState(pos.relative(Direction.NORTH)).isEmpty() ||
               !level.getFluidState(pos.relative(Direction.SOUTH)).isEmpty() ||
               !level.getFluidState(pos.relative(Direction.EAST)).isEmpty() ||
               !level.getFluidState(pos.relative(Direction.WEST)).isEmpty();
    }

    public static void fillPotholes(
            LevelAccessor level,
            BoundingBox boundingBox,
            int structureBaseY) {
        int minX = boundingBox.minX();
        int maxX = boundingBox.maxX();
        int minZ = boundingBox.minZ();
        int maxZ = boundingBox.maxZ();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = structureBaseY - 1; y >= structureBaseY - 5; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
