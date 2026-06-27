package com.shioh.sengoku.entity;

import com.shioh.sengoku.registry.SoundRegistry;
import com.shioh.sengoku.villager.VillagerTradeLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class ShiryoEntity extends Villager {
    private static final int SHIRYO_TRADE_MAX_USES = 12;
    private static final long SHIRYO_TRADE_REFRESH_INTERVAL = 12000L;
    private static final String SHIRYO_REFRESH_TAG = "ShiryoTradeRefreshTick";

    private static final TagKey<Block> SPAWNABLE_ON = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("sengoku", "shiryo_spawnable_on")
    );

    private long nextTradeRefreshTick;

    private static final VillagerTrades.ItemListing[][] SHIRYO_TRADES = new VillagerTrades.ItemListing[][] {
        {
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.NETHER_WART, 18, 1, SHIRYO_TRADE_MAX_USES, 2),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.NETHERRACK, 32, 1, SHIRYO_TRADE_MAX_USES, 2),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.SOUL_TORCH, 8), 1, 8, SHIRYO_TRADE_MAX_USES, 1, 0.05F),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.NETHER_BRICKS, 12), 1, 12, SHIRYO_TRADE_MAX_USES, 1, 0.05F)
        },
        {
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.MAGMA_CREAM, 5, 1, SHIRYO_TRADE_MAX_USES, 5),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.SOUL_SAND, 16, 1, SHIRYO_TRADE_MAX_USES, 5),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.WITHER_ROSE, 4, 1, SHIRYO_TRADE_MAX_USES, 5),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.FIRE_CHARGE, 3), 2, 3, SHIRYO_TRADE_MAX_USES, 5, 0.05F),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.GLOWSTONE, 8), 2, 8, SHIRYO_TRADE_MAX_USES, 5, 0.05F)
        },
        {
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.BLAZE_ROD, 4, 1, SHIRYO_TRADE_MAX_USES, 10),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.BLACKSTONE, 24, 1, SHIRYO_TRADE_MAX_USES, 10),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.RABBIT_FOOT, 8), 3, 8, SHIRYO_TRADE_MAX_USES, 10, 0.05F),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.SOUL_LANTERN), 3, 1, SHIRYO_TRADE_MAX_USES, 10, 0.05F)
        },
        {
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.CRYING_OBSIDIAN, 2, 1, SHIRYO_TRADE_MAX_USES, 15),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.GHAST_TEAR, 1, 2, SHIRYO_TRADE_MAX_USES, 15),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.OBSIDIAN, 4), 3, 4, SHIRYO_TRADE_MAX_USES, 15, 0.05F),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.CHAIN, 6), 4, 6, SHIRYO_TRADE_MAX_USES, 15, 0.05F)
        },
        {
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.GOLD_INGOT, 12, 1, SHIRYO_TRADE_MAX_USES, 30),
            new VillagerTradeLoader.EmeraldsForItemsTrade(Items.ANCIENT_DEBRIS, 1, 8, SHIRYO_TRADE_MAX_USES, 30),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.RESPAWN_ANCHOR), 12, 1, SHIRYO_TRADE_MAX_USES, 30, 0.05F),
            new VillagerTradeLoader.ItemsForEmeraldsTrade(new ItemStack(Items.ENDER_PEARL, 2), 5, 2, SHIRYO_TRADE_MAX_USES, 30, 0.05F)
        }
    };

    public ShiryoEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        // Use the non-job villager state so Shiryo keeps villager wandering AI without job-site logic.
        VillagerData data = this.getVillagerData();
        super.setVillagerData(data.setProfession(VillagerProfession.NITWIT));
    }

    /** Lock Shiryo to the non-job villager state so vanilla job-site logic never takes over. */
    @Override
    public void setVillagerData(VillagerData villagerData) {
        super.setVillagerData(villagerData.setProfession(VillagerProfession.NITWIT));
    }

    /** Shiryo never needs a job-site block to restock. */
    @Override
    public boolean canRestock() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType spawnType,
        @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NITWIT));
        this.nextTradeRefreshTick = this.level().getGameTime() + SHIRYO_TRADE_REFRESH_INTERVAL;
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && this.nextTradeRefreshTick > 0L && this.level().getGameTime() >= this.nextTradeRefreshTick) {
            this.refreshShiryoTrades();
            this.nextTradeRefreshTick = this.level().getGameTime() + SHIRYO_TRADE_REFRESH_INTERVAL;
        }
    }

    @Override
    protected void updateTrades() {
        int level = this.getVillagerData().getLevel();
        if (level < 1 || level > SHIRYO_TRADES.length) {
            return;
        }

        MerchantOffers offers = this.getOffers();
        VillagerTrades.ItemListing[] listings = SHIRYO_TRADES[level - 1];
        if (listings.length > 0) {
            this.addOffersFromItemListings(offers, listings, Math.min(2, listings.length));
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.is(DamageTypes.WITHER)) {
            return false;
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Do NOT call super — vanilla Villager.mobInteract opens a screen based on profession/job-site
        // state which causes the flash-and-close. We own the full interaction path here.
        if (!this.isAlive() || this.isSleeping() || this.isTrading() || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (this.isBaby()) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        MerchantOffers offers = this.getOffers();
        if (offers.isEmpty()) {
            this.updateTrades();
            offers = this.getOffers();
        }

        if (offers.isEmpty()) {
            this.playSound(SoundRegistry.SHIRYO_AMBIENT, 0.7F, 1.1F);
            return InteractionResult.CONSUME;
        }

        player.awardStat(Stats.TALKED_TO_VILLAGER);
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());

        return InteractionResult.CONSUME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.SHIRYO_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.SHIRYO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.SHIRYO_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean sold) {
        return sold ? SoundRegistry.SHIRYO_AMBIENT : SoundRegistry.SHIRYO_HURT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong(SHIRYO_REFRESH_TAG, this.nextTradeRefreshTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(SHIRYO_REFRESH_TAG)) {
            this.nextTradeRefreshTick = tag.getLong(SHIRYO_REFRESH_TAG);
        } else {
            this.nextTradeRefreshTick = this.level().getGameTime() + SHIRYO_TRADE_REFRESH_INTERVAL;
        }
    }

    private void refreshShiryoTrades() {
        MerchantOffers offers = this.getOffers();
        for (MerchantOffer offer : offers) {
            offer.resetUses();
        }
    }

    public static boolean checkShiryoSpawnRules(
        EntityType<? extends Mob> entityType,
        LevelAccessor level,
        MobSpawnType spawnType,
        BlockPos pos,
        RandomSource random
    ) {
        if (level instanceof ServerLevel serverLevel && serverLevel.dimension() != Level.NETHER) {
            return false;
        }

        return level.getBlockState(pos.below()).is(SPAWNABLE_ON)
            && level.getBlockState(pos).isAir()
            && level.getBlockState(pos.above()).isAir()
            && Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }
}