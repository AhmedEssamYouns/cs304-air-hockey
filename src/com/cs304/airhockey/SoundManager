package com.cs304.airhockey;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Simple singleton sound manager.
 * Uses WAV sounds from classpath:
 *  - /sounds/game_music.wav
 *  - /sounds/hit.wav
 *  - /sounds/onclick.wav
 *  - /sounds/game-over.wav
 */
public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private boolean soundEnabled = true;

    // Background music clip (looped)
    private Clip musicClip;

    // ---- ctor ----
    private SoundManager() {
        // load only the music as persistent Clip
        musicClip = loadClip("/sounds/game_music.wav");

        // just to validate paths at startup (no need to keep clips)
        preload("/sounds/hit.wav");
        preload("/sounds/onclick.wav");
        preload("/sounds/game-over.wav");
    }

    // ============ Loading helpers ============

    /** Load a clip that we keep (for looped music). */
    private Clip loadClip(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                System.err.println("❌ Sound not found on classpath: " + path);
                return null;
            }
            System.out.println("✅ Loading (loop) sound from: " + url);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.err.println("❌ Failed to load loop sound: " + path);
            ex.printStackTrace();
            return null;
        }
    }

    /** Just check that a resource exists & is a valid WAV. */
    private void preload(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                System.err.println("❌ (preload) Sound not found: " + path);
                return;
            }
            System.out.println("✅ (preload) Found sound: " + url);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            audioIn.close();
        } catch (Exception ex) {
            System.err.println("❌ (preload) Failed for: " + path);
            ex.printStackTrace();
        }
    }

    /** For short SFX that can overlap (hit, click, game-over). Creates a fresh Clip each time. */
    private void playOneShot(String path) {
        if (!soundEnabled) return;
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                System.err.println("❌ One-shot sound not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ex) {
            System.err.println("❌ Failed to play one-shot: " + path);
            ex.printStackTrace();
        }
    }

    // ============ Global sound toggle ============

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

    // ============ Background music ============

    public void playGameMusicLoop() {
        if (!soundEnabled || musicClip == null) return;
        if (musicClip.isRunning()) return;

        musicClip.setFramePosition(0);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopGameMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    // ============ SFX ============

    public void playClick() {
        playOneShot("/sounds/onclick.wav");
    }

    public void playHit() {
        playOneShot("/sounds/hit.wav");
    }

    /**
     * Game over vs AI:
     *  - stop bg music
     *  - play /sounds/game-over.wav once
     *  - after delayMs, resume bg music (if sound still enabled)
     */
    public void playGameOverThenResume(final long delayMs) {
        if (!soundEnabled) return;

        // pause bg music first
        stopGameMusic();

        // play game over sound once
        playOneShot("/sounds/game-over.wav");

        // resume bg after delay in a background thread
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {
            }
            if (soundEnabled) {
                playGameMusicLoop();
            }
        });

        t.setDaemon(true);
        t.start();
    }
}
