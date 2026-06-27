package com.shioh.sengoku.mixin;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes tipped arrows throwable like tridents instead of requiring a bow.
 * Only affects TIPPED arrows, not regular arrows.
 */
@Mixin(ArrowItem.class)
public abstract class ThrowableArrowMixin {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Only make tipped arrows throwable, not regular arrows
        if (stack.is(Items.TIPPED_ARROW)) {
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        }
    }
    
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void onReleaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfo ci) {
        // Only make tipped arrows throwable, not regular arrows
        if (!stack.is(Items.TIPPED_ARROW)) {
            return;
        }
        
        if (entity instanceof Player player) {
            int chargeTime = 72000 - timeLeft;
            if (chargeTime < 10) {
                return;
            }
            
            if (!level.isClientSide) {
                // Create arrow entity - copy stack to preserve potion data
                ItemStack arrowStack = stack.copyWithCount(1);
                Arrow arrow = new Arrow(level, player, arrowStack, null);
                
                // Calculate throw power
                float power = Math.min(chargeTime / 20.0F, 1.0F);
                float velocity = power * 2.5F;
                
                // Shoot the arrow
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
                
                // Set pickup rules
                arrow.pickup = player.getAbilities().instabuild ? 
                    AbstractArrow.Pickup.CREATIVE_ONLY : 
                    AbstractArrow.Pickup.DISALLOWED;
                
                // Critical if fully charged
                if (power == 1.0F) {
                    arrow.setCritArrow(true);
                }
                
                level.addFreshEntity(arrow);
                
                // Play sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
                
                // Consume arrow
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            
            player.awardStat(Stats.ITEM_USED.get((ArrowItem)(Object)this));
            ci.cancel();
        }
    }
    
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void onGetUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        // Only affect tipped arrows
        if (stack.is(Items.TIPPED_ARROW)) {
            cir.setReturnValue(72000);
        }
    }
    
    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void onGetUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        // Only affect tipped arrows
        if (stack.is(Items.TIPPED_ARROW)) {
            cir.setReturnValue(UseAnim.SPEAR);
        }
    }
}
