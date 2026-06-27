package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class HotSpringWaterBlock extends LiquidBlock {

    public HotSpringWaterBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        
        // Apply healing effects to living entities
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            // Add regeneration effect every few seconds
            if (level.getGameTime() % 40 == 0) { // Every 2 seconds
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0)); // 5 seconds of regen I
                
                // Small chance for instant healing
                if (level.random.nextFloat() < 0.1f) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        
        // Add steam particles
        if (random.nextInt(15) == 0) {
            level.addParticle(ParticleTypes.CLOUD,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                pos.getY() + 1.0,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
        }
        
        // Add bubble particles occasionally
        if (random.nextInt(25) == 0) {
            level.addParticle(ParticleTypes.BUBBLE,
                pos.getX() + random.nextDouble(),
                pos.getY() + random.nextDouble() * 0.5,
                pos.getZ() + random.nextDouble(),
                0.0, 0.05, 0.0);
        }
    }
}