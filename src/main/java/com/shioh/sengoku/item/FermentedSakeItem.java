package com.shioh.sengoku.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;

public class FermentedSakeItem extends Item {
    public FermentedSakeItem(Properties props) {
        super(props
            .stacksTo(16) // stack like other bottles
            .food(new FoodProperties.Builder()
                .nutrition(6)         // restore like tea/honey bottles (3 food bars)
                        .saturationModifier(0.1F)
                        .alwaysEdible()
                        .build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        try {
            tooltip.add(Component.translatable("item.sengoku.fermented_sake.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
        } catch (Throwable t) {}
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
        if (entity instanceof Player player) {
            if (!world.isClientSide) {
                // Always nausea
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));

                // Special healing variant check
                if (stack.getItem() == com.shioh.sengoku.init.SakeItemReg.FERMENTED_SAKE_HEALING) {
                    player.heal(4.0F);
                }
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);

                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                } else {
                    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(bottle)) {
                        player.drop(bottle, false);
                    }
                }
            }
        }

        return stack;
    }
}
