package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class FoxSafeAvoidanceMixin {

    private static final TagKey<Structure> FOX_SAFE = TagKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath("sengoku", "fox_safe")
    );

    @Unique
    private int sengoku$fsCheckCooldown = 0;

@Inject(method = "aiStep", at = @At("TAIL"))
private void sengoku$avoidFoxSafe(CallbackInfo ci) {
    Mob self = (Mob) (Object) this;

    boolean isUntamedWolf = self instanceof Wolf wolf && !wolf.isTame();
    boolean isPolarBear = self instanceof PolarBear;
    if (!isUntamedWolf && !isPolarBear) return;

    if (!(self.level() instanceof ServerLevel serverLevel)) return;
    if (sengoku$fsCheckCooldown-- > 0) return;
    sengoku$fsCheckCooldown = 10;

    BlockPos pos = self.blockPosition();

    // Check if inside any structure in the fox_safe tag
    var structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
    StructureManager structureManager = serverLevel.structureManager();

    StructureStart foundStart = null;
    for (var holder : structureRegistry.getTagOrEmpty(FOX_SAFE)) {
        StructureStart start = structureManager.getStructureAt(pos, holder.value());
        if (start != null && start.isValid() && start.getBoundingBox().isInside(pos)) {
            foundStart = start;
            break;
        }
    }
    if (foundStart == null) return;

    var box = foundStart.getBoundingBox();
    double centerX = (box.minX() + box.maxX()) / 2.0;
    double centerZ = (box.minZ() + box.maxZ()) / 2.0;
    double dx = pos.getX() - centerX;
    double dz = pos.getZ() - centerZ;
    double len = Math.sqrt(dx * dx + dz * dz);
    if (len < 0.001) { dx = 1; dz = 0; len = 1; }

    double spanX = (box.maxX() - box.minX()) / 2.0 + 16;
    double spanZ = (box.maxZ() - box.minZ()) / 2.0 + 16;
    double dist = Math.max(spanX, spanZ);

    double targetX = centerX + (dx / len) * (dist + 8);
    double targetZ = centerZ + (dz / len) * (dist + 8);

    self.getNavigation().moveTo(targetX, pos.getY(), targetZ, 1.0);
}
}