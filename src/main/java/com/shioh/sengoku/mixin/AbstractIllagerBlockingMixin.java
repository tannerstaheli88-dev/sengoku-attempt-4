package com.shioh.sengoku.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a synced `weapon blocking` flag to AbstractIllager so clients can render
 * the two-handed block pose exactly when the server intends it.
 */
@Mixin(AbstractIllager.class)
public class AbstractIllagerBlockingMixin {
    // Placeholder stub mixin to avoid duplicate/conflicting implementations.
    // The Vindicator-specific mixin handles the synced flag for vindicators.
}
