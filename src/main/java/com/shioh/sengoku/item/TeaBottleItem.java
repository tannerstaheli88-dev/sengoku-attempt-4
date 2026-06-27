package com.shioh.sengoku.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class TeaBottleItem extends Item {

    public TeaBottleItem(Properties props) {
        super(props.food(new FoodProperties.Builder()
                .nutrition(6)          // same as honey bottle
                .saturationModifier(0.1f)
                .alwaysEdible()
                .build())
                .stacksTo(16));        // let it stack like honey bottles
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public int getUseDuration(ItemStack stack) {
        return 40; // same as honey bottle (2 seconds)
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        // let vanilla handle hunger/saturation first
        ItemStack result = super.finishUsingItem(stack, world, entity);

        // add Haste I (260 ticks = 13 seconds)
        if (entity instanceof Player player && !world.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 260, 0));
        }

        // return empty glass bottle
        if (entity instanceof Player player) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            } else {
                ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(empty)) {
                    player.drop(empty, false);
                }
            }
        }

        return result;
    }
}
