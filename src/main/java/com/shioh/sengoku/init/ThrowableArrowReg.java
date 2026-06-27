package com.shioh.sengoku.init;

import com.shioh.sengoku.item.ThrowableTippedArrow;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Registers throwable kunai.
 */
public final class ThrowableArrowReg {
    
    private ThrowableArrowReg() {}
    
    // Kunai variants
    // Basic kunai with no effects
    public static final Item KUNAI = new ThrowableTippedArrow(
        new Item.Properties()
    );
    
    public static final Item POISON_KUNAI = new ThrowableTippedArrow(
        new Item.Properties()
            .component(DataComponents.POTION_CONTENTS, new PotionContents(
                java.util.Optional.empty(),
                java.util.Optional.of(0x4E9331),
                java.util.List.of(new MobEffectInstance(MobEffects.POISON, 220, 0))
            ))
    );
    
    public static final Item WEAKNESS_KUNAI = new ThrowableTippedArrow(
        new Item.Properties()
            .component(DataComponents.POTION_CONTENTS, new PotionContents(
                java.util.Optional.empty(),
                java.util.Optional.of(0x484D48),
                java.util.List.of(new MobEffectInstance(MobEffects.WEAKNESS, 1800, 0))
            ))
    );
    
    public static final Item SLOWNESS_KUNAI = new ThrowableTippedArrow(
        new Item.Properties()
            .component(DataComponents.POTION_CONTENTS, new PotionContents(
                java.util.Optional.empty(),
                java.util.Optional.of(0x5A6C81),
                java.util.List.of(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1800, 0))
            ))
    );
    
    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("kunai"), KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("poison_kunai"), POISON_KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("weakness_kunai"), WEAKNESS_KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("slowness_kunai"), SLOWNESS_KUNAI);
        
        // Add to combat creative tab
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
            .register(entries -> {
                entries.addAfter(net.minecraft.world.item.Items.TIPPED_ARROW, KUNAI);
                entries.addAfter(KUNAI, POISON_KUNAI);
                entries.addAfter(POISON_KUNAI, WEAKNESS_KUNAI);
                entries.addAfter(WEAKNESS_KUNAI, SLOWNESS_KUNAI);
            });
        
        sengokuFabric.LOGGER.info("Registered throwable kunai");
    }
}
