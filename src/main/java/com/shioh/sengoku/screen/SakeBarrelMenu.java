package com.shioh.sengoku.screen;

import com.shioh.sengoku.block.entity.SakeBarrelEntity;
import com.shioh.sengoku.item.FermentedSakeItem;
import com.shioh.sengoku.init.TansuBlockReg;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SakeBarrelMenu extends AbstractContainerMenu {
    private final SakeBarrelEntity barrel;

    public SakeBarrelMenu(int id, Inventory playerInventory, SakeBarrelEntity barrel) {
        super(MenuType.GENERIC_9x3, id);
        this.barrel = barrel;

        // Barrel inventory: 3 rows x 9 cols
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                final int index = col + row * 9;
                this.addSlot(new Slot(barrel, index, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public int getMaxStackSize() {
                        ItemStack stack = this.getItem();
                        // If attempting to place fermented sake, limit per-slot capacity to 1
                        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof FermentedSakeItem) {
                            return 1;
                        }
                        return super.getMaxStackSize();
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        // allow placement of any item; the slot size limiter will restrict counts for fermented sake
                        return true;
                    }
                    @Override
                    public int getMaxStackSize(net.minecraft.world.item.ItemStack stack) {
                        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof FermentedSakeItem) {
                            return 1;
                        }
                        return super.getMaxStackSize(stack);
                    }
                });
            }
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(barrel.getLevel(), barrel.getBlockPos()), player, TansuBlockReg.SAKE_BARREL);
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        net.minecraft.world.item.ItemStack itemstack = net.minecraft.world.item.ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            net.minecraft.world.item.ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            int containerSlots = 27; // barrel size

            if (index < containerSlots) {
                if (!this.moveItemStackTo(stackInSlot, containerSlots, this.slots.size(), true)) {
                    return net.minecraft.world.item.ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, containerSlots, false)) {
                    return net.minecraft.world.item.ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(net.minecraft.world.item.ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
