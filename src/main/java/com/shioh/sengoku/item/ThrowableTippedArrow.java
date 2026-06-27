package com.shioh.sengoku.item;

import com.shioh.sengoku.entity.KunaiEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

/**
 * Throwable kunai that can ONLY be thrown by hand, not fired from bows/crossbows.
 * Uses custom KunaiEntity for proper rendering and potion effects.
 */
public class ThrowableTippedArrow extends Item {
    
    public ThrowableTippedArrow(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Ensure the item has potion contents (fix for creative menu items)
        if (!itemstack.has(DataComponents.POTION_CONTENTS)) {
            itemstack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        }
        
        player.startUsingItem(hand);
        return InteractionResultHolder.success(itemstack);
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack, entity) - timeLeft;
            if (chargeTime < 10) {
                return;
            }
            
            if (!level.isClientSide) {
                // Create kunai entity with the item stack for potion effects
                KunaiEntity kunai = new KunaiEntity(level, player, stack.copyWithCount(1));
                
                // Calculate throw power
                float power = Math.min(chargeTime / 20.0F, 1.0F);
                float velocity = power * 2.5F;
                
                // Shoot the kunai
                kunai.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
                
                // Kunai are consumed when thrown
                kunai.pickup = player.getAbilities().instabuild ? 
                    AbstractArrow.Pickup.CREATIVE_ONLY : 
                    AbstractArrow.Pickup.DISALLOWED;
                
                // Critical hit if fully charged
                if (power == 1.0F) {
                    kunai.setCritArrow(true);
                }
                
                // Spawn kunai
                level.addFreshEntity(kunai);
                
                // Play sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 
                    0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                
                // Consume kunai
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }
}
