package com.shioh.sengoku.mixin;

import com.shioh.sengoku.worldgen.AdjustableBeardRegistry;
import com.shioh.sengoku.worldgen.TerrainAdaptationOverrideResolver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Beardifier.class)
public abstract class BeardifierMixin {
    @Inject(
        method = "forStructuresInChunk",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void sengoku$allowPieceTerrainOverrides(
        StructureManager structureManager,
        ChunkPos chunkPos,
        CallbackInfoReturnable<Beardifier> cir
    ) {
        int minBlockX = chunkPos.getMinBlockX();
        int minBlockZ = chunkPos.getMinBlockZ();
        ObjectList<Beardifier.Rigid> rigidPieces = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> junctions = new ObjectArrayList<>(32);

        for (StructureStart structureStart : structureManager.startsForStructure(chunkPos, structure -> true)) {
            TerrainAdjustment structureAdjustment = structureStart.getStructure().terrainAdaptation();

            for (StructurePiece piece : structureStart.getPieces()) {
                if (!piece.isCloseToChunk(chunkPos, 12)) {
                    continue;
                }

                if (piece instanceof PoolElementStructurePiece poolPiece) {
                    StructurePoolElement element = poolPiece.getElement();
                    TerrainAdjustment pieceAdjustment = TerrainAdaptationOverrideResolver.resolveOrDefault(element, structureAdjustment);

                    if (element.getProjection() == StructureTemplatePool.Projection.RIGID && pieceAdjustment != TerrainAdjustment.NONE) {
                        rigidPieces.add(new Beardifier.Rigid(
                            poolPiece.getBoundingBox(),
                            pieceAdjustment,
                            poolPiece.getGroundLevelDelta()
                        ));
                    }

                    if (pieceAdjustment == TerrainAdjustment.NONE) {
                        continue;
                    }

                    for (JigsawJunction junction : poolPiece.getJunctions()) {
                        int sourceX = junction.getSourceX();
                        int sourceZ = junction.getSourceZ();
                        if (sourceX <= minBlockX - 12 || sourceZ <= minBlockZ - 12) {
                            continue;
                        }

                        if (sourceX >= minBlockX + 15 + 12 || sourceZ >= minBlockZ + 15 + 12) {
                            continue;
                        }

                        junctions.add(junction);
                    }

                    continue;
                }

                if (structureAdjustment == TerrainAdjustment.NONE) {
                    continue;
                }

                rigidPieces.add(new Beardifier.Rigid(piece.getBoundingBox(), structureAdjustment, 0));
            }
        }

        cir.setReturnValue(new Beardifier(rigidPieces.iterator(), junctions.iterator()));
    }

    @Inject(
        method = "compute(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void sengoku$addAdjustableBeardContribution(DensityFunction.FunctionContext functionContext, CallbackInfoReturnable<Double> cir) {
        double contribution = AdjustableBeardRegistry.computeContribution(
            functionContext.blockX(),
            functionContext.blockY(),
            functionContext.blockZ()
        );
        cir.setReturnValue(cir.getReturnValue() + contribution);
    }
}
