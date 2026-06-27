package com.shioh.sengoku.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TerrainAdaptationOverridePoolElement extends StructurePoolElement implements TerrainAdaptationOverrideSettings {
    public static final MapCodec<TerrainAdaptationOverridePoolElement> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            StructurePoolElement.CODEC.fieldOf("delegate").forGetter(element -> element.delegate),
            TerrainAdjustment.CODEC.fieldOf("terrain_adaptation").forGetter(TerrainAdaptationOverridePoolElement::terrainAdjustment)
        ).apply(instance, TerrainAdaptationOverridePoolElement::new)
    );

    private final StructurePoolElement delegate;
    private final TerrainAdjustment terrainAdjustment;

    public TerrainAdaptationOverridePoolElement(StructurePoolElement delegate, TerrainAdjustment terrainAdjustment) {
        super(delegate.getProjection());
        this.delegate = delegate;
        this.terrainAdjustment = terrainAdjustment;
    }

    @Override
    public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
        return delegate.getSize(structureTemplateManager, rotation);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
        StructureTemplateManager structureTemplateManager,
        BlockPos pos,
        Rotation rotation,
        RandomSource random
    ) {
        return delegate.getShuffledJigsawBlocks(structureTemplateManager, pos, rotation, random);
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation) {
        return delegate.getBoundingBox(structureTemplateManager, pos, rotation);
    }

    @Override
    public boolean place(
        StructureTemplateManager structureTemplateManager,
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        BlockPos offset,
        BlockPos pos,
        Rotation rotation,
        BoundingBox box,
        RandomSource random,
        LiquidSettings liquidSettings,
        boolean keepJigsaws
    ) {
        return delegate.place(
            structureTemplateManager,
            level,
            structureManager,
            generator,
            offset,
            pos,
            rotation,
            box,
            random,
            liquidSettings,
            keepJigsaws
        );
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructurePoolElements.TERRAIN_ADAPTATION_OVERRIDE;
    }

    @Override
    public int getGroundLevelDelta() {
        return delegate.getGroundLevelDelta();
    }

    @Override
    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        return new TerrainAdaptationOverridePoolElement(delegate.setProjection(projection), terrainAdjustment);
    }

    @Override
    public Optional<TerrainAdjustment> getTerrainAdjustmentOverride() {
        return Optional.of(terrainAdjustment);
    }

    public TerrainAdjustment terrainAdjustment() {
        return terrainAdjustment;
    }

    @Override
    public String toString() {
        return "TerrainAdaptationOverridePoolElement[" + delegate + ", " + terrainAdjustment.getSerializedName() + "]";
    }
}