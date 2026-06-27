package com.shioh.sengoku.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ParticleRegistry {
    public static final SimpleParticleType POSTURE_SPARK = register(
            "posture_spark",
            new SimpleParticleType(false) {} // ✅ subclass to bypass protected constructor
    );

    public static final SimpleParticleType BLOCK_PARTICLE = register(
            "block_particle",
            new SimpleParticleType(false) {} // ✅ subclass again
    );

    public static final SimpleParticleType FOG = register(
            "fog",
            new SimpleParticleType(false) {}
    );

    public static final SimpleParticleType FOG_FLAT = register(
            "fog_flat",
            new SimpleParticleType(false) {}
    );

    public static final SimpleParticleType FOG_MIST = register(
            "fog_mist",
            new SimpleParticleType(false) {}
    );

    public static final SimpleParticleType BLOOD_PARTICLE = register(
            "blood_particle",
            new SimpleParticleType(false) {}
    );

    public static final SimpleParticleType DETECTION_PARTICLE = register(
            "detection_particle",
            new SimpleParticleType(false) {}
    );

    public static final SimpleParticleType FLOWING_LEAVES = register(
            "flowing_leaves",
            new SimpleParticleType(false) {}
    );

private static <T extends ParticleType<?>> T register(String name, T type) {
    return Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            ResourceLocation.fromNamespaceAndPath("sengoku", name),
            type
    );
}


    public static void init() {
        // call this during common setup
    }
}
