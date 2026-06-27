package com.shioh.sengoku.init;

import com.shioh.sengoku.item.KunaiItem;
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
 * Registry for kunai items (throwable weapons).
 * Includes base kunai and variants with potion effects.
 */
public final class KunaiItemReg {
    
    private KunaiItemReg() {}
    
    // Base kunai item
    public static final Item KUNAI = new KunaiItem(5.0, new Item.Properties().durability(64));
    
    // Tipped kunai variants - with default potion components
    public static final Item POISON_KUNAI = new KunaiItem(5.0, new Item.Properties()
        .durability(64)
        .component(DataComponents.POTION_CONTENTS, new PotionContents(
            java.util.Optional.empty(),
            java.util.Optional.of(0x4E9331), // Green color
            java.util.List.of(new MobEffectInstance(MobEffects.POISON, 200, 0)) // 10 seconds
        ))
    );
    
    public static final Item WEAKNESS_KUNAI = new KunaiItem(5.0, new Item.Properties()
        .durability(64)
        .component(DataComponents.POTION_CONTENTS, new PotionContents(
            java.util.Optional.empty(),
            java.util.Optional.of(0x484D48), // Gray color
            java.util.List.of(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0)) // 15 seconds
        ))
    );
    
    public static final Item SLOWNESS_KUNAI = new KunaiItem(5.0, new Item.Properties()
        .durability(64)
        .component(DataComponents.POTION_CONTENTS, new PotionContents(
            java.util.Optional.empty(),
            java.util.Optional.of(0x5A6C81), // Blue-gray color
            java.util.List.of(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0)) // 20 seconds
        ))
    );
    
    public static void registerItems() {
        // Register kunai items
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("kunai"), KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("poison_kunai"), POISON_KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("weakness_kunai"), WEAKNESS_KUNAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("slowness_kunai"), SLOWNESS_KUNAI);
        
        // Add to combat creative tab after tridents
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
            .register(entries -> {
                entries.addAfter(net.minecraft.world.item.Items.TRIDENT, KUNAI);
                entries.addAfter(KUNAI, POISON_KUNAI);
                entries.addAfter(POISON_KUNAI, WEAKNESS_KUNAI);
                entries.addAfter(WEAKNESS_KUNAI, SLOWNESS_KUNAI);
            });
    }
}
