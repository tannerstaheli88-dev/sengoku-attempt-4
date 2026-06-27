package com.shioh.sengoku.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom musket smoke particle that uses campfire smoke texture but with faster dissipation
 */
@Environment(EnvType.CLIENT)
public class MusketSmokeParticle extends TextureSheetParticle {
    
    private final SpriteSet sprites;
    
    protected MusketSmokeParticle(ClientLevel level, double x, double y, double z, 
                                   double xSpeed, double ySpeed, double zSpeed, 
                                   SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        
        this.sprites = sprites;
        this.gravity = -0.1F; // Float upward slightly
        this.lifetime = 40; // Dissipate after 2 seconds (40 ticks) - much faster than campfire smoke
        this.quadSize = 0.7F; // Slightly larger initial size
        this.hasPhysics = false; // Don't collide with blocks
        
        // Start semi-transparent and fade out
        this.alpha = 0.7F;
        
        // White-gray smoke color
        this.rCol = 0.9F;
        this.gCol = 0.9F;
        this.bCol = 0.9F;
        
        this.setSpriteFromAge(sprites);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Fade out and grow over time
        this.alpha = Math.max(0, 0.7F - (0.7F * this.age / this.lifetime));
        this.quadSize += 0.02F; // Gradually expand
        
        // Update sprite based on age
        this.setSpriteFromAge(sprites);
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return new MusketSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
