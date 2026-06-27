package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Thinner late-body segment used after the second arms pair.
 */
public class DragonPartThinEntity extends DragonPartEntity {

    public DragonPartThinEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonPartThinEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}