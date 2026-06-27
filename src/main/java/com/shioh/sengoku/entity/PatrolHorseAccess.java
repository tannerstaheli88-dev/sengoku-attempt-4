package com.shioh.sengoku.entity;

/**
 * Interface for accessing patrol horse despawn functionality added by HorsePatrolDespawnMixin.
 */
public interface PatrolHorseAccess {
    void sengoku$setNeedsDespawn(boolean value, long spawnTime);
}
