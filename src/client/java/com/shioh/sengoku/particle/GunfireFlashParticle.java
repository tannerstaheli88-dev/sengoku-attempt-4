package com.shioh.sengoku.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Brief muzzle flash particle that appears for just 2 ticks at the gun barrel
 */
@Environment(EnvType.CLIENT)
public class GunfireFlashParticle extends TextureSheetParticle {
    
    private final SpriteSet sprites;
    
    protected GunfireFlashParticle(ClientLevel level, double x, double y, double z, 
                                    double xSpeed, double ySpeed, double zSpeed, 
                                    SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0); // No velocity - stays in place
        
        this.sprites = sprites;
        this.gravity = 0; // No gravity
        this.lifetime = 2; // Only 2 ticks = 0.1 seconds - very brief flash
        this.quadSize = 0.8F; // Medium size
        this.hasPhysics = false; // Don't collide with blocks
        
        // Start fully opaque
        this.alpha = 1.0F;
        
        // Bright white-yellow for flash
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 0.9F;
        
        this.setSpriteFromAge(sprites);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Fade out very quickly
        this.alpha = 1.0F - ((float)this.age / (float)this.lifetime);
        
        // Update sprite
        this.setSpriteFromAge(sprites);
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0; // Max brightness - glowing effect
    }
    
    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        
        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }
        
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, 
                                       double x, double y, double z, 
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new GunfireFlashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
