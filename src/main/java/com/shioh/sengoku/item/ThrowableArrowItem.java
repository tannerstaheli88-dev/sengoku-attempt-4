package com.shioh.sengoku.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Makes tipped arrows throwable like kunai instead of requiring a bow.
 */
public class ThrowableArrowItem extends ArrowItem {
    
    public ThrowableArrowItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack, entity) - timeLeft;
            if (chargeTime < 10) {
                return;
            }
            
            if (!level.isClientSide) {
                // Create arrow entity using the vanilla Arrow class
                Arrow arrow = new Arrow(level, player, stack.copyWithCount(1), null);
                
                // Calculate throw power (1.0 to 2.5 based on charge time)
                float power = Math.min(chargeTime / 20.0F, 1.0F);
                float velocity = power * 2.5F;
                
                // Shoot the arrow
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
                
                // Set pickup rules
                arrow.pickup = player.getAbilities().instabuild ? 
                    AbstractArrow.Pickup.CREATIVE_ONLY : 
                    AbstractArrow.Pickup.ALLOWED;
                
                // Make arrow critical if fully charged
                if (power == 1.0F) {
                    arrow.setCritArrow(true);
                }
                
                // Add entity to world
                level.addFreshEntity(arrow);
                
                // Play throw sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
                
                // Consume arrow (unless creative mode)
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.getInventory().removeItem(stack);
                    }
                }
            }
            
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Same as bow
    }
    
    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.SPEAR; // Trident throwing animation
    }
}
