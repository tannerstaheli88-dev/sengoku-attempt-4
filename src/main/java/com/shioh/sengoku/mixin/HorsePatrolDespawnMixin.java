package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.PatrolHorseAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to handle patrol horse despawning and player interaction tracking.
 * Horses spawned by patrols will be killed immediately when their rider dies unless a player interacts with them.
 */
@Mixin(Horse.class)
public class HorsePatrolDespawnMixin implements PatrolHorseAccess {
    @Unique
    private boolean sengoku$needsDespawn = false;
    
    @Unique
    private long sengoku$patrolSpawnTime = 0L;
    
    /**
     * Track when player interacts with horse - removes despawn flag
     */
    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void onPlayerInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        sengoku$needsDespawn = false;
        // Notify manager that a player claimed/interacted with this horse so it shouldn't be killed
        try {
            Horse horse = (Horse)(Object)this;
            com.shioh.sengoku.fabric.HorsePatrolManager.onPlayerInteracted(horse);
            if (horse.getTags().contains("sengoku_needs_despawn")) {
                horse.removeTag("sengoku_needs_despawn");
            }
        } catch (Throwable ignored) {}
    }
    
    // NOTE: Per-entity tick injection was removed because method target differs across mappings
    // and caused startup crashes. Despawn is handled centrally by HorsePatrolManager instead.
    
    /**
     * Save the despawn flag to NBT
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onSaveData(CompoundTag tag, CallbackInfo ci) {
        if (sengoku$needsDespawn) {
            tag.putBoolean("NeedsDespawn", true);
            tag.putLong("PatrolSpawnTime", sengoku$patrolSpawnTime);
        }
    }
    
    /**
     * Load the despawn flag from NBT
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onLoadData(CompoundTag tag, CallbackInfo ci) {
        sengoku$needsDespawn = tag.getBoolean("NeedsDespawn");
        sengoku$patrolSpawnTime = tag.getLong("PatrolSpawnTime");

        // Fallback: if spawner added an entity tag, honor that too
        Horse horse = (Horse)(Object)this;
        try {
            if (!sengoku$needsDespawn && horse.getTags().contains("sengoku_needs_despawn")) {
                sengoku$needsDespawn = true;
                sengoku$patrolSpawnTime = horse.level().getGameTime();
            }
        } catch (Throwable ignored) {}
    }
    
    /**
     * Public method to set despawn flag from spawner
     */
    @Unique
    public void sengoku$setNeedsDespawn(boolean value, long spawnTime) {
        this.sengoku$needsDespawn = value;
        this.sengoku$patrolSpawnTime = spawnTime;
    }
}
