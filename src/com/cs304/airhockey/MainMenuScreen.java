package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Main menu screen.
 *
 * When a game is currently running (hasActiveGame = true),
 * the menu shows:
 *   Continue
 *   End Game
 *   Instructions
 *   High Scores
 *   Quit
 *
 * Otherwise:
 *   Start Game
 *   Instructions
 *   High Scores
 *   Quit
 */
public class MainMenuScreen {

    private boolean hasActiveGame = false;
    private int selected = 0;

    public void open(boolean hasActiveGame) {
        this.hasActiveGame = hasActiveGame;
        this.selected = 0;
    }

    private String[] getItems() {
        if (hasActiveGame) {
            return new String[] {
                    "Continue",
                    "End Game",
                    "Instructions",
                    "High Scores",
                    "Quit"
            };
        } else {
            return new String[] {
                    "Start Game",
                    "Instructions",
                    "High Scores",
                    "Quit"
            };
        }
    }

    public void moveUp() {
        String[] items = getItems();
        selected--;
        if (selected < 0) {
            selected = items.length - 1;
        }
    }

    public void moveDown() {
        String[] items = getItems();
        selected++;
        if (selected >= items.length) {
            selected = 0;
        }
    }

    /**
     * Returns a logical action string, not the label:
     *   "start", "continue", "endgame", "instructions", "highscores", "quit"
     */
    public String getSelectedAction() {
        String[] items = getItems();
        if (selected < 0) selected = 0;
        if (selected >= items.length) selected = items.length - 1;

        String label = items[selected];

        if ("Start Game".equals(label)) return "start";
        if ("Continue".equals(label)) return "continue";
        if ("End Game".equals(label)) return "endgame";
        if ("Instructions".equals(label)) return "instructions";
        if ("High Scores".equals(label)) return "highscores";
        if ("Quit".equals(label)) return "quit";

        return "start";
    }

    public void draw(TextRenderer r, int w, int h) {
        String[] items = getItems();
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
            r.draw(items[i], centerX - 70, baseY - i * 30);
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Use UP / DOWN to navigate, ENTER to select, ESC to quit/close",
                40, 40);
    }
}
