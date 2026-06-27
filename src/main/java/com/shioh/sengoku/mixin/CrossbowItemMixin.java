package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.shioh.sengoku.util.PlayerNoiseTracker;

import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {
    public CrossbowItemMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "getAllSupportedProjectiles", at = @At("HEAD"), cancellable = true)
    private void onlySpectralArrows(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        cir.setReturnValue(stack -> stack.is(Items.SPECTRAL_ARROW));
    }

    /**
     * Intercept the shootProjectile method to make crossbow projectiles faster and straighter.
     * Note: Spectral arrows already get 8x speed boost from SpectralArrowVelocityMixin,
     * so this adds an additional boost when fired from crossbow for gun-like behavior.
     * 
     * Also spawns dramatic smoke particles at the shooter's position to simulate musket discharge.
     */
    @Inject(method = "shootProjectile", at = @At("RETURN"))
    private static void makeProjectileFasterAndStraighter(
            LivingEntity shooter, 
            Projectile projectile, 
            int index, 
            float velocity, 
            float inaccuracy, 
            float angle, 
            LivingEntity target, 
            CallbackInfo ci) {
        
        // Add an additional 2x velocity multiplier on top of the spectral arrow's existing 8x
        // This results in ~16x total speed (8x from SpectralArrowVelocityMixin * 2x from here)
        Vec3 currentVelocity = projectile.getDeltaMovement();
        projectile.setDeltaMovement(currentVelocity.scale(2.0));
        
        // Gravity is already removed by SpectralArrowVelocityMixin, but ensure it stays off
        projectile.setNoGravity(true);
        
        // Spawn dramatic smoke particles at shooter's position (tanegashima discharge effect)
        // Tanegashima produces a large forward-erupting cloud with significant muzzle blast
        if (shooter.level() instanceof ServerLevel serverLevel) {
            // Get shooter's position and look direction
            Vec3 eyePos = shooter.getEyePosition();
            Vec3 lookVec = shooter.getLookAngle();
            
            // Spawn smoke further in front to avoid blinding the player
            double muzzleDistance = 2.0; // 2 blocks in front of shooter
            double smokeX = eyePos.x + lookVec.x * muzzleDistance;
            double smokeY = eyePos.y + lookVec.y * muzzleDistance - 0.1; // Slightly lower
            double smokeZ = eyePos.z + lookVec.z * muzzleDistance;
            
            // Create muzzle flash particle at gun tip (closer than smoke)
            double flashDistance = 1.2; // At the gun barrel
            double flashX = eyePos.x + lookVec.x * flashDistance;
            double flashY = eyePos.y + lookVec.y * flashDistance - 0.1;
            double flashZ = eyePos.z + lookVec.z * flashDistance;
            
            // Single bright gunfire flash particle at muzzle
            serverLevel.sendParticles(
                com.shioh.sengoku.particle.ModParticles.GUNFIRE_FLASH,
                flashX,
                flashY,
                flashZ,
                1,
                0, 0, 0, // No velocity - stays in place
                0
            );
            
            // Create brief muzzle flash (light source for 2 ticks = 0.1 seconds)
            BlockPos flashPos = BlockPos.containing(smokeX, smokeY, smokeZ);
            
            // Only place light if the position is air or replaceable
            if (serverLevel.getBlockState(flashPos).isAir()) {
                serverLevel.setBlock(flashPos, Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, 15), 3);
                
                // Schedule removal using server executor with delay
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    serverLevel.getServer().execute(() -> {
                        if (serverLevel.getBlockState(flashPos).is(Blocks.LIGHT)) {
                            serverLevel.removeBlock(flashPos, false);
                        }
                    });
                }, 100, java.util.concurrent.TimeUnit.MILLISECONDS); // 100ms = 2 ticks
            }
            
            // Initial muzzle flash/blast - rapid forward expansion
            // This simulates the explosive discharge of black powder
            for (int i = 0; i < 25; i++) {
                // Small spread at spawn point
                double spreadX = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                double spreadY = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                double spreadZ = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                
                // Strong forward velocity in aim direction with slight spread
                double velX = lookVec.x * (0.5 + serverLevel.random.nextDouble() * 0.2) + (serverLevel.random.nextDouble() - 0.5) * 0.05;
                double velY = lookVec.y * (0.5 + serverLevel.random.nextDouble() * 0.2) + 0.08; // Slight upward
                double velZ = lookVec.z * (0.5 + serverLevel.random.nextDouble() * 0.2) + (serverLevel.random.nextDouble() - 0.5) * 0.05;
                
                serverLevel.sendParticles(
                    com.shioh.sengoku.particle.ModParticles.MUSKET_SMOKE,
                    smokeX + spreadX,
                    smokeY + spreadY,
                    smokeZ + spreadZ,
                    1,
                    velX, velY, velZ,
                    0.02
                );
            }
            
            // Secondary billowing smoke - slower but still forward-moving
            // This represents the lingering black powder smoke
            for (int i = 0; i < 40; i++) {
                double spreadX = (serverLevel.random.nextDouble() - 0.5) * 0.15;
                double spreadY = (serverLevel.random.nextDouble() - 0.5) * 0.15;
                double spreadZ = (serverLevel.random.nextDouble() - 0.5) * 0.15;
                
                // Forward velocity with minimal spread
                double velX = lookVec.x * (0.3 + serverLevel.random.nextDouble() * 0.15) + (serverLevel.random.nextDouble() - 0.5) * 0.03;
                double velY = lookVec.y * (0.3 + serverLevel.random.nextDouble() * 0.15) + 0.05; // Float upward
                double velZ = lookVec.z * (0.3 + serverLevel.random.nextDouble() * 0.15) + (serverLevel.random.nextDouble() - 0.5) * 0.03;
                
                serverLevel.sendParticles(
                    com.shioh.sengoku.particle.ModParticles.MUSKET_SMOKE,
                    smokeX + spreadX,
                    smokeY + spreadY,
                    smokeZ + spreadZ,
                    1,
                    velX, velY, velZ,
                    0.015
                );
            }
            
            // Dense initial smoke puff at muzzle - straight forward
            // This creates the characteristic "blast" appearance
            for (int i = 0; i < 15; i++) {
                double spreadX = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                double spreadY = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                double spreadZ = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                
                // Strong forward velocity
                double velX = lookVec.x * (0.6 + serverLevel.random.nextDouble() * 0.2);
                double velY = lookVec.y * (0.6 + serverLevel.random.nextDouble() * 0.2) + 0.1;
                double velZ = lookVec.z * (0.6 + serverLevel.random.nextDouble() * 0.2);
                
                serverLevel.sendParticles(
                    com.shioh.sengoku.particle.ModParticles.MUSKET_SMOKE,
                    smokeX + spreadX,
                    smokeY + spreadY,
                    smokeZ + spreadZ,
                    1,
                    velX, velY, velZ,
                    0.025
                );
            }
            // Mark shooter as noisy when firing a crossbow so nearby mobs detect them
            try {
                if (shooter instanceof net.minecraft.world.entity.player.Player p) {
                    PlayerNoiseTracker.getInstance().addNoise(p, PlayerNoiseTracker.CROSSBOW_NOISE);
                }
            } catch (Throwable ignored) {}
        }
    }
}
