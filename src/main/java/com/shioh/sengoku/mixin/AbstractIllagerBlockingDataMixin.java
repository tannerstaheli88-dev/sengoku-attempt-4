package com.shioh.sengoku.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Add a synced boolean to AbstractIllager so all illager-derived entities
 * (including modded samurai/ronin if they extend AbstractIllager) receive
 * a reliable client-side flag we can use to render the two-handed block pose.
 */
@Mixin(AbstractIllager.class)
public abstract class AbstractIllagerBlockingDataMixin extends Mob {
    @Shadow
    private SynchedEntityData entityData;

    private static final EntityDataAccessor<Boolean> SENGOKU_WEAPON_BLOCKING = SynchedEntityData.defineId(AbstractIllager.class, EntityDataSerializers.BOOLEAN);

    protected AbstractIllagerBlockingDataMixin(EntityType<? extends Mob> type, Level level) { super(type, level); }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void sengoku$defineData(net.minecraft.network.syncher.SynchedEntityData.Builder builder, CallbackInfo ci) {
        try {
            builder.define(SENGOKU_WEAPON_BLOCKING, Boolean.valueOf(false));
        } catch (Throwable ignored) {}
    }

    public void sengoku$setWeaponBlocking(boolean v) {
        try { this.entityData.set(SENGOKU_WEAPON_BLOCKING, Boolean.valueOf(v)); } catch (Throwable ignored) {}
    }

    public boolean sengoku$isWeaponBlocking() {
        try { return this.entityData.get(SENGOKU_WEAPON_BLOCKING); } catch (Throwable ignored) { return false; }
    }
}
