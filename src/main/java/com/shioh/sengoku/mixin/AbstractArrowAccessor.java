package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    
    @Accessor("life")
    int getLife();
    
    @Accessor("life")
    void setLife(int life);
    
    @Accessor("inGround")
    boolean isInGround();
}
