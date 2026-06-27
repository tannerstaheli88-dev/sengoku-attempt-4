package com.shioh.sengoku.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Music instance with dynamic volume control for smooth fading.
 */
public class FadingMusicInstance extends AbstractTickableSoundInstance {
    private float targetVolume;
    private float volumeStep;

    protected FadingMusicInstance(SoundEvent event, float initialVolume) {
        super(event, SoundSource.MUSIC, RandomSource.create());
        this.volume = initialVolume;
        this.targetVolume = initialVolume;
        this.volumeStep = 0.0f;
        this.looping = false;
        this.delay = 0;
        this.relative = true;
    }

    public static FadingMusicInstance forMusic(SoundEvent event, float volume) {
        return new FadingMusicInstance(event, volume);
    }

    public void setTargetVolume(float target, int ticks) {
        this.targetVolume = Math.max(0.01f, Math.min(1.0f, target));
        if (ticks > 0) {
            this.volumeStep = (this.targetVolume - this.volume) / (float)ticks;
        } else {
            this.volume = this.targetVolume;
            this.volumeStep = 0.0f;
        }
    }

    public boolean isPlaying() {
        return !this.isStopped();
    }

    public boolean hasStopped() {
        return this.isStopped();
    }

    @Override
    public void tick() {
        if (this.volumeStep != 0.0f) {
            float newVol = this.volume + this.volumeStep;
            // Check if we've reached or passed the target
            if ((this.volumeStep > 0 && newVol >= this.targetVolume) ||
                (this.volumeStep < 0 && newVol <= this.targetVolume)) {
                this.volume = this.targetVolume;
                this.volumeStep = 0.0f;
            } else {
                this.volume = newVol;
            }
        }
    }
}
