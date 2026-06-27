package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Extra trailing tail segment used after the dedicated end piece.
 */
public class DragonTailEntity extends DragonPartEntity {

    public DragonTailEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonTailEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
