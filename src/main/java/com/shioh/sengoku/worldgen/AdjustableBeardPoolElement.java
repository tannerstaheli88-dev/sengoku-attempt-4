package com.shioh.sengoku.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class AdjustableBeardPoolElement extends SinglePoolElement implements AdjustableBeardSettings {
    public static final MapCodec<AdjustableBeardPoolElement> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            templateCodec(),
            processorsCodec(),
            projectionCodec(),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings").forGetter(element -> element.liquidSettings),
            Codec.INT.optionalFieldOf("horizontal_beard_radius", 8).forGetter(AdjustableBeardPoolElement::getHorizontalBeardRadius),
            Codec.FLOAT.optionalFieldOf("beard_strength", 1.0F).forGetter(AdjustableBeardPoolElement::getBeardStrength),
            Codec.FLOAT.optionalFieldOf("carve_strength", 0.0F).forGetter(AdjustableBeardPoolElement::getCarveStrength)
        ).apply(instance, AdjustableBeardPoolElement::new)
    );

    private final java.util.Optional<LiquidSettings> liquidSettings;
    private final int horizontalBeardRadius;
    private final float beardStrength;
    private final float carveStrength;

    protected AdjustableBeardPoolElement(
        Either<ResourceLocation, StructureTemplate> template,
        Holder<StructureProcessorList> processors,
        StructureTemplatePool.Projection projection,
        java.util.Optional<LiquidSettings> liquidSettings,
        int horizontalBeardRadius,
        float beardStrength,
        float carveStrength
    ) {
        super(template, processors, projection, liquidSettings);
        this.liquidSettings = liquidSettings;
        this.horizontalBeardRadius = Math.max(1, horizontalBeardRadius);
        this.beardStrength = beardStrength;
        this.carveStrength = carveStrength;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructurePoolElements.ADJUSTABLE_BEARD_POOL_ELEMENT;
    }

    @Override
    public int getHorizontalBeardRadius() {
        return horizontalBeardRadius;
    }

    @Override
    public float getBeardStrength() {
        return beardStrength;
    }

    @Override
    public float getCarveStrength() {
        return carveStrength;
    }

    @Override
    public String toString() {
        return "AdjustableBeardPoolElement[" + this.template + "]";
    }
}
