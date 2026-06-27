package com.shioh.sengoku.mixin;

import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndDragonFight.class)
public abstract class EndDragonFightPortalScanFixMixin {

    /**
     * During legacy state scan, ignore pattern-only bedrock matches so the normal
     * exit fountain gets spawned when there is no active portal block entity.
     */
    @Redirect(
        method = "scanState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;findExitPortal()Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;"
        )
    )
    private BlockPattern.BlockPatternMatch sengoku$ignoreExitPortalPatternDuringLegacyScan(EndDragonFight instance) {
        return null;
    }
}