package com.shioh.sengoku.block.entity;

import com.shioh.sengoku.block.SakeBreweryBlock;
import com.shioh.sengoku.init.TansuBlockReg;
import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.init.SakeItemReg;
import com.shioh.sengoku.recipes.ModSakeBrewingRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * Sake Brewery block entity with a custom slot layout:
 * - slots 0..2: ingredient slots (three ingredients)
 * - slot 3: bottle slot (water/potion/fermented sake)
 * - slot 4: fuel slot (coal/charcoal/other fuels)
 */
public class SakeBreweryEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private static final int[] INGREDIENT_SLOTS = new int[]{0, 1, 2};
    private static final int BOTTLE_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int OUTPUT_SLOT = 5;
    private static final int[] SLOTS_FOR_UP = new int[]{BOTTLE_SLOT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, OUTPUT_SLOT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, FUEL_SLOT};
    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int DATA_FUEL_MAX = 2;
    public static final int NUM_DATA_VALUES = 3;
    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private int brewTime;
    private int fuelUses;
    private int fuelMax;

    private final ContainerData dataAccess = new ContainerData() {
        public int get(int index) {
            if (index == DATA_BREW_TIME) return SakeBreweryEntity.this.brewTime;
            if (index == DATA_FUEL_USES) return SakeBreweryEntity.this.fuelUses;
            return SakeBreweryEntity.this.fuelMax;
        }

        public void set(int index, int value) {
            if (index == DATA_BREW_TIME) SakeBreweryEntity.this.brewTime = value;
            else if (index == DATA_FUEL_USES) SakeBreweryEntity.this.fuelUses = value;
            else if (index == DATA_FUEL_MAX) SakeBreweryEntity.this.fuelMax = value;
        }

        public int getCount() {
            return NUM_DATA_VALUES;
        }
    };

    public SakeBreweryEntity(BlockPos pos, BlockState state) {
        super(TansuBlockReg.SAKE_BREWERY_BLOCK_ENTITY, pos, state);
        this.items = NonNullList.withSize(6, ItemStack.EMPTY);
    }

    // ...existing code...

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.sengoku.sake_brewery");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    // Basic inventory methods
    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(this.items, slot);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        this.setChanged();
        updateBottleBits(this.level, this.worldPosition, this.getBlockState());
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // WorldlyContainer: accessible slots by side
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? SLOTS_FOR_DOWN : (side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        if (index == BOTTLE_SLOT) {
            // allow bottles/potions
            Item i = itemStackIn.getItem();
            // Accept any potion item types (regular, splash, lingering)
            return i == Items.POTION || i == Items.SPLASH_POTION || i == Items.LINGERING_POTION;
        } else if (index == FUEL_SLOT) {
            // allow coal/charcoal and other fuels; client-side we accept coal/charcoal
            Item i = itemStackIn.getItem();
            return i == Items.COAL || i == Items.CHARCOAL;
        } else {
            // ingredient slots: enforce per-slot restrictions
            // INGREDIENT_SLOTS indices are 0,1,2
            if (index == INGREDIENT_SLOTS[0]) {
                // slot 0: only poisonous potato
                return itemStackIn.getItem() == net.minecraft.world.item.Items.POISONOUS_POTATO;
            } else if (index == INGREDIENT_SLOTS[1]) {
                // slot 1: only rice
                return itemStackIn.getItem() == TansuItemReg.RICE_I;
            } else if (index == INGREDIENT_SLOTS[2]) {
                // slot 2: accept any ingredient
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    // Load/save (match other block entities signatures)
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.brewTime = tag.getShort("BrewTime");
        this.fuelUses = tag.getShort("FuelUses");
        this.fuelMax = tag.getShort("FuelMax");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("BrewTime", (short)this.brewTime);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putShort("FuelUses", (short)this.fuelUses);
        tag.putShort("FuelMax", (short)this.fuelMax);
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    // Server tick: manage fuel and brewing progress
    public static void serverTick(Level level, BlockPos pos, BlockState state, SakeBreweryEntity be) {
        boolean dirty = false;

        boolean brewing = be.isBrewable(be.items);

        // Start burning only when there's a recipe (furnace-like). If already burning, continue burning
        if (be.fuelUses <= 0 && brewing && canConsumeFuel(be.items.get(FUEL_SLOT))) {
            be.fuelUses = getFuelUses(be.items.get(FUEL_SLOT));
            be.fuelMax = be.fuelUses;
            if (be.fuelUses > 0) {
                ItemStack f = be.items.get(FUEL_SLOT);
                f.shrink(1);
                dirty = true;
                // play campfire crackle when fuel starts burning with a slight pitch variation
                try {
                    float pitch = 0.9F + (level.random.nextFloat() * 0.2F);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, pitch);
                } catch (Throwable t) {}
            }
        }

        // If fuel is present, it burns down each tick regardless of whether brewing is currently possible.
        if (be.fuelUses > 0) {
            if (brewing) {
                be.brewTime++;
                if (be.brewTime >= 400) {
                    doBrew(level, pos, be);
                    be.brewTime = 0;
                    dirty = true;
                }
            } else {
                if (be.brewTime > 0) {
                    be.brewTime = Math.max(0, be.brewTime - 2);
                }
            }

            int before = be.fuelUses;
            be.fuelUses = Math.max(0, be.fuelUses - 1);
            if (be.fuelUses != before) dirty = true;

            // occasionally play campfire crackle while burning so players hear continuous burning
            if (level.random.nextInt(80) == 0) {
                try {
                    float p = 0.8F + level.random.nextFloat() * 0.4F;
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, p);
                } catch (Throwable t) {}
            }
        } else {
            if (be.brewTime > 0) {
                be.brewTime = Math.max(0, be.brewTime - 2);
            }
        }

        if (dirty) {
            be.setChanged();
            updateBottleBits(level, pos, state);
        }
    }

    private static boolean canConsumeFuel(ItemStack fuel) {
        if (fuel == null) return false;
        if (fuel.isEmpty()) return false;
        Integer val = FuelRegistry.INSTANCE.get(fuel.getItem());
        return val != null && val > 0;
    }

    private static int getFuelUses(ItemStack fuel) {
        if (fuel == null || fuel.isEmpty()) return 0;
        Integer val = FuelRegistry.INSTANCE.get(fuel.getItem());
        return val != null ? val : 0;
    }

    private boolean isBrewable(NonNullList<ItemStack> items) {
        ItemStack bottle = items.get(BOTTLE_SLOT);
        if (bottle.isEmpty()) return false;
        // First check mod-packaged JSON recipes via our loader
        for (com.shioh.sengoku.recipes.SakeBrewingRecipe sr : com.shioh.sengoku.recipes.ModSakeBrewingRecipes.getRecipes()) {
            SimpleContainer inv = new SimpleContainer(6);
            for (int i = 0; i < 6; i++) inv.setItem(i, items.get(i));
            boolean m = false;
            try { m = sr.matches(inv, this.level); } catch (Throwable t) { try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("Error testing sake recipe {}: {}", sr.getId(), t.toString()); } catch (Throwable tt) {} }
            if (m) {
                // Check if output slot can accept the result
                ItemStack output = sr.assemble(inv);
                ItemStack out = items.get(OUTPUT_SLOT);
                if (!out.isEmpty() && !out.is(output.getItem())) continue;
                int outCount = out.isEmpty() ? 0 : out.getCount();
                int slotMax = getOutputSlotMax(output);
                if (outCount + output.getCount() > slotMax) continue;
                return true;
            }
        }

        // Only JSON-packaged recipes are allowed now. If none matched, not brewable.
        return false;
    }

    private static void doBrew(Level level, BlockPos pos, SakeBreweryEntity be) {
        NonNullList<ItemStack> items = be.items;
        ItemStack bottle = items.get(BOTTLE_SLOT);

        boolean hasRice = false;
        int riceIdx = -1;
        boolean hasPoison = false;
        int poisonIdx = -1;
        for (int idx : INGREDIENT_SLOTS) {
            ItemStack s = items.get(idx);
            if (s.isEmpty()) continue;
            if (!hasRice && s.getItem() == TansuItemReg.RICE_I) { hasRice = true; riceIdx = idx; }
            else if (!hasPoison && s.getItem() == Items.POISONOUS_POTATO) { hasPoison = true; poisonIdx = idx; }
        }

        // Attempt to find a matching JSON recipe first
        for (com.shioh.sengoku.recipes.SakeBrewingRecipe sr : com.shioh.sengoku.recipes.ModSakeBrewingRecipes.getRecipes()) {
            net.minecraft.world.SimpleContainer inv = new net.minecraft.world.SimpleContainer(6);
            for (int i = 0; i < 6; i++) inv.setItem(i, items.get(i));

            if (!sr.matches(inv, level)) continue;

            ItemStack output = sr.assemble(inv);
            ItemStack out = items.get(OUTPUT_SLOT);
            if (!out.isEmpty() && !out.is(output.getItem())) continue;
            int outCount = out.isEmpty() ? 0 : out.getCount();
            // Allow fermented sake to stack up to 16 in the output slot, even though the item stacks to 1 elsewhere
            int slotMax = getOutputSlotMax(output);
            if (outCount + output.getCount() > slotMax) continue;

            if (out.isEmpty()) {
                items.set(OUTPUT_SLOT, output.copy());
            } else {
                // Directly set count to bypass item's maxStackSize restriction
                out.setCount(outCount + output.getCount());
            }

            // consume ingredients by consulting recipe.getIngredients()
            java.util.List<net.minecraft.world.item.crafting.Ingredient> ingr = sr.getIngredients();
            for (int j = 0; j < ingr.size(); j++) {
                net.minecraft.world.item.crafting.Ingredient ing = ingr.get(j);
                for (int i = 0; i < 3; i++) {
                    ItemStack candidate = items.get(i);
                    if (candidate.isEmpty()) continue;
                    if (ing.test(candidate)) { candidate.shrink(1); break; }
                }
            }

            if (!bottle.isEmpty()) {
                bottle.shrink(1);
                if (bottle.isEmpty()) items.set(BOTTLE_SLOT, ItemStack.EMPTY);
            }

            try {
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
            } catch (Throwable t) {}

            be.setChanged();
            updateBottleBits(level, pos, be.getBlockState());
            return;
        }

        // If we reach here, no JSON recipe matched. Log at debug level for troubleshooting.
        try { com.shioh.sengoku.sengokuFabric.LOGGER.debug("No sake recipe matched for contents at {}", pos); } catch (Throwable t) {}
        return;

        // (generic JSON recipe handling is performed earlier in this method)
    }

    private static int getOutputSlotMax(ItemStack stack) {
        try {
            if (stack.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem) {
                return 16; // brewery output can hold up to 16 fermented sake
            }
        } catch (Throwable t) {}
        return stack.getMaxStackSize();
    }

    private static void updateBottleBits(Level level, BlockPos pos, BlockState state) {
        if (level == null || state.getBlock() != TansuBlockReg.SAKE_BREWERY) return;
        boolean[] bits = new boolean[3];
        SakeBreweryEntity be = (SakeBreweryEntity) level.getBlockEntity(pos);
        if (be == null) return;
        for (int i = 0; i < 3; i++) bits[i] = !be.items.get(i).isEmpty();
        state = state.setValue(SakeBreweryBlock.HAS_BOTTLE[0], bits[0]);
        state = state.setValue(SakeBreweryBlock.HAS_BOTTLE[1], bits[1]);
        state = state.setValue(SakeBreweryBlock.HAS_BOTTLE[2], bits[2]);
        boolean lit = be.fuelUses > 0;
        try { state = state.setValue(SakeBreweryBlock.LIT, lit); } catch (Throwable t) {}
        level.setBlock(pos, state, 3);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new com.shioh.sengoku.screen.SakeBreweryMenu(containerId, inventory, this, this.dataAccess);
    }

}

