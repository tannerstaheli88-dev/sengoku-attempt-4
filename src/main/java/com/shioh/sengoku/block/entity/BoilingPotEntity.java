package com.shioh.sengoku.block.entity;

import com.shioh.sengoku.init.TansuBlockReg;
import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.screen.BoilingPotMenu;
import net.minecraft.core.BlockPos;
import com.shioh.sengoku.block.BoilingPot;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import java.util.Optional;

/**
 * Custom Boiling Pot block entity with 4 slots:
 * 0: tea input (tea leaf for tea recipe, or any food for smoking)
 * 1: water input (potion item treated as water bottle, or empty for smoking)
 * 2: fuel
 * 3: result (tea bottle from tea recipe, or smoked food from smoker recipes)
 *
 * Behaves as:
 * - Tea processing machine when tea leaf + water bottle are present
 * - Smoker when water slot is empty and food input is present
 */
public class BoilingPotEntity extends BlockEntity implements MenuProvider, net.minecraft.world.Container {

    public static final int SLOT_TEA = 0;
    public static final int SLOT_WATER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_RESULT = 3;
    private static final int SIZE = 4;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    // Furnace-like state
    private int litTime;        // remaining burn time
    private int litDuration;    // total burn duration of current fuel
    private int cookTime;       // progress
    private int cookTimeTotal = 200; // total time to cook (increased to slow processing)
    // Accumulated experience from processed recipes (floating to preserve fractional XP)
    private float accumulatedExperience = 0.0F;

    public BoilingPotEntity(BlockPos pos, BlockState state) {
        super(TansuBlockReg.BOILING_POT_BLOCK_ENTITY, pos, state);
    }

    // ----- Container implementation -----
    @Override
    public int getContainerSize() { return SIZE; }
    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false; return true;
    }
    @Override
    public ItemStack getItem(int slot) { return items.get(slot); }
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }
    @Override
    public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override
    public boolean stillValid(Player player) { return player.distanceToSqr(worldPosition.getCenter()) <= 64.0; }
    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
    }

    // ----- MenuProvider -----
    @Override
    public Component getDisplayName() { return Component.translatable("container.boiling_pot"); }
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BoilingPotMenu(id, playerInventory, this, this.getDataAccess());
    }

    // ----- Tick Logic -----
    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilingPotEntity be) {
        boolean dirty = false;

        // Burn fuel
        if (be.litTime > 0) be.litTime--;

        boolean hasFuel = !be.getItem(SLOT_FUEL).isEmpty();
        boolean canProcess = be.canProcess();

        // If not lit and can process with fuel available, consume fuel
        if (be.litTime == 0 && hasFuel && canProcess) {
            int burn = be.getFuelBurnTime(be.getItem(SLOT_FUEL));
            if (burn > 0) {
                be.litTime = burn;
                be.litDuration = burn;
                ItemStack fuel = be.getItem(SLOT_FUEL);
                fuel.shrink(1);
                if (fuel.isEmpty()) be.setItem(SLOT_FUEL, ItemStack.EMPTY);
                dirty = true;
            }
        }

        // Cooking progress: require lit time for all recipes
        if (canProcess && be.litTime > 0) {
            be.cookTime++;
            if (be.cookTime >= be.cookTimeTotal) {
                be.cookTime = 0;
                be.doProcess();
                dirty = true;
            }
        } else {
            if (be.cookTime > 0) be.cookTime = Math.max(be.cookTime - 2, 0); // slight cooldown
        }

        if (dirty) be.setChanged();
        // Ensure blockstate LIT matches whether the entity has burn time
        try {
            boolean isLit = state.getValue(BoilingPot.LIT);
            boolean shouldBeLit = be.litTime > 0;
            if (isLit != shouldBeLit) {
                level.setBlock(pos, state.setValue(BoilingPot.LIT, shouldBeLit), 3);
            }
        } catch (Throwable ignored) {}
    }

    private boolean isTeaLeaf(ItemStack stack) { return stack.is(TansuItemReg.TEA_LEAF); }
    private boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        // Use getOrDefault like the menu does to avoid relying on has(...) or direct NBT access
        PotionContents pc = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return pc.is(Potions.WATER);
    }
    
    

    private boolean canProcess() {
        ItemStack input = getItem(SLOT_TEA);
        ItemStack water = getItem(SLOT_WATER);
        
        if (input.isEmpty()) return false;
        
        // Tea processing: requires tea leaf + water bottle
        if (!water.isEmpty() && isTeaLeaf(input) && isWaterBottle(water)) {
            ItemStack result = getItem(SLOT_RESULT);
            if (result.isEmpty()) return true;
            if (result.is(TansuItemReg.TEA_BOTTLE)) return result.getCount() < result.getMaxStackSize();
            return false;
        }
        
        // Smoker recipe processing: requires empty water slot
        if (water.isEmpty()) {
            Optional<RecipeHolder<SmokingRecipe>> recipeOpt = this.level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(input), this.level);
            if (recipeOpt.isPresent()) {
                ItemStack recipeResult = recipeOpt.get().value().getResultItem(this.level.registryAccess());
                ItemStack currentResult = getItem(SLOT_RESULT);
                if (currentResult.isEmpty()) return true;
                if (ItemStack.isSameItemSameComponents(currentResult, recipeResult)) {
                    return currentResult.getCount() + recipeResult.getCount() <= currentResult.getMaxStackSize();
                }
            }
        }
        
        return false;
    }

    private void doProcess() {
        if (!canProcess()) return;
        
        ItemStack input = getItem(SLOT_TEA);
        ItemStack water = getItem(SLOT_WATER);
        
        // Tea processing
        if (!water.isEmpty() && isTeaLeaf(input) && isWaterBottle(water)) {
            ItemStack result = getItem(SLOT_RESULT);
            if (result.isEmpty()) {
                setItem(SLOT_RESULT, new ItemStack(TansuItemReg.TEA_BOTTLE));
            } else if (result.is(TansuItemReg.TEA_BOTTLE)) {
                result.grow(1);
            }
            input.shrink(1); 
            if (input.isEmpty()) setItem(SLOT_TEA, ItemStack.EMPTY);
            water.shrink(1); 
            if (water.isEmpty()) setItem(SLOT_WATER, ItemStack.EMPTY);
            return;
        }
        
        // Smoker recipe processing
        if (water.isEmpty()) {
            Optional<RecipeHolder<SmokingRecipe>> recipeOpt = this.level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(input), this.level);
            if (recipeOpt.isPresent()) {
                ItemStack recipeResult = recipeOpt.get().value().getResultItem(this.level.registryAccess()).copy();
                ItemStack currentResult = getItem(SLOT_RESULT);
                if (currentResult.isEmpty()) {
                    setItem(SLOT_RESULT, recipeResult);
                } else if (ItemStack.isSameItemSameComponents(currentResult, recipeResult)) {
                    currentResult.grow(recipeResult.getCount());
                }
                // Accumulate experience from smoker recipes (may be fractional)
                float exp = recipeOpt.get().value().getExperience();
                if (exp > 0) accumulatedExperience += exp;
                input.shrink(1);
                if (input.isEmpty()) setItem(SLOT_TEA, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Award any accumulated experience to the given player. Called when a player takes result items.
     */
    public void awardAccumulatedExperience(Player player) {
        if (this.level == null || this.level.isClientSide) return;
        int xpToGive = (int)Math.floor(this.accumulatedExperience);
        if (xpToGive <= 0) return;
        try {
            // Spawn experience orbs on the server (vanilla furnace behavior)
            if (this.level instanceof net.minecraft.server.level.ServerLevel server) {
                // Use player's position (Vec3) so the ExperienceOrb helper receives the correct type
                net.minecraft.world.entity.ExperienceOrb.award(server, player.position(), xpToGive);
            } else {
                player.giveExperiencePoints(xpToGive);
            }
        } catch (Throwable ignored) {}
        this.accumulatedExperience -= xpToGive;
        this.setChanged();
    }

    /**
     * Drop all contained items into the world at the given position.
     * Used when the block is broken.
     */
    public void dropAllContents(Level level, BlockPos pos) {
        if (level == null) return;
        try {
            net.minecraft.world.Containers.dropContents(level, pos, this);
        } catch (Throwable ignored) {}
    }

    private int getFuelBurnTime(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 1600;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        return 0;
    }

    // ----- Data accessors for screen -----
    public int getLitTime() { return litTime; }
    public int getLitDuration() { return litDuration; }
    public int getCookTime() { return cookTime; }
    public int getCookTimeTotal() { return cookTimeTotal; }

    // ----- NBT (modern signature with HolderLookup) -----
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookTimeTotal", cookTimeTotal);
        tag.putFloat("AccumXP", accumulatedExperience);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        cookTime = tag.getInt("CookTime");
        cookTimeTotal = tag.getInt("CookTimeTotal");
        accumulatedExperience = tag.contains("AccumXP") ? tag.getFloat("AccumXP") : 0.0F;
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }

    // ContainerData for syncing the GUI (0=litTime,1=litDuration,2=cookTime,3=cookTimeTotal)
    private final ContainerData dataAccess = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> BoilingPotEntity.this.litTime;
                case 1 -> BoilingPotEntity.this.litDuration;
                case 2 -> BoilingPotEntity.this.cookTime;
                case 3 -> BoilingPotEntity.this.cookTimeTotal;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) BoilingPotEntity.this.litTime = value;
            else if (index == 1) BoilingPotEntity.this.litDuration = value;
            else if (index == 2) BoilingPotEntity.this.cookTime = value;
            else if (index == 3) BoilingPotEntity.this.cookTimeTotal = value;
        }
        @Override public int getCount() { return 4; }
    };

    public ContainerData getDataAccess() { return this.dataAccess; }
}
