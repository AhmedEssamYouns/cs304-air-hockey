package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.geom.Rectangle2D;

/**
 * Instructions / controls screen.
 * Color sections + subtle pulsing title.
 */
public class InstructionsScreen {

    public void draw(TextRenderer r, int w, int h) {
        int centerX = w / 2;
        int leftX = 80;

        double t = System.nanoTime() / 1_000_000_000.0;
        float titlePulse = 0.7f + 0.3f * (float) Math.sin(t * 2.0);

        // ===== Title =====
        String title = "How to Play";
        Rectangle2D tb = r.getBounds(title);
        int titleX = centerX - (int) (tb.getWidth() / 2);
        int titleY = h - 120 + (int) (Math.sin(t * 2.5) * 4);

        r.setColor(0f, 0f, 0f, 0.8f);
        r.draw(title, titleX + 5, titleY - 5);

        r.setColor(1.0f,
                0.8f + 0.2f * titlePulse,
                0.4f + 0.3f * titlePulse,
                1f);
        r.draw(title, titleX, titleY);

        int y = h - 180;

        // ===== Main Menu section =====
        r.setColor(0.4f, 1.0f, 0.9f, 1f);
        r.draw("Main Menu", leftX, y);
        y -= 30;

        r.setColor(0.95f, 0.97f, 1f, 1f);
        r.draw("↑ / ↓   Move selection", leftX, y);
        y -= 26;
        r.draw("ENTER   Select highlighted option", leftX, y);
        y -= 26;
        r.draw("ESC     Back / Quit game (from menu)", leftX, y);
        y -= 34;

        // ===== In Game =====
        r.setColor(1.0f, 0.75f, 0.35f, 1f);
        r.draw("In Game", leftX, y);
        y -= 30;

        r.setColor(0.96f, 0.96f, 1f, 1f);
        r.draw("Left player  :  W / S        (move paddle up / down)", leftX, y);
        y -= 26;
        r.draw("Right player :  ↑ / ↓       (move paddle up / down)", leftX, y);
        y -= 26;
        r.draw("P or SPACE   :  Pause / resume match", leftX, y);
        y -= 26;
        r.draw("ESC          :  Open main menu (match is paused)", leftX, y);
        y -= 34;

        // ===== Goal & tips =====
        r.setColor(0.85f, 0.65f, 1.0f, 1f);
        r.draw("Goal of the Game", leftX, y);
        y -= 30;

        r.setColor(0.97f, 0.97f, 1f, 1f);
        r.draw("• Hit the puck into your opponent's goal to score.", leftX, y);
        y -= 26;
        r.draw("• First player to reach 5 points wins the match.", leftX, y);
        y -= 26;
        r.draw("• The puck bounces off walls and paddles with angle.", leftX, y);
        y -= 26;
        r.draw("• Move while hitting the puck to curve your shots!", leftX, y);
        y -= 34;

        // bottom hint
        String hint = "Press ENTER or ESC to return to the main menu";
        Rectangle2D hb = r.getBounds(hint);
        int hintX = centerX - (int) (hb.getWidth() / 2);

        float hintGlow = 0.5f + 0.5f * (float) Math.abs(Math.sin(t * 2.0));

        r.setColor(0f, 0f, 0f, 0.7f);
        r.draw(hint, hintX + 3, 40);
        r.setColor(0.8f + 0.2f * hintGlow,
                0.9f + 0.1f * hintGlow,
                1.0f,
                1f);
        r.draw(hint, hintX, 44);
    }
}
