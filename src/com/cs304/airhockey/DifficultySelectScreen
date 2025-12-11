package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.geom.Rectangle2D;

/**
 * Screen for choosing AI difficulty: Easy / Medium / Hard.
 */
public class DifficultySelectScreen {

    private static class Item {
        String label;
        String action;
        Item(String label, String action) {
            this.label = label;
            this.action = action;
        }
    }

    private final Item[] items;
    private int selected = 0;

    public DifficultySelectScreen() {
        items = new Item[] {
                new Item("Easy",   "easy"),
                new Item("Medium", "medium"),
                new Item("Hard",   "hard"),
                new Item("Back",   "back")
        };
    }

    public void moveUp() {
        selected--;
        if (selected < 0) selected = items.length - 1;
    }

    public void moveDown() {
        selected++;
        if (selected >= items.length) selected = 0;
    }

    public String getSelectedAction() {
        return items[selected].action;
    }

    public void draw(TextRenderer r, int w, int h) {
        int centerX = w / 2;

        double t = System.nanoTime() / 1_000_000_000.0;
        float glow = 0.5f + 0.5f * (float) Math.sin(t * 2.8);
        float wobble = (float) Math.sin(t * 3.2) * 4f;

        // Title
        String title = "AI Difficulty";
        Rectangle2D tb = r.getBounds(title);
        int titleX = centerX - (int) (tb.getWidth() / 2);
        int titleY = h - 140 + (int) wobble;

        r.setColor(0f, 0f, 0f, 0.8f);
        r.draw(title, titleX + 4, titleY - 4);

        r.setColor(0.25f, 0.1f, 0.5f, 1f);
        r.draw(title, titleX + 1, titleY - 1);

        r.setColor(1.0f,
                0.85f + 0.15f * glow,
                0.45f + 0.25f * glow,
                1f);
        r.draw(title, titleX, titleY);

        // Subtitle
        String subtitle = "Choose how smart the AI is";
        Rectangle2D sb = r.getBounds(subtitle);
        int subX = centerX - (int) (sb.getWidth() / 2);
        int subY = titleY - 40;

        r.setColor(0f, 0f, 0f, 0.6f);
        r.draw(subtitle, subX + 3, subY - 3);
        r.setColor(0.7f, 0.95f, 1f, 1f);
        r.draw(subtitle, subX, subY);

        // Items
        int baseY = h / 2 + 40;
        int lineSpacing = 50;

        for (int i = 0; i < items.length; i++) {
            boolean isSelected = (i == selected);
            String text = items[i].label;

            Rectangle2D mb = r.getBounds(text);
            int itemX = centerX - (int) (mb.getWidth() / 2);
            int itemY = baseY - i * lineSpacing;

            if (isSelected) {
                int yOffset = (int) (Math.sin(t * 5.0) * 3.0);
                itemY += yOffset;

                r.setColor(0f, 0f, 0f, 0.8f);
                r.draw("▶", itemX - 40, itemY - 3);

                r.setColor(0f, 0f, 0f, 0.7f);
                r.draw(text, itemX + 3, itemY - 3);

                float pulse = 0.7f + 0.3f * (float) Math.sin(t * 7.0);
                if ("Easy".equals(text)) {
                    r.setColor(0.4f, 1.0f * pulse, 0.4f, 1f);
                } else if ("Medium".equals(text)) {
                    r.setColor(0.9f * pulse, 0.9f, 0.3f, 1f);
                } else if ("Hard".equals(text)) {
                    r.setColor(1.0f, 0.3f * pulse, 0.3f, 1f);
                } else {
                    r.setColor(0.8f, 0.85f, 0.95f, 1f);
                }
                r.draw(text, itemX, itemY);
            } else {
                r.setColor(0f, 0f, 0f, 0.5f);
                r.draw(text, itemX + 2, itemY - 2);

                r.setColor(0.8f, 0.85f, 0.95f, 0.9f);
                r.draw(text, itemX, itemY);
            }
        }

        // footer
        String footer = "↑ / ↓ move   ·   ENTER select   ·   ESC back";
        Rectangle2D fb = r.getBounds(footer);
        int footerX = centerX - (int) (fb.getWidth() / 2);

        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(footer, footerX + 2, 40);

        r.setColor(0.7f, 0.9f, 1.0f, 1f);
        r.draw(footer, footerX, 44);
    }
}
