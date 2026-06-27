package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace ravagers and witches with bandits/ronin on horses in village raids.
 */
@Mixin(Raid.class)
public abstract class RaidRavagerReplacementMixin {
    
    /**
     * Replace ravagers and witches with bandits or ronin riding armored, saddled horses.
     */
    @Inject(method = "joinRaid", at = @At("HEAD"), cancellable = true)
    private void replaceWithMountedRaider(int wave, Raider raider, BlockPos pos, boolean placeRaider, 
                                        CallbackInfo ci) {
        // Prevent clan members from joining raids: cancel if raider is a clan entity
        if (raider != null) {
            if (raider.getType() == ModEntities.KOBAYAKAWA_ASHIGARU || raider.getType() == ModEntities.KOBAYAKAWA_SAMURAI || raider.getType() == ModEntities.KOBAYAKAWA_SOHEI ||
                raider.getType() == ModEntities.TAKEDA_ASHIGARU || raider.getType() == ModEntities.TAKEDA_SAMURAI || raider.getType() == ModEntities.TAKEDA_SOHEI ||
                raider.getType() == ModEntities.SATOMI_ASHIGARU || raider.getType() == ModEntities.SATOMI_SAMURAI || raider.getType() == ModEntities.SATOMI_SOHEI) {
                // Cancel joinRaid so these clan mobs are not recruited into the raid
                ci.cancel();
                return;
            }
        }

        // Check if this is a ravager or witch
        if (raider != null && (raider.getType() == EntityType.RAVAGER || raider.getType() == EntityType.WITCH)) {
            // Get the level from the raider
            if (raider.level() instanceof ServerLevel serverLevel && pos != null) {
                // Discard the original entity
                raider.discard();
                
                // Randomly choose bandit or ronin
                EntityType<? extends Raider> riderType = serverLevel.random.nextBoolean() 
                    ? ModEntities.BANDIT : ModEntities.RONIN;
                
                // Create the raider
                Raider newRaider = riderType.create(serverLevel);
                if (newRaider != null) {
                    // Position the raider
                    newRaider.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                                   serverLevel.random.nextFloat() * 360F, 0.0F);
                    
                    // Ensure they have weapons
                    newRaider.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), 
                                          MobSpawnType.EVENT, null);
                    
                    // Spawn a horse
                    Horse horse = EntityType.HORSE.create(serverLevel);
                    if (horse != null) {
                        horse.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                                   serverLevel.random.nextFloat() * 360F, 0.0F);
                        horse.setTamed(true);
                        horse.setAge(0); // Adult
                        
                        // Set random horse variant (color and markings)
                        // There are 7 base colors and 5 marking patterns = 35 total combinations
                        int variant = serverLevel.random.nextInt(35);
                        horse.setVariant(net.minecraft.world.entity.animal.horse.Variant.byId(variant));
                        
                        // Set horse speed based on rider type
                        // Ronin get faster horses (0.35-0.4), Bandits get normal speed (0.25-0.3)
                        double speed;
                        if (riderType == ModEntities.RONIN) {
                            speed = 0.35 + serverLevel.random.nextDouble() * 0.05; // Fast horses for ronin
                        } else {
                            speed = 0.25 + serverLevel.random.nextDouble() * 0.05; // Normal horses for bandits
                        }
                        horse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                            .setBaseValue(speed);
                        
                        // Add saddle
                        horse.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);
                        
                        // Add armor (70% iron, 30% leather)
                        if (serverLevel.random.nextFloat() < 0.7f) {
                            horse.setBodyArmorItem(new ItemStack(Items.IRON_HORSE_ARMOR));
                        } else {
                            horse.setBodyArmorItem(new ItemStack(Items.LEATHER_HORSE_ARMOR));
                        }
                        
                        // Spawn horse first
                        serverLevel.addFreshEntity(horse);
                        
                        // Mount the raider on the horse
                        newRaider.startRiding(horse, true);
                    }
                    
                    // Add raider to world and raid
                    serverLevel.addFreshEntity(newRaider);
                    ((Raid)(Object)this).joinRaid(wave, newRaider, pos, true);
                }
                
                // Cancel the original entity spawn
                ci.cancel();
            }
        }
    }
}
