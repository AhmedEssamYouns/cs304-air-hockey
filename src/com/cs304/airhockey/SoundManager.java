package com.cs304.airhockey;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Simple singleton sound manager for background music + SFX.
 *
 * Expects these files on the classpath (resources root):
 *   /sounds/game_music.wav
 *   /sounds/hit.wav
 *   /sounds/onclick.wav
 */
public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private boolean soundEnabled = true;

    // Background music: just one looped clip
    private Clip gameMusicClip;

    // SFX pools so multiple sounds can overlap
    private static final int POOL_SIZE = 5;
    private Clip[] hitPool;
    private Clip[] clickPool;

    private SoundManager() {
        // Load music (single clip)
        gameMusicClip = loadClip("/sounds/game_music.wav");

        // Load SFX into small pools so they can overlap
        hitPool = new Clip[POOL_SIZE];
        clickPool = new Clip[POOL_SIZE];

        for (int i = 0; i < POOL_SIZE; i++) {
            hitPool[i] = loadClip("/sounds/hit.wav");
            clickPool[i] = loadClip("/sounds/onclick.wav");
        }
    }

    // ---------- Loading helper ----------

    private Clip loadClip(String resourcePath) {
        try {
            URL url = SoundManager.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("❌ Sound not found on classpath: " + resourcePath);
                return null;
            }
            System.out.println("✅ Loading sound from: " + url);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (Exception ex) {
            System.err.println("❌ Failed to load sound: " + resourcePath);
            ex.printStackTrace();
            return null;
        }
    }

    // ---------- Global enable/disable ----------

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopGameMusic();
        }
    }

    /** Toggle and return the new value. */
    public boolean toggleSoundEnabled() {
        setSoundEnabled(!soundEnabled);
        return soundEnabled;
    }

    // ---------- Music (single loop, no overlap needed) ----------

    public void playGameMusicLoop() {
        if (!soundEnabled || gameMusicClip == null) return;

        if (gameMusicClip.isRunning()) {
            return; // already playing
        }
        gameMusicClip.setFramePosition(0);
        gameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopGameMusic() {
        if (gameMusicClip != null && gameMusicClip.isRunning()) {
            gameMusicClip.stop();
        }
    }

    // ---------- SFX with overlap support ----------

    private void playFromPool(Clip[] pool) {
        if (!soundEnabled || pool == null) return;

        // 1) Try to find a free clip
        for (Clip clip : pool) {
            if (clip == null) continue;
            if (!clip.isRunning()) {
                clip.setFramePosition(0);
                clip.start();
                return;
            }
        }

        // 2) All clips are busy -> just restart the first one
        Clip first = pool[0];
        if (first != null) {
            first.stop();
            first.setFramePosition(0);
            first.start();
        }
    }

    /** Called when puck hits a paddle – can overlap if hit happens fast. */
    public void playHit() {
        playFromPool(hitPool);
    }

    /** Called when menu items / options are selected or moved. */
    public void playClick() {
        playFromPool(clickPool);
    }
}
