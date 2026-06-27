package com.shioh.sengoku.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Blood particle that sprays out from entities when hit by melee weapons.
 * we are charlie kirk 
 * 
 * Behavior:
 * - Sprays outward from the hit entity
 * - Falls due to gravity like real blood droplets
 * - Gradually fades and shrinks as it falls
 * - Short lifetime (about 1-2 seconds)
 */
@Environment(EnvType.CLIENT)
public class BloodParticle extends TextureSheetParticle {
    
    private final SpriteSet sprites;
    
    protected BloodParticle(ClientLevel level, double x, double y, double z, 
                           double xSpeed, double ySpeed, double zSpeed, 
                           SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        
        this.sprites = sprites;
        
        // Blood is affected by gravity
        this.gravity = 0.75F; // Falls faster than normal particles
        
        // Short lifetime - blood droplets dissipate quickly
        this.lifetime = 20 + this.random.nextInt(20); // 1-2 seconds (20-40 ticks)
        
        // Small droplet size
        this.quadSize = 0.15F + this.random.nextFloat() * 0.1F; // Varies between 0.15 and 0.25
        
        // Blood collides with blocks and ground
        this.hasPhysics = true;
        
        // Start opaque, will fade over time
        this.alpha = 1.0F;
        
        // Deep red blood color with slight variation
        this.rCol = 0.6F + this.random.nextFloat() * 0.2F; // 0.6-0.8 red
        this.gCol = 0.0F + this.random.nextFloat() * 0.1F; // 0.0-0.1 green (very dark)
        this.bCol = 0.0F + this.random.nextFloat() * 0.05F; // 0.0-0.05 blue (very dark)
        
        // Set initial sprite
        this.setSpriteFromAge(sprites);
    }
    
    @Override
    public void tick() {
        // Store old position for collision detection
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        
        // Age the particle
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        
        // Fade out as particle ages
        float ageRatio = (float)this.age / (float)this.lifetime;
        this.alpha = Math.max(0, 1.0F - ageRatio);
        
        // Shrink slightly as it ages
        this.quadSize = (0.15F + this.random.nextFloat() * 0.1F) * (1.0F - ageRatio * 0.5F);
        
        // Apply gravity
        this.yd -= 0.04D * this.gravity;
        
        // Move the particle
        this.move(this.xd, this.yd, this.zd);
        
        // Apply air resistance (blood droplets slow down in air)
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;
        
        // If on ground, slow down horizontal movement dramatically
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
            // Reduce vertical bounce
            if (Math.abs(this.yd) < 0.01D) {
                this.yd = 0;
            }
        }
        
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
            return new BloodParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
