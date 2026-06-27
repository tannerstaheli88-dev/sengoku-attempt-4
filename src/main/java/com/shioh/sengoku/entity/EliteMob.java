package com.shioh.sengoku.entity;

/**
 * Lightweight interface used by mixins to mark and query "elite" entities.
 * Restored to satisfy compile-time references; implementations provide
 * persistent behavior where appropriate.
 */
public interface EliteMob {
    boolean sengoku$isElite();
    void sengoku$setElite(boolean elite);
}
