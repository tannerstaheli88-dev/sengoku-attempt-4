package com.shioh.sengoku.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Mist effect - similar to darkness but white/grey fog instead of black
 * Creates atmospheric mist weather effect
 * Hidden from inventory HUD via MistEffectHideMixin
 * shits actually dead in the water, doesnt work. 
 */
public class MistEffect extends MobEffect {
    
    public MistEffect() {
        super(MobEffectCategory.NEUTRAL, 0xC0C0C0); // Light grey color
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Effect is purely visual via renderer
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Don't need to do anything every tick
        return false;
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}
