package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Shows controls / instructions for the game.
 */
public class InstructionsScreen {

    public void draw(TextRenderer r, int w, int h) {
        int x = 60;
        int y = h - 80;

        r.setColor(1f, 1f, 1f, 1f);
        r.draw("Instructions", w / 2 - 70, y);
        y -= 40;

        r.setColor(0.9f, 0.9f, 0.9f, 1f);
        r.draw("Goal: Hit the puck into your opponent's goal.", x, y); y -= 30;

        r.setColor(1f, 1f, 0.5f, 1f);
        r.draw("Controls", x, y); y -= 30;

        r.setColor(0.9f, 0.9f, 0.9f, 1f);
        r.draw("Left player  : W / S", x, y); y -= 25;
        r.draw("Right player : Up / Down arrows", x, y); y -= 25;
        r.draw("Pause / Play : P or SPACE", x, y); y -= 25;
        r.draw("Open menu    : ESC (also pauses game)", x, y); y -= 40;

        r.setColor(1f, 1f, 0.5f, 1f);
        r.draw("Menu", x, y); y -= 30;

        r.setColor(0.9f, 0.9f, 0.9f, 1f);
        r.draw("UP / DOWN    : Move selection", x, y); y -= 25;
        r.draw("ENTER        : Activate option", x, y); y -= 25;
        r.draw("ESC          : Quit or close menu", x, y); y -= 40;

        r.setColor(0.7f, 0.7f, 0.7f, 1f);
        r.draw("Press ENTER or ESC to return to the main menu.", x, 60);
    }
}
