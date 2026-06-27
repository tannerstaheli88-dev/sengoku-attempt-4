package com.shioh.sengoku.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * IkuchiEndEntity – the tail tip of the Ikuchi sea eel.
 * Behavior identical to IkuchiPartEntity; separate entity ID allows a unique tail texture/renderer.
 */
public class IkuchiEndEntity extends IkuchiPartEntity {

    public IkuchiEndEntity(EntityType<? extends IkuchiPartEntity> type, Level level) {
        super(type, level);
    }

    public IkuchiEndEntity(EntityType<? extends IkuchiPartEntity> type, Level level, IkuchiEntity parent, int partNumber) {
        super(type, level, parent, partNumber);
    }
}