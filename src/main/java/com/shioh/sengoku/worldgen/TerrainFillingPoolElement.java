package com.shioh.sengoku.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * A custom structure pool element that fills terrain irregularities before placing structures.
 * This behaves like a terrain_matching projection but preprocesses the area by filling
 * holes with dirt and stone to create a smoother foundation.
 */
public class TerrainFillingPoolElement extends SinglePoolElement {
    
    private final java.util.Optional<LiquidSettings> liquidSettings;
    
    public static final MapCodec<TerrainFillingPoolElement> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            templateCodec(),
            processorsCodec(),
            projectionCodec(),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings").forGetter(element -> element.liquidSettings)
        ).apply(instance, TerrainFillingPoolElement::new)
    );
    
    protected TerrainFillingPoolElement(
            Either<ResourceLocation, StructureTemplate> template,
            Holder<StructureProcessorList> processors,
            StructureTemplatePool.Projection projection,
            java.util.Optional<LiquidSettings> liquidSettings) {
        super(template, processors, projection, liquidSettings);
        this.liquidSettings = liquidSettings;
    }
    
    /**
     * Places the structure with terrain filling.
     */
    @Override
    public boolean place(
            StructureTemplateManager templateManager,
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            BlockPos pos,
            BlockPos pivot,
            Rotation rotation,
            BoundingBox boundingBox,
            RandomSource random,
            LiquidSettings liquidSettings,
            boolean keepJigsaws) {
        
        // Get the template
        StructureTemplate template = this.template.map(templateManager::getOrCreate, t -> t);
        if (template == null) {
            return false;
        }
        
        // Create placement settings
        StructurePlaceSettings settings = this.getSettings(rotation, boundingBox, liquidSettings, keepJigsaws);
        
        // Calculate the actual bounding box for the structure
        Vec3i size = template.getSize(rotation);
        BlockPos adjustedPos = pos;
        BoundingBox structureBox = template.getBoundingBox(settings, adjustedPos);
        
        // Fill terrain irregularities before placement
        TerrainFillingProjection.fillTerrainBeforePlacement(
            level, template, settings, adjustedPos, structureBox, random
        );
        
        // Place the structure normally using parent's placement logic
        return super.place(templateManager, level, structureManager, chunkGenerator, 
                          pos, pivot, rotation, boundingBox, random, liquidSettings, keepJigsaws);
    }
    
    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructurePoolElements.TERRAIN_FILLING;
    }
    
    @Override
    public String toString() {
        return "TerrainFillingPoolElement[" + this.template + "]";
    }
}
