package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.geom.Rectangle2D;

/**
 * Screen for choosing AI difficulty: Easy / Medium / Hard.
 */
public class AiDifficultyScreen {

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

    public AiDifficultyScreen() {
        items = new Item[] {
                new Item("Easy - Chill Bot 😴",      "easy"),
                new Item("Medium - Smart Bot 😎",    "medium"),
                new Item("Hard - Tryhard Bot 😈",    "hard"),
                new Item("Back to Mode Select",      "back")
        };
    }

    public void reset() {
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
        if (items == null || items.length == 0) return "easy";
        return items[selected].action;
    }

    public void draw(TextRenderer r, int w, int h) {
        int centerX = w / 2;

        double t = System.nanoTime() / 1_000_000_000.0;
        float wobble = (float) Math.sin(t * 2.3) * 3f;

        // Title
        String title = "SELECT AI DIFFICULTY";
        Rectangle2D tb = r.getBounds(title);
        int titleX = centerX - (int) (tb.getWidth() / 2);
        int titleY = h - 140 + (int) wobble;

        // back shadow
        r.setColor(0f, 0f, 0f, 0.8f);
        r.draw(title, titleX + 4, titleY - 4);

        // main glowing text (slightly bluish)
        float pulse = 0.5f + 0.5f * (float) Math.sin(t * 3.2);
        r.setColor(0.5f, 0.85f + 0.15f * pulse, 1.0f, 1f);
        r.draw(title, titleX, titleY);

        String desc = "Higher difficulty = faster, smarter AI and more score per goal.";
        Rectangle2D db = r.getBounds(desc);
        int descX = centerX - (int) (db.getWidth() / 2);
        int descY = titleY - 40;
        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(desc, descX + 2, descY - 2);
        r.setColor(0.8f, 0.9f, 1f, 1f);
        r.draw(desc, descX, descY);

        int baseY = h / 2 + 40;
        int lineSpacing = 48;

        for (int i = 0; i < items.length; i++) {
            boolean isSelected = (i == selected);
            int y = baseY - i * lineSpacing;

            String text = items[i].label;
            Rectangle2D mb = r.getBounds(text);
            int itemX = centerX - (int) (mb.getWidth() / 2);

            if (isSelected) {
                int yOffset = (int) (Math.sin(t * 4.5) * 3.0);

                // shadow
                r.setColor(0f, 0f, 0f, 0.8f);
                r.draw(text, itemX + 4, y + yOffset - 4);

                // colored edge
                r.setColor(0.25f, 0.95f, 0.8f, 1f);
                r.draw(text, itemX - 1, y + yOffset + 1);
                r.draw(text, itemX + 1, y + yOffset - 1);

                r.setColor(0.3f, 1.0f, 0.9f, 1f);
                r.draw(text, itemX, y + yOffset);

                r.setColor(0.9f, 1f, 1f, 1f);
                r.draw("▶", itemX - 40, y + yOffset);
            } else {
                r.setColor(0f, 0f, 0f, 0.7f);
                r.draw(text, itemX + 3, y - 3);

                r.setColor(0.85f, 0.9f, 1f, 0.9f);
                r.draw(text, itemX, y);
            }
        }

        String footer = "↑ / ↓  move   ·   ENTER select   ·   ESC back";
        Rectangle2D fb = r.getBounds(footer);
        int footerX = centerX - (int) (fb.getWidth() / 2);

        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(footer, footerX + 2, 40);
        r.setColor(0.7f, 0.9f, 1f, 1f);
        r.draw(footer, footerX, 44);
    }
}
