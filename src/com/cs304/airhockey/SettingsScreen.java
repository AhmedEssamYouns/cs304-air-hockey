package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Settings screen (currently just sound ON/OFF).
 */
public class SettingsScreen {

    public void draw(TextRenderer r, int w, int h) {
        int x = 80;
        int y = h - 80;

        boolean soundOn = SoundManager.getInstance().isSoundEnabled();

        r.setColor(1f, 1f, 1f, 1f);
        r.draw("Settings", w / 2 - 50, y);
        y -= 40;

        r.setColor(0.9f, 0.9f, 0.9f, 1f);
        String soundLine = "Sound: " + (soundOn ? "ON" : "OFF");
        r.draw(soundLine, x, y);
        y -= 30;

        r.setColor(0.7f, 0.7f, 0.7f, 1f);
        r.draw("Press ENTER or SPACE to toggle sound.", x, y);
        y -= 25;
        r.draw("Press ESC to go back to the main menu.", x, y);
    }
}
