package com.shioh.sengoku.entity;

import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CustomGiant extends Giant {

    public CustomGiant(EntityType<? extends Giant> type, Level world) {
        super(type, world);

        // Set custom attack damage
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0D); // ~4 hearts
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.23D); // slower than default
    }

    // Optional: custom knockback
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            target.push(dx * 0.3, 0.1, dz * 0.3); // small knockback
        }
        return hit;
    }
}
