package com.shioh.sengoku.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class BlockClashParticle extends TextureSheetParticle {
    protected final SpriteSet sprites;

    protected BlockClashParticle(ClientLevel world, double x, double y, double z, SpriteSet sprites) {
        super(world, x, y, z);
        this.sprites = sprites;
        this.lifetime = 8 + this.random.nextInt(4); 
        
        this.quadSize = 1.5F;
        
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        
        this.gravity = 0.0F;
        
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getRenderType() {
        return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
            return new BlockClashParticle(world, x, y, z, spriteSet);
        }
    }
}
