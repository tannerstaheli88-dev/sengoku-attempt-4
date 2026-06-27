package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Parrot.Variant;
import net.minecraft.world.entity.animal.Parrot.Variant;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Ensure parrots spawned as a group share the same color variant.
 */
@Mixin(Parrot.class)
public abstract class ParrotSpawnVariantMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void sengoku$applyGroupVariant(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Parrot self = (Parrot) (Object) this;

        try {
            // Apply biome-driven variant when spawning in our tagged biomes (no return value changes)
            net.minecraft.core.BlockPos pos = self.blockPosition();
            try {
                net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> greenTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_green_birds"));
                net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> yellowTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_yellow_birds"));
                net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> redBlueTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_red_blue_birds"));
                net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> greyTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_grey_birds"));
                net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> blueTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_blue_birds"));

                // Explicit numeric variant IDs:
                // 0 = red_blue, 1 = blue, 2 = green, 3 = yellow, 4 = grey
                int chosenId = -1;
                if (level.getBiome(pos).is(greenTag)) {
                    chosenId = 2;
                } else if (level.getBiome(pos).is(yellowTag)) {
                    chosenId = 3;
                } else if (level.getBiome(pos).is(redBlueTag)) {
                    // pick red_blue (0) or blue (1) randomly
                    try { chosenId = (self.getRandom().nextBoolean()) ? 0 : 1; } catch (Throwable ignoredInner) {}
                } else if (level.getBiome(pos).is(greyTag)) {
                    chosenId = 4;
                } else if (level.getBiome(pos).is(blueTag)) {
                    chosenId = 1;
                }

                if (chosenId >= 0) {
                    try {
                        self.setVariant(Variant.byId(chosenId));
                        try {
                            java.lang.reflect.Method m = self.getClass().getMethod("getPersistentData");
                            Object tag = m.invoke(self);
                            if (tag instanceof net.minecraft.nbt.CompoundTag) {
                                ((net.minecraft.nbt.CompoundTag) tag).putInt("Variant", chosenId);
                            }
                        } catch (Throwable ignoredInner) {}
                    } catch (Throwable ignoredInner) {}
                }
            } catch (Throwable ignoredInner) {}
        } catch (Throwable ignored) {}
    }
}
