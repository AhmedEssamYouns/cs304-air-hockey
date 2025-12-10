package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Simple main menu screen for the Air Hockey game.
 * Can show either "Start Game" or "Continue" depending on whether
 * a match is already in progress.
 */
public class MainMenuScreen {

    private static class Item {
        String label;
        String action;

        Item(String label, String action) {
            this.label = label;
            this.action = action;
        }
    }

    private Item[] items;
    private int selected = 0;

    public MainMenuScreen() {
        open(false);
    }

    /**
     * Configure the menu for either "no active game" or "game in progress".
     */
    public void open(boolean canContinue) {
        if (canContinue) {
            items = new Item[] {
                    new Item("Continue", "continue"),
                    new Item("Start New Game", "start"),
                    new Item("End Current Game", "endgame"),
                    new Item("Instructions", "instructions"),
                    new Item("High Scores", "highscores"),
                    new Item("Quit", "quit")
            };
        } else {
            items = new Item[] {
                    new Item("Start Game", "start"),
                    new Item("Instructions", "instructions"),
                    new Item("High Scores", "highscores"),
                    new Item("Quit", "quit")
            };
        }
        selected = 0;
    }

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
        if (items == null || items.length == 0) return "start";
        return items[selected].action;
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
            r.draw(items[i].label, centerX - 80, baseY - i * 30);
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Use UP / DOWN to navigate, ENTER to select, ESC to quit", 40, 40);
    }
}
