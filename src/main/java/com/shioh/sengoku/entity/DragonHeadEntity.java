package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

/**
 * Front-most head segment for the replacement dragon chain.
 */
public class DragonHeadEntity extends DragonPartEntity {

    public DragonHeadEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
    }

    public DragonHeadEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
