package com.shioh.sengoku.block.entity;

import com.shioh.sengoku.block.SakeBarrel;
import com.shioh.sengoku.init.TansuBlockReg;
import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.init.SakeItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SakeBarrelEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private NonNullList<Integer> agingTimers;
    private final ContainerOpenersCounter openersCounter;

    public static final int AGING_TIME = 25000; //  ~50 seconds at 20 TPS

    public SakeBarrelEntity(BlockPos pos, BlockState state) {
        super(TansuBlockReg.SAKE_BARREL_BLOCK_ENTITY, pos, state);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
        this.agingTimers = NonNullList.withSize(27, 0);
        this.openersCounter = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level level, BlockPos pos, BlockState state) {
                SakeBarrelEntity.this.playSound(state, SoundEvents.BARREL_OPEN);
                SakeBarrelEntity.this.updateBlockState(state, true);
            }

            @Override
            protected void onClose(Level level, BlockPos pos, BlockState state) {
                SakeBarrelEntity.this.playSound(state, SoundEvents.BARREL_CLOSE);
                SakeBarrelEntity.this.updateBlockState(state, false);
            }

            @Override
            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {}

            @Override
            protected boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof ChestMenu cm) {
                    return cm.getContainer() == SakeBarrelEntity.this;
                }
                return false;
            }
        };
    }

    // ================= TICKING LOGIC =================
    public static void tick(Level level, BlockPos pos, BlockState state, SakeBarrelEntity barrel) {
        if (!level.isClientSide) {
            for (int i = 0; i < barrel.items.size(); i++) {
                ItemStack stack = barrel.items.get(i);

                if (!stack.isEmpty()) {
                    // Accept any fermented sake item (future-proof via class check)
                    if (stack.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem) {
                        int time = barrel.agingTimers.get(i) + 1;
                        barrel.agingTimers.set(i, time);


                        if (time >= AGING_TIME) {
                            // transform fermented sake -> corresponding sake bottle
                            net.minecraft.world.item.Item fermented = stack.getItem();
                            net.minecraft.world.item.Item result = SakeItemReg.SAKE_BOTTLE;
                            if (fermented == SakeItemReg.FERMENTED_SAKE_HEALING) result = SakeItemReg.SAKE_HEALING;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_REGEN) result = SakeItemReg.SAKE_REGEN;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_SWIFTNESS) result = SakeItemReg.SAKE_SWIFTNESS;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_LEAPING) result = SakeItemReg.SAKE_LEAPING;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_FIRE_RESIST) result = SakeItemReg.SAKE_FIRE_RESIST;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_INVISIBILITY) result = SakeItemReg.SAKE_INVISIBILITY;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_SLOW_FALLING) result = SakeItemReg.SAKE_SLOW_FALLING;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_ABSORPTION) result = SakeItemReg.SAKE_ABSORPTION;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_WATER_BREATHING) result = SakeItemReg.SAKE_WATER_BREATHING;
                            else if (fermented == SakeItemReg.FERMENTED_SAKE_LUCK) result = SakeItemReg.SAKE_LUCK;
                            barrel.items.set(i, new ItemStack(result, stack.getCount()));
                            barrel.agingTimers.set(i, 0);
                            barrel.setChanged();
                            // play a gentle brewing completion sound at the barrel
                            try { barrel.playSound(state, SoundEvents.BREWING_STAND_BREW); } catch (Throwable t) {}
                            // server-side log and particle feedback to help verify conversion
                            try {
                                // conversion completed; sound/particles are played below
                                if (level instanceof net.minecraft.server.level.ServerLevel server) {
                                    double x = pos.getX() + 0.5;
                                    double y = pos.getY() + 0.7;
                                    double z = pos.getZ() + 0.5;
                                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, x, y, z, 8, 0.2, 0.2, 0.2, 0.02);
                                }
                            } catch (Throwable t) { }
                        }
                    } else {
                        barrel.agingTimers.set(i, 0);
                    }
                } else {
                    barrel.agingTimers.set(i, 0);
                }
            }
        }
    }

    // ================= SAVE / LOAD =================
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }

        int[] timerArray = this.agingTimers.stream().mapToInt(Integer::intValue).toArray();
        tag.putIntArray("AgingTimers", timerArray);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }

        int[] timerArray = tag.getIntArray("AgingTimers");
        this.agingTimers = NonNullList.withSize(getContainerSize(), 0);
        for (int i = 0; i < timerArray.length && i < this.agingTimers.size(); i++) {
            this.agingTimers.set(i, timerArray[i]);
        }
    }

    // ================= INVENTORY =================
    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int getMaxStackSize() {
        // Each barrel slot should only hold a single bottle (prevents stacks from being inserted)
        return 1;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) {
                agingTimers.set(i, 0);
            } else {
                // Clamp fermented sake counts to 1 inside the barrel
                if (s.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem && s.getCount() > 1) {
                    s.setCount(1);
                }
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof com.shioh.sengoku.item.FermentedSakeItem) {
            // Clamp fermented sake to single bottles inside the barrel
            ItemStack single = stack.copy();
            single.setCount(1);
            this.items.set(slot, single);
            // Do NOT modify the provided stack here; slot/menu logic will decrement the player's stack appropriately.
        } else {
            this.items.set(slot, stack);
        }
        this.setChanged();
    }

    // ================= UI =================
    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.sengokutansu.sake_barrel");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        // Use ChestMenu.threeRows so the menu is a ChestMenu instance.
        // This allows ContainerOpenersCounter.isOwnContainer to recognize
        // the player's open container and trigger open/close sounds/state.
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    // ================= OPEN / CLOSE =================
    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    // ================= HELPERS =================
    public void updateBlockState(BlockState state, boolean open) {
        if (this.level != null) {
            this.level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, open), 3);
        }
    }

    public void playSound(BlockState state, SoundEvent sound) {
        Vec3i vec3i = state.getValue(BarrelBlock.FACING).getNormal();
        double x = this.worldPosition.getX() + 0.5 + vec3i.getX() / 2.0;
        double y = this.worldPosition.getY() + 0.5 + vec3i.getY() / 2.0;
        double z = this.worldPosition.getZ() + 0.5 + vec3i.getZ() / 2.0;
        if (this.level != null) {
            this.level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public SakeBarrel getBlock() {
        if (getBlockState().getBlock() instanceof SakeBarrel barrel) {
            return barrel;
        }
        return null;
    }
}
