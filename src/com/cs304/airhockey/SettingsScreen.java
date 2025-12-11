package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.geom.Rectangle2D;

/**
 * Settings screen: currently only sound on/off,
 * styled as a big glowing toggle.
 */
public class SettingsScreen {

    public void draw(TextRenderer r, int w, int h) {
        int centerX = w / 2;

        double t = System.nanoTime() / 1_000_000_000.0;
        float pulse = 0.6f + 0.4f * (float) Math.sin(t * 2.2);

        // ===== Title =====
        String title = "Settings";
        Rectangle2D tb = r.getBounds(title);
        int titleX = centerX - (int) (tb.getWidth() / 2);
        int titleY = h - 120;

        r.setColor(0f, 0f, 0f, 0.8f);
        r.draw(title, titleX + 5, titleY - 5);

        r.setColor(0.5f, 0.9f, 1.0f, 1f);
        r.draw(title, titleX, titleY);

        // ===== Sound toggle =====
        boolean soundOn = SoundManager.getInstance().isSoundEnabled();
        String label = "Sound";
        String status = soundOn ? "ON" : "OFF";

        int baseY = h / 2 + 10;

        // label
        Rectangle2D lb = r.getBounds(label);
        int labelX = centerX - 250;
        int labelY = baseY + 30;

        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(label, labelX + 3, labelY - 3);
        r.setColor(0.9f, 0.9f, 0.98f, 1f);
        r.draw(label, labelX, labelY);

        // status big text
        Rectangle2D sb = r.getBounds(status);
        int statusX = centerX + 40;
        int statusY = baseY + 35;

        if (soundOn) {
            r.setColor(0f, 0f, 0f, 0.7f);
            r.draw(status, statusX + 4, statusY - 4);
            r.setColor(0.2f, 1.0f * pulse, 0.4f + 0.2f * pulse, 1f);
            r.draw(status, statusX, statusY);
        } else {
            r.setColor(0f, 0f, 0f, 0.7f);
            r.draw(status, statusX + 4, statusY - 4);
            r.setColor(1.0f, 0.3f + 0.2f * (1 - pulse), 0.3f, 1f);
            r.draw(status, statusX, statusY);
        }

        // hint under toggle
        String hint = "Press ENTER / SPACE to toggle sound · ESC to go back";
        Rectangle2D hb = r.getBounds(hint);
        int hintX = centerX - (int) (hb.getWidth() / 2);
        int hintY = baseY - 20;

        r.setColor(0f, 0f, 0f, 0.6f);
        r.draw(hint, hintX + 2, hintY - 2);
        r.setColor(0.8f, 0.85f, 0.96f, 1f);
        r.draw(hint, hintX, hintY);

        // bottom tip with animated color
        String tip = "Tip: turn sound OFF if your laptop is in the lab 😅";
        Rectangle2D tb2 = r.getBounds(tip);
        int tipX = centerX - (int) (tb2.getWidth() / 2);

        float tipGlow = 0.4f + 0.6f * (float) Math.abs(Math.sin(t * 1.4));

        r.setColor(0f, 0f, 0f, 0.6f);
        r.draw(tip, tipX + 2, 50);
        r.setColor(0.6f + 0.4f * tipGlow,
                0.8f + 0.2f * tipGlow,
                1.0f,
                1f);
        r.draw(tip, tipX, 53);
    }
}
