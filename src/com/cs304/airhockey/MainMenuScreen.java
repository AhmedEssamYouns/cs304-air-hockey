package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.geom.Rectangle2D;

/**
 * Main menu screen for the Air Hockey game.
 * Fancy animated title + animated 3D-ish menu entries.
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
                    new Item("Continue Match",     "continue"),
                    new Item("Start New Game",    "start"),
                    new Item("End Current Game",  "endgame"),
                    new Item("Settings",          "settings"),
                    new Item("Instructions",      "instructions"),
                    new Item("High Scores",       "highscores"),
                    new Item("Quit",              "quit")
            };
        } else {
            items = new Item[] {
                    new Item("Start Game",        "start"),
                    new Item("Settings",          "settings"),
                    new Item("Instructions",      "instructions"),
                    new Item("High Scores",       "highscores"),
                    new Item("Quit",              "quit")
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

        // global time for animation
        double t = System.nanoTime() / 1_000_000_000.0;
        float glow = 0.6f + 0.4f * (float) Math.sin(t * 2.0);   // 0.2 .. 1.0
        float wobble = (float) Math.sin(t * 3.0) * 4f;          // small up/down wobble

        // ===== TITLE (big, 3D-ish, glowing) =====
        String title = "CS304 AIR HOCKEY";
        Rectangle2D tb = r.getBounds(title);
        int titleX = centerX - (int) (tb.getWidth() / 2);
        int titleY = h - 130 + (int) wobble;

        // shadow layer (black)
        r.setColor(0f, 0f, 0f, 0.8f);
        r.draw(title, titleX + 5, titleY - 5);

        // back layer (dark purple)
        r.setColor(0.25f, 0.05f, 0.4f, 1f);
        r.draw(title, titleX + 2, titleY - 2);

        // front layer (animated "neon gold")
        float rCol = 1.0f;
        float gCol = 0.85f + 0.15f * glow;
        float bCol = 0.45f + 0.25f * glow;
        r.setColor(rCol, gCol, bCol, 1f);
        r.draw(title, titleX, titleY);

        // little stars around the title for arcade vibe
        r.setColor(0.8f, 0.9f, 1f, 0.9f);
        r.draw("★", titleX - 40, titleY + 10);
        r.draw("★", titleX + (int) tb.getWidth() + 10, titleY + 25);

        // Subtitle
        String subtitle = "2-Player Arcade · Best of 5 Goals";
        Rectangle2D sb = r.getBounds(subtitle);
        int subX = centerX - (int) (sb.getWidth() / 2);
        int subY = titleY - 40;

        r.setColor(0f, 0f, 0f, 0.6f);
        r.draw(subtitle, subX + 3, subY - 3);
        r.setColor(0.7f, 0.95f, 1f, 1f);
        r.draw(subtitle, subX, subY);

        // ===== MENU ITEMS (fake 3D "buttons") =====
        int baseY = h / 2 + 50;
        int lineSpacing = 48;

        for (int i = 0; i < items.length; i++) {
            boolean isSelected = (i == selected);

            // vertical bounce for selected item
            int yOffset = isSelected ? (int) (Math.sin(t * 5.0) * 4.0) : 0;

            String text = items[i].label;
            Rectangle2D mb = r.getBounds(text);

            int itemX = centerX - (int) (mb.getWidth() / 2);
            int itemY = baseY - i * lineSpacing + yOffset;

            if (isSelected) {
                // background "button" suggestion using text shadows
                // left "pill"
                r.setColor(0f, 0f, 0f, 0.8f);
                r.draw("▶", itemX - 40, itemY - 3);

                // fake glow shadow
                r.setColor(0f, 0f, 0f, 0.8f);
                r.draw(text, itemX + 4, itemY - 4);

                // neon border effect: draw same text multiple times
                r.setColor(0.05f, 0.9f, 0.8f, 1f);
                r.draw(text, itemX - 1, itemY + 1);
                r.draw(text, itemX + 1, itemY - 1);

                float cPulse = 0.6f + 0.4f * (float) Math.sin(t * 6.0);
                r.setColor(0.2f * cPulse, 1.0f, 0.9f, 1f);
                r.draw(text, itemX, itemY);
            } else {
                r.setColor(0f, 0f, 0f, 0.6f);
                r.draw(text, itemX + 3, itemY - 3);

                r.setColor(0.8f, 0.85f, 0.95f, 0.9f);
                r.draw(text, itemX, itemY);
            }
        }

        // ===== Footer hint with subtle glow =====
        String footer = "↑ / ↓  move   ·   ENTER select   ·   ESC back / quit";
        Rectangle2D fb = r.getBounds(footer);
        int footerX = centerX - (int) (fb.getWidth() / 2);

        float glowFooter = 0.4f + 0.6f * (float) Math.abs(Math.sin(t * 2.0));

        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(footer, footerX + 2, 40);

        r.setColor(0.6f + 0.4f * glowFooter,
                0.8f + 0.2f * glowFooter,
                1.0f,
                1f);
        r.draw(footer, footerX, 44);
    }
}
