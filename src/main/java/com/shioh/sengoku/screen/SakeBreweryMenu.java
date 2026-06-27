package com.shioh.sengoku.screen;

import com.shioh.sengoku.init.TansuItemReg;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Standalone menu for the Sake Brewery. Slot layout (server+client):
 * - slots 0..2: ingredient slots (top row)
 * - slot 3: bottle-input slot (stackable bottles)
 * - slot 4: output slot (results)
 * - slot 5: fuel slot
 */
public class SakeBreweryMenu extends AbstractContainerMenu {
    public static final int INGREDIENT_SLOT_0 = 0;
    public static final int INGREDIENT_SLOT_1 = 1;
    public static final int INGREDIENT_SLOT_2 = 2;
    public static final int BOTTLE_INPUT_SLOT = 3; // bottle-input slot
    public static final int FUEL_SLOT = 4; // fuel slot
    public static final int OUTPUT_SLOT = 5; // output slot
    public static final int NUM_BLOCK_SLOTS = 6; // total number of block slots (matches BE)

    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int DATA_FUEL_MAX = 2; // total burn time for current fuel

    private final Container blockContainer;
    private final ContainerData data;

    // Server-side constructor
    public SakeBreweryMenu(int id, Inventory playerInventory, Container blockContainer, ContainerData data) {
    super(com.shioh.sengoku.registry.ModMenuTypes.SAKE_BREWERY, id);
    this.blockContainer = blockContainer;
    this.data = data == null ? new SimpleContainerData(3) : data;

        // Ingredient slots (top row)
        for (int i = 0; i < 3; i++) {
            int guiX = 44 + i * 22;
            int guiY = 17;
            final int index = i;
            this.addSlot(new Slot(blockContainer, index, guiX, guiY) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack == null || stack.isEmpty()) return false;
                    // slot 0: only poisonous potato
                    if (index == INGREDIENT_SLOT_0) return stack.is(net.minecraft.world.item.Items.POISONOUS_POTATO);
                    // slot 1: only rice
                    if (index == INGREDIENT_SLOT_1) return stack.is(TansuItemReg.RICE_I);
                    // slot 2: accept any ingredient
                    if (index == INGREDIENT_SLOT_2) return true;
                    return false;
                }
            });
        }

        // Bottle-input slot (moved to far left, same vertical level as ingredient slots)
        this.addSlot(new Slot(blockContainer, BOTTLE_INPUT_SLOT, 17, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (stack == null || stack.isEmpty()) return false;
                    // Accept any potion item types (regular, splash, lingering) and reject empty glass bottles
                    if (stack.is(Items.GLASS_BOTTLE)) return false;
                    return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
            }

            @Override
            public int getMaxStackSize() {
                // Force this slot to accept stacks up to 16 (like beverages)
                return 16;
            }
        });

        // Fuel slot (bottom-left)
        this.addSlot(new Slot(blockContainer, FUEL_SLOT, 17, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (stack == null || stack.isEmpty()) return false;
                return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || super.mayPlace(stack);
            }
        });

        // Output slot (bottom-center) - results go here. Split fermented sake to singles when extracted
        this.addSlot(new Slot(blockContainer, OUTPUT_SLOT, 66, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // prevent players from placing into output
            }

            @Override
            public ItemStack remove(int amount) {
                ItemStack current = this.getItem();
                if (current.isEmpty()) return ItemStack.EMPTY;
                
                // For fermented sake, always extract just 1 regardless of amount requested
                if (current.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem) {
                    ItemStack single = current.copy();
                    single.setCount(1);
                    current.shrink(1);
                    this.setChanged();
                    return single;
                }
                
                return super.remove(amount);
            }
        });

        // Player inventory slots
        // main inventory (3 rows x 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        // Data syncing
        this.addDataSlots(this.data);
    }


    public ContainerData getDataAccess() {
        return this.data;
    }

    public int getBrewTime() {
        return this.data.get(DATA_BREW_TIME);
    }

    public int getFuelUses() {
        return this.data.get(DATA_FUEL_USES);
    }

    public int getFuelMax() {
        return this.data.get(DATA_FUEL_MAX);
    }

    // Client-side constructor (when opening via network)
    public SakeBreweryMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, new SimpleContainer(NUM_BLOCK_SLOTS), new SimpleContainerData(3));
    }

    // Convenience constructor used by MenuType supplier that only provides id and player inventory
    public SakeBreweryMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(NUM_BLOCK_SLOTS), new SimpleContainerData(3));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int blockEnd = NUM_BLOCK_SLOTS;
            int playerStart = blockEnd;
            int playerEnd = blockEnd + 36;

            if (index < blockEnd) {
                // from block to player (including output)
                // Special handling for fermented sake from output slot - only move 1 at a time
                if (index == OUTPUT_SLOT && itemstack1.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem) {
                    // Create a single-item stack to transfer
                    ItemStack single = itemstack1.copy();
                    single.setCount(1);
                    
                    if (this.moveItemStackTo(single, playerStart, playerEnd, true)) {
                        // Successfully moved one, reduce the original stack
                        itemstack1.shrink(1);
                        if (itemstack1.isEmpty()) {
                            slot.set(ItemStack.EMPTY);
                        }
                        slot.setChanged();
                        return single;
                    }
                    return ItemStack.EMPTY;
                }
                
                if (!this.moveItemStackTo(itemstack1, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // from player to appropriate block slot
                boolean moved = false;
                
                if (itemstack1.is(Items.POISONOUS_POTATO)) {
                    moved = this.moveItemStackTo(itemstack1, INGREDIENT_SLOT_0, INGREDIENT_SLOT_0 + 1, false);
                } else if (itemstack1.is(TansuItemReg.RICE_I)) {
                    moved = this.moveItemStackTo(itemstack1, INGREDIENT_SLOT_1, INGREDIENT_SLOT_2 + 1, false);
                } else if (itemstack1.is(Items.COAL) || itemstack1.is(Items.CHARCOAL)) {
                    moved = this.moveItemStackTo(itemstack1, FUEL_SLOT, FUEL_SLOT + 1, false);
                } else if (itemstack1.is(Items.POTION) || itemstack1.is(Items.SPLASH_POTION) || itemstack1.is(Items.LINGERING_POTION)) {
                    moved = this.moveItemStackTo(itemstack1, BOTTLE_INPUT_SLOT, BOTTLE_INPUT_SLOT + 1, false);
                }
                
                // If nothing moved, return empty to cancel the shift-click
                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }
}
