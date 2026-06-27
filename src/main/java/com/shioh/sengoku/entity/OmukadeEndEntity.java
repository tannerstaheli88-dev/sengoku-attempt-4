package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Dedicated final segment for Omukade.
 * Behavior is identical to OmukadePartEntity; separate entity id allows unique texture/renderer.
 */
public class OmukadeEndEntity extends OmukadePartEntity {

    public OmukadeEndEntity(EntityType<? extends OmukadePartEntity> type, Level level) {
        super(type, level);
    }

    public OmukadeEndEntity(EntityType<? extends OmukadePartEntity> type, Level level, OmukadeEntity parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}
