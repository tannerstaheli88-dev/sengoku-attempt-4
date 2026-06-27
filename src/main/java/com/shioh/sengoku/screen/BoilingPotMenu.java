package com.shioh.sengoku.screen;

import com.shioh.sengoku.block.entity.BoilingPotEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import com.shioh.sengoku.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.inventory.ContainerData;

/**
 * Menu for Boiling Pot: slots
 * 0 tea input, 1 water input, 2 fuel, 3 result
 */
public class BoilingPotMenu extends AbstractContainerMenu {
    private final BoilingPotEntity entity;
    private final Container container;
    private final ContainerData data;

    public static final int SLOT_TEA = 0;
    public static final int SLOT_WATER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_RESULT = 3;

    public BoilingPotMenu(int id, Inventory playerInv, BoilingPotEntity entity, ContainerData data) {
        super(ModMenuTypes.BOILING_POT, id);
        this.entity = entity;
        this.container = (entity != null) ? entity : new SimpleContainer(4);
        this.data = data == null ? new net.minecraft.world.inventory.SimpleContainerData(4) : data;
        this.addDataSlots(this.data);

        // Input slot (accepts tea leaf for tea recipe, or any food for smoking)
        this.addSlot(new Slot(container, SLOT_TEA, 56, 17) {
            @Override public boolean mayPlace(ItemStack stack) { return true; }
        });
        // Water slot (strict: only plain water potion)
        this.addSlot(new Slot(container, SLOT_WATER, 79, 17) {
            @Override public boolean mayPlace(ItemStack stack) {
                // Allow any potion to be placed in the water slot client-side; server will enforce water-only when processing.
                return stack.is(Items.POTION);
            }
            @Override public int getMaxStackSize() { return 16; }
        });
        // Fuel slot
        this.addSlot(new Slot(container, SLOT_FUEL, 56, 53) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.COAL) || stack.is(Items.CHARCOAL); }
        });
        // Result slot
        this.addSlot(new Slot(container, SLOT_RESULT, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (BoilingPotMenu.this.entity != null) {
                    try { BoilingPotMenu.this.entity.awardAccumulatedExperience(player); } catch (Throwable ignored) {}
                }
            }
        });

        // Player inventory (3 rows) + hotbar
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public BoilingPotEntity getEntity() { return entity; }

    // Client-side constructor used when the client creates the menu from the open packet
    public BoilingPotMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, getEntityFromBuf(playerInv, buf), new net.minecraft.world.inventory.SimpleContainerData(4));
    }

    // Convenience constructor used by MenuType supplier (client-side fallback)
    public BoilingPotMenu(int id, Inventory playerInv) {
        this(id, playerInv, null, new net.minecraft.world.inventory.SimpleContainerData(4));
    }

    private static BoilingPotEntity getEntityFromBuf(Inventory playerInv, FriendlyByteBuf buf) {
        try {
            if (buf == null || buf.readableBytes() < 8) return null;
            BlockPos pos = buf.readBlockPos();
            if (playerInv.player != null && playerInv.player.level() != null) {
                net.minecraft.world.level.Level level = playerInv.player.level();
                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BoilingPotEntity) return (BoilingPotEntity)be;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @Override
    public boolean stillValid(Player player) { return entity != null ? entity.stillValid(player) : true; }

    // Data accessors used by the screen
    public int getLitTime() { return this.data.get(0); }
    public int getLitDuration() { return this.data.get(1); }
    public int getCookTime() { return this.data.get(2); }
    public int getCookTimeTotal() { return this.data.get(3); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            int invStart = 4; // after block slots
            int invEnd = this.slots.size();

            if (index < invStart) {
                // block -> player
                if (!this.moveItemStackTo(stack, invStart, invEnd, true)) return ItemStack.EMPTY;
                // If player took the result slot, award accumulated XP
                if (index == SLOT_RESULT && this.entity != null) {
                    try { this.entity.awardAccumulatedExperience(player); } catch (Throwable ignored) {}
                }
            } else {
                // player -> block
                // Try input slot first for any item
                if (stack.is(Items.POTION)) {
                    PotionContents pc = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (!pc.is(Potions.WATER)) return ItemStack.EMPTY;
                    if (!this.moveItemStackTo(stack, SLOT_WATER, SLOT_WATER + 1, false)) return ItemStack.EMPTY;
                } else if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
                    if (!this.moveItemStackTo(stack, SLOT_FUEL, SLOT_FUEL + 1, false)) return ItemStack.EMPTY;
                } else {
                    // Try main input slot for any other item (smoker recipes)
                    if (!this.moveItemStackTo(stack, SLOT_TEA, SLOT_TEA + 1, false)) return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return original;
    }
}
