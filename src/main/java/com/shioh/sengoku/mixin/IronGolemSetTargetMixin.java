package com.shioh.sengoku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.animal.IronGolem;

/**
 * Legacy mixin placeholder for IronGolem targeting.
 *
 * The original injection targeted a method that does not exist on the
 * `IronGolem` class in these mappings and caused startup crashes.
 * The generic `MobSetTargetMixin` already provides safe suppression for
 * Iron Golem target assignments; keep this no-op mixin as a placeholder
 * to avoid removing the source file (can be deleted later).
 */
@Mixin(IronGolem.class)
public class IronGolemSetTargetMixin {
    // intentionally left blank to avoid invalid injection during mixin apply
}
