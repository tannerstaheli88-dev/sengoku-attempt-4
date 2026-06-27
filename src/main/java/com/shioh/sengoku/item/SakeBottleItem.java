package com.shioh.sengoku.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import com.shioh.sengoku.init.SakeItemReg;

public class SakeBottleItem extends Item {
    public SakeBottleItem(Properties props) {
        super(props
            .food(new FoodProperties.Builder()
                .nutrition(6)           // restores 3 food bars
                .saturationModifier(0.6F)
                .alwaysEdible()
                .build()).stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        // Ensure hunger/saturation are applied by letting the superclass handle food first
        Item consumed = stack.getItem();
        ItemStack result = super.finishUsingItem(stack, world, entity);

        if (entity instanceof Player player) {
            if (!world.isClientSide) {
                // Apply different positive effects depending on the specific sake item consumed
                if (consumed == SakeItemReg.SAKE_HEALING) {
                    // Instant heal instead of regeneration (there is a separate regen sake)
                    player.heal(4.0F);
                } else if (consumed == SakeItemReg.SAKE_REGEN) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1)); // 30s regen II
                } else if (consumed == SakeItemReg.SAKE_SWIFTNESS) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)); // 30s speed II
                } else if (consumed == SakeItemReg.SAKE_LEAPING) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1)); // 30s jump II
                } else if (consumed == SakeItemReg.SAKE_FIRE_RESIST) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0)); // 30s fire resistance
                } else if (consumed == SakeItemReg.SAKE_INVISIBILITY) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0)); // 30s invisibility
                } else if (consumed == SakeItemReg.SAKE_SLOW_FALLING) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0)); // 30s slow falling
                } else if (consumed == SakeItemReg.SAKE_ABSORPTION) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0)); // 30s absorption I
                } else if (consumed == SakeItemReg.SAKE_WATER_BREATHING) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0)); // 30s water breathing
                } else if (consumed == SakeItemReg.SAKE_LUCK) {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 0)); // 60s luck
                } else {
                    // default: small damage boost (legacy behavior)
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0)); // 20s strength I
                }
                // Apply a short universal damage boost so all sakes provide Strength I for 20s
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0)); // 20s strength I
                // Retain confusion for fermented bottles (keep nausea for all sake bottles)
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0)); // 5s nausea
            }

            // Handle returning an empty glass bottle / adding it to inventory like TeaBottleItem
            if (player instanceof Player) {
                if (result.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                } else {
                    ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(empty)) {
                        player.drop(empty, false);
                    }
                }
            }
        }

        return result;
    }
}
