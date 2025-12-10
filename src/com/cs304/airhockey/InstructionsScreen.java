package com.cs304.airhockey;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Instructions / help screen.
 */
public class InstructionsScreen {

    public void draw(TextRenderer r, int w, int h) {
        int x = 60;
        int y = h - 80;

        r.setColor(1f, 1f, 1f, 1f);
        r.draw("How to Play Air Hockey", x, y);

        y -= 40;
        r.setColor(0.8f, 0.8f, 0.8f, 1f);
        r.draw("Goal:", x, y);
        y -= 25;
        r.draw("- Score 5 points to win the match.", x + 20, y);
        y -= 35;

        r.draw("Controls:", x, y);
        y -= 25;
        r.draw("- Left player  : W = up,  S = down", x + 20, y);
        y -= 20;
        r.draw("- Right player : UP arrow = up,  DOWN arrow = down", x + 20, y);
        y -= 20;
        r.draw("- P or SPACE   : Pause / resume game", x + 20, y);
        y -= 20;
        r.draw("- ESC          : Open / close main menu", x + 20, y);
        y -= 35;

        r.draw("Menu:", x, y);
        y -= 25;
        r.draw("- Start Game / Continue : play or resume a match.", x + 20, y);
        y -= 20;
        r.draw("- End Game              : cancel the current match.", x + 20, y);
        y -= 20;
        r.draw("- High Scores           : shows best winners.", x + 20, y);
        y -= 20;
        r.draw("- Quit                  : exit the game.", x + 20, y);

        y -= 40;
        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Press ENTER or ESC to return to the main menu.", x, y);
    }
}
