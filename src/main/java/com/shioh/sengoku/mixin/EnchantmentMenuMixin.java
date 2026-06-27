package com.shioh.sengoku.mixin;

import com.shioh.sengoku.item.TantoItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {

    @Shadow
    private net.minecraft.util.RandomSource random;

    @Shadow
    private DataSlot enchantmentSeed;

    @Inject(method = "getEnchantmentList(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void sengoku$addSwiftSneak(net.minecraft.core.RegistryAccess registryAccess, ItemStack stack, int slot, int cost, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        // Only modify when the input stack is a Sengoku Tanto
        if (!(stack.getItem() instanceof TantoItem)) return;

        this.random.setSeed(this.enchantmentSeed.get() + slot);

        Optional<HolderSet.Named<Enchantment>> optional = registryAccess.registryOrThrow(Registries.ENCHANTMENT).getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE);
        if (optional.isEmpty()) {
            cir.setReturnValue(List.of());
            return;
        }

        // Collect base holders and ensure Swift Sneak is present exactly once
        List<Holder<Enchantment>> holders = ((HolderSet.Named<Enchantment>) optional.get()).stream().collect(Collectors.toCollection(ArrayList::new));
        registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.SWIFT_SNEAK).ifPresent(h -> {
            if (!holders.contains(h)) holders.add(h);
        });

        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, stack, cost, holders.stream());

        // Ensure Swift Sneak is present for Tantos — add as a fallback if selection didn't include it.
        boolean hasSwift = false;
        for (EnchantmentInstance ei : list) {
            if (ei.enchantment == Enchantments.SWIFT_SNEAK) { hasSwift = true; break; }
        }
        if (!hasSwift) {
            // Add a safe level 1 Swift Sneak instance so the table can offer it for Tantos.
            list = new ArrayList<>(list);
            var swiftHolderOpt = registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.SWIFT_SNEAK);
            if (swiftHolderOpt.isPresent()) {
                list.add(new EnchantmentInstance(swiftHolderOpt.get(), 1));
            }
        }

        if (stack.is(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        cir.setReturnValue(list);
    }
}
