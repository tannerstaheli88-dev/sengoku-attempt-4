package com.shioh.sengoku.particle;

import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModParticles {
    
    // Fast-dissipating musket smoke particle
    public static final SimpleParticleType MUSKET_SMOKE = FabricParticleTypes.simple();
    
    // Brief muzzle flash particle
    public static final SimpleParticleType GUNFIRE_FLASH = FabricParticleTypes.simple();
    
    // Replacement particle for dragon breath visuals
    public static final SimpleParticleType DRAGON_SPLASH = FabricParticleTypes.simple();
    
    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, 
            ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "musket_smoke"), 
            MUSKET_SMOKE);
        
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, 
            ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "gunfire_flash"), 
            GUNFIRE_FLASH);

        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
            ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_splash"),
            DRAGON_SPLASH);
        
        sengokuFabric.LOGGER.info("Registered custom musket particles");
        sengokuFabric.LOGGER.info("Registered dragon_splash particle");
    }
}
