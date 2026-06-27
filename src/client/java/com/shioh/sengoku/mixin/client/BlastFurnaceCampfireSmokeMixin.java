package com.shioh.sengoku.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Make lit blast furnaces emit campfire-like smoke client-side exactly like a campfire.
 */
@Mixin(targets = "net.minecraft.world.level.block.BlastFurnaceBlock")
public abstract class BlastFurnaceCampfireSmokeMixin {

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void sengoku$spawnCampfireSmoke(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!(level instanceof ClientLevel)) return;
        
        // Only emit campfire smoke when the furnace is lit
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {
            // Cancel default blast furnace particles (lava sparks)
            ci.cancel();
            
            // Spawn exact vanilla campfire smoke particles
            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY() + 0.5;
            double z = (double)pos.getZ() + 0.5;
            
            // Campfire spawns 10 cosy smoke particles per tick
            for(int i = 0; i < 10; ++i) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                    x + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                    y + random.nextDouble() + random.nextDouble(),
                    z + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                    0.0, 0.07, 0.0);
            }
            
            // Occasionally play campfire crackle sound (like vanilla campfire)
            if (random.nextInt(10) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F, false);
            }
        }
    }
}
