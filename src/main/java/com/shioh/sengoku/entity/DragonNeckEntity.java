package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Neck segment directly behind the Ender Dragon head.
 * Separate entity type allows a different hitbox size and unique model/renderer.
 */
public class DragonNeckEntity extends DragonPartEntity {

    public DragonNeckEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonNeckEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
