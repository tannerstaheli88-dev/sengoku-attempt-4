package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Dedicated final segment for the dragon tail chain.
 * Behavior is identical to DragonPartEntity; separate entity id allows unique model/renderer.
 */
public class DragonPartEndEntity extends DragonPartEntity {

    public DragonPartEndEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonPartEndEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
