package com.cs304.airhockey;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Simple singleton sound manager for background music.
 * Uses /sounds/game_music.wav from the classpath.
 */
public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private boolean soundEnabled = true;
    private Clip gameMusicClip;

    private SoundManager() {
        loadGameMusic();
    }

    private void loadGameMusic() {
        try {
            // sound file must be at: resources/sounds/game_music.wav
            URL url = SoundManager.class.getResource("/sounds/game_music.wav");
            if (url == null) {
                System.err.println("❌ Sound file NOT FOUND: /sounds/game_music.wav");
                return;
            }

            System.out.println("✅ Loading sound from: " + url);

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            gameMusicClip = AudioSystem.getClip();
            gameMusicClip.open(audioIn);

            System.out.println("✅ Sound loaded OK");
        } catch (Exception ex) {
            System.err.println("❌ Failed to load game music:");
            ex.printStackTrace();
            gameMusicClip = null;
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopGameMusic();
        }
    }

    /**
     * Toggle and return the new value.
     */
    public boolean toggleSoundEnabled() {
        setSoundEnabled(!soundEnabled);
        return soundEnabled;
    }

    public void playGameMusicLoop() {
        if (!soundEnabled) {
            System.out.println("🔇 Sound disabled, not playing music.");
            return;
        }
        if (gameMusicClip == null) {
            System.out.println("⚠️ No music clip loaded, cannot play.");
            return;
        }

        if (gameMusicClip.isRunning()) {
            // already playing
            return;
        }

        gameMusicClip.setFramePosition(0);
        gameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        System.out.println("🎵 Game music started.");
    }

    public void stopGameMusic() {
        if (gameMusicClip != null && gameMusicClip.isRunning()) {
            gameMusicClip.stop();
            System.out.println("⏹ Game music stopped.");
        }
    }
}
