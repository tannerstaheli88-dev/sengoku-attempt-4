package com.shioh.sengoku.item;

import com.shioh.sengoku.entity.KunaiEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Throwable kunai item that creates a KunaiEntity projectile.
 * Can be thrown like a trident with proper physics.
 */
public class KunaiItem extends Item {
    private final double damage;
    
    public KunaiItem(double damage, Item.Properties properties) {
        super(properties);
        this.damage = damage;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack, entity) - timeLeft;
            if (chargeTime < 10) {
                return;
            }
            
            if (!level.isClientSide) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
                
                // Create and launch kunai entity
                KunaiEntity kunai = new KunaiEntity(level, player, stack);
                kunai.setOwner(player);
                
                // Calculate throw power (1.0 to 2.5 based on charge time)
                float power = Math.min(chargeTime / 20.0F, 1.0F);
                float velocity = power * 2.5F;
                
                // Shoot the kunai
                kunai.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
                
                // Add slight upward arc for better feel
                if (power == 1.0F) {
                    kunai.setCritArrow(true);
                }
                
                // Set pickup rules
                kunai.pickup = player.getAbilities().instabuild ? 
                    net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY : 
                    net.minecraft.world.entity.projectile.AbstractArrow.Pickup.ALLOWED;
                
                // Add entity to world
                boolean spawned = level.addFreshEntity(kunai);
                com.shioh.sengoku.sengokuFabric.LOGGER.info("Spawning kunai entity. Success: {}, Velocity: {}", spawned, velocity);
                
                // Play throw sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 
                    1.0F, 1.0F);
                
                if (!player.getAbilities().instabuild) {
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR; // Trident throwing animation
    }
    
    public double getDamage() {
        return this.damage;
    }
}
