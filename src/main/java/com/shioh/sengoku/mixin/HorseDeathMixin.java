package com.shioh.sengoku.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spawns a zombie horse (Sagari yokai) when a horse dies
 * 30% chance on death - represents the cursed transformation
 */
@Mixin(LivingEntity.class)
public class HorseDeathMixin {
    
    @Inject(method = "die", at = @At("HEAD"))
    private void sengoku$spawnZombieHorseOnDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Only process horses
        if (entity instanceof Horse horse) {
            // Only spawn on server side
            if (!horse.level().isClientSide && horse.level() instanceof ServerLevel level) {
                // 30% chance to spawn zombie horse
                if (horse.getRandom().nextFloat() < 0.3f) {
                    // Spawn zombie horse at horse's position
                    ZombieHorse zombieHorse = EntityType.ZOMBIE_HORSE.create(level);
                    if (zombieHorse != null) {
                        zombieHorse.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
                        zombieHorse.setTamed(true); // Make it tamed like in the datapack
                        level.addFreshEntity(zombieHorse);
                    }
                }
            }
        }
    }
}
