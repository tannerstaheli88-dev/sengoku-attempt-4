package com.shioh.sengoku.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponentType;

import com.mojang.serialization.Codec;

public class ModDataComponents {
    public static final DataComponentType<Integer> POSTURE = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        ResourceLocation.fromNamespaceAndPath("sengoku", "posture"),
        DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .build()
    );

    // Posture damage dealt to blocking entities when attacking with this weapon
    public static final DataComponentType<Integer> WEAPON_POSTURE_DAMAGE = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        ResourceLocation.fromNamespaceAndPath("sengoku", "weapon_posture_damage"),
        DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .build()
    );

    public static void register() {
        // trigger class load
    }
}
