package com.shioh.sengoku.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class HotSpringWaterBucketItem extends BucketItem {
    
    public HotSpringWaterBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // If player is holding the bucket and sneaking, drink it for healing effects
        if (player.isCrouching() && !level.isClientSide) {
            // Give healing and regeneration effects
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1)); // Instant healing II
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0)); // Regeneration I for 30 seconds
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0)); // Fire resistance for 1 minute
            
            // Play drinking sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // Add steam particles around player
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 10; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                        player.getX() + (level.random.nextDouble() - 0.5) * 2,
                        player.getY() + 1 + level.random.nextDouble(),
                        player.getZ() + (level.random.nextDouble() - 0.5) * 2,
                        1, 0.0, 0.05, 0.0, 0.02);
                }
            }
            
            // Return empty bucket
            return InteractionResultHolder.success(new ItemStack(Items.BUCKET));
        }
        
        // Otherwise, use normal bucket placement behavior
        return super.use(level, player, hand);
    }
}