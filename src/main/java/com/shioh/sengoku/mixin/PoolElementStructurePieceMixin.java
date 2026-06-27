package com.shioh.sengoku.mixin;

import com.shioh.sengoku.worldgen.AdjustableBeardBox;
import com.shioh.sengoku.worldgen.AdjustableBeardRegistry;
import com.shioh.sengoku.worldgen.AdjustableBeardSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoolElementStructurePiece.class)
public abstract class PoolElementStructurePieceMixin {
    @Inject(
        method = "<init>(Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/world/level/levelgen/structure/pools/StructurePoolElement;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/level/block/Rotation;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)V",
        at = @At("RETURN"),
        require = 0
    )
    private void sengoku$registerAdjustableBeardFromPlacement(
        StructureTemplateManager structureTemplateManager,
        StructurePoolElement poolElement,
        BlockPos pos,
        int groundLevelDelta,
        Rotation rotation,
        BoundingBox boundingBox,
        LiquidSettings liquidSettings,
        CallbackInfo ci
    ) {
        sengoku$registerIfAdjustable(poolElement, boundingBox);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("RETURN"),
        require = 0
    )
    private void sengoku$registerAdjustableBeardFromNbt(StructurePieceSerializationContext context, CompoundTag nbt, CallbackInfo ci) {
        PoolElementStructurePiece self = (PoolElementStructurePiece) (Object) this;
        sengoku$registerIfAdjustable(self.getElement(), self.getBoundingBox());
    }

    private static void sengoku$registerIfAdjustable(StructurePoolElement poolElement, BoundingBox boundingBox) {
        if (poolElement instanceof AdjustableBeardSettings settings) {
            AdjustableBeardRegistry.add(new AdjustableBeardBox(
                boundingBox,
                settings.getHorizontalBeardRadius(),
                settings.getBeardStrength(),
                settings.getCarveStrength()
            ));
        }
    }
}
