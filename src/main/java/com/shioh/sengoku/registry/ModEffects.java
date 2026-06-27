package com.shioh.sengoku.registry;

import com.shioh.sengoku.effect.MistEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {
    
    public static final MobEffect MIST = register("mist", new MistEffect());
    
    private static MobEffect register(String name, MobEffect effect) {
        return Registry.register(BuiltInRegistries.MOB_EFFECT, 
            ResourceLocation.fromNamespaceAndPath("sengoku", name), 
            effect);
    }
    
    public static void initialize() {
        // Called to ensure class is loaded
    }
}
