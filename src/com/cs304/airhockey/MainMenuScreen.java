package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Simple main menu screen for the Air Hockey game.
 */
public class MainMenuScreen {

    private final String[] items = {
            "Start Game",
            "High Scores",
            "Quit"
    };

    private int selected = 0;

    public void moveUp() {
        selected--;
        if (selected < 0) {
            selected = items.length - 1;
        }
    }

    public void moveDown() {
        selected++;
        if (selected >= items.length) {
            selected = 0;
        }
    }

    public String getSelectedAction() {
        switch (selected) {
            case 0: return "start";
            case 1: return "highscores";
            case 2: return "quit";
            default: return "start";
        }
    }

    public void draw(TextRenderer r, int w, int h) {
        int centerX = w / 2;
        int baseY = h / 2 + 40;

        r.setColor(1f, 1f, 1f, 1f);
        r.draw("CS304 Air Hockey", centerX - 120, h - 80);

        for (int i = 0; i < items.length; i++) {
            if (i == selected) {
                r.setColor(1f, 1f, 0f, 1f);
            } else {
                r.setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            r.draw(items[i], centerX - 60, baseY - i * 30);
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Use UP / DOWN to navigate, ENTER to select, ESC to quit",
               40, 40);
    }
}
