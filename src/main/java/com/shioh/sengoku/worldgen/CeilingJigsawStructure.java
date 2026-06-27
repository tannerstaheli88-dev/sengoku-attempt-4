package com.shioh.sengoku.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shioh.sengoku.sengokuFabric;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.NoiseColumn;

/**
 * A Jigsaw structure variant that supports a ceiling-aware start height mode.
 * This mirrors Nether fossil placement by probing downward in the target column
 * until it finds an air pocket resting on solid terrain.
 */
public class CeilingJigsawStructure extends Structure {
    public static final MapCodec<CeilingJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 20).fieldOf("size").forGetter(structure -> structure.maxDepth),
            StartHeight.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.INT.optionalFieldOf("max_distance_from_center", 80).forGetter(structure -> structure.maxDistanceFromCenter),
            PoolAliasBinding.CODEC.listOf().optionalFieldOf("pool_aliases", List.of()).forGetter(structure -> structure.poolAliases),
            DimensionPadding.CODEC.optionalFieldOf("dimension_padding", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
        ).apply(instance, CeilingJigsawStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final StartHeight startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public CeilingJigsawStructure(
        StructureSettings settings,
        Holder<StructureTemplatePool> startPool,
        Optional<ResourceLocation> startJigsawName,
        int maxDepth,
        StartHeight startHeight,
        boolean useExpansionHack,
        Optional<Heightmap.Types> projectStartToHeightmap,
        int maxDistanceFromCenter,
        List<PoolAliasBinding> poolAliases,
        DimensionPadding dimensionPadding,
        LiquidSettings liquidSettings
    ) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int startY = this.startHeight.resolveY(context, chunkPos);
        if (startY == Integer.MIN_VALUE) {
            return Optional.empty();
        }

        BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());
        return JigsawPlacement.addPieces(
            context,
            this.startPool,
            this.startJigsawName,
            this.maxDepth,
            startPos,
            this.useExpansionHack,
            this.projectStartToHeightmap,
            this.maxDistanceFromCenter,
            PoolAliasLookup.create(this.poolAliases, startPos, context.seed()),
            this.dimensionPadding,
            this.liquidSettings
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.CEILING_JIGSAW;
    }

    private interface StartHeight {
        Codec<StartHeight> CODEC = Codec.either(HeightProvider.CODEC, CeilingSearchStartHeight.CODEC).xmap(
            either -> either.map(VanillaStartHeight::new, Function.identity()),
            startHeight -> {
                if (startHeight instanceof VanillaStartHeight vanillaStartHeight) {
                    return Either.left(vanillaStartHeight.heightProvider());
                }
                return Either.right((CeilingSearchStartHeight) startHeight);
            }
        );

        int resolveY(GenerationContext context, ChunkPos chunkPos);
    }

    private record VanillaStartHeight(HeightProvider heightProvider) implements StartHeight {
        @Override
        public int resolveY(GenerationContext context, ChunkPos chunkPos) {
            return this.heightProvider.sample(
                context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())
            );
        }
    }

    private record CeilingSearchStartHeight(
        VerticalAnchor minInclusive,
        VerticalAnchor maxInclusive,
        boolean allowSoulSand
    ) implements StartHeight {
        private static final ResourceLocation TYPE = sengokuFabric.asId("ceiling_search");
        private static final Codec<ResourceLocation> TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> TYPE.equals(id)
                ? DataResult.success(id)
                : DataResult.error(() -> "Expected start_height type " + TYPE + ", got " + id),
            Function.identity()
        );
        private static final Codec<CeilingSearchStartHeight> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                TYPE_CODEC.fieldOf("type").forGetter(settings -> TYPE),
                VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(CeilingSearchStartHeight::minInclusive),
                VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(CeilingSearchStartHeight::maxInclusive),
                Codec.BOOL.optionalFieldOf("allow_soul_sand", false).forGetter(CeilingSearchStartHeight::allowSoulSand)
            ).apply(instance, (type, minInclusive, maxInclusive, allowSoulSand) -> new CeilingSearchStartHeight(minInclusive, maxInclusive, allowSoulSand))
        );

        @Override
        public int resolveY(GenerationContext context, ChunkPos chunkPos) {
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            RandomState randomState = context.randomState();
            RandomSource random = context.random();
            WorldGenerationContext worldContext = new WorldGenerationContext(chunkGenerator, context.heightAccessor());
            int minBuildHeight = context.heightAccessor().getMinBuildHeight();
            int maxBuildHeight = context.heightAccessor().getMaxBuildHeight() - 1;
            int minY = Math.max(Math.min(this.minInclusive.resolveY(worldContext), this.maxInclusive.resolveY(worldContext)), minBuildHeight);
            int maxY = Math.min(Math.max(this.minInclusive.resolveY(worldContext), this.maxInclusive.resolveY(worldContext)), maxBuildHeight);
            if (minY >= maxY) {
                return Integer.MIN_VALUE;
            }

            int sampledY = minY + random.nextInt(maxY - minY + 1);
            int x = chunkPos.getMinBlockX();
            int z = chunkPos.getMinBlockZ();
            NoiseColumn column = chunkGenerator.getBaseColumn(x, z, context.heightAccessor(), randomState);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, sampledY, z);
            int y = sampledY;

            while (y > minY) {
                BlockState currentState = column.getBlock(y);
                y--;
                BlockState belowState = column.getBlock(y);
                if (!currentState.isAir()) {
                    continue;
                }
                if (!this.allowSoulSand && belowState.is(Blocks.SOUL_SAND)) {
                    continue;
                }
                if (belowState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutablePos.setY(y), Direction.UP)) {
                    return y;
                }
            }

            return Integer.MIN_VALUE;
        }
    }
}