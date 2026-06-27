package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Arms/limb segment for the dragon chain (appears at shoulders and hips).
 * Separate entity type allows a wider hitbox and unique model/renderer.
 */
public class DragonArmsEntity extends DragonPartEntity {

    public DragonArmsEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonArmsEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
