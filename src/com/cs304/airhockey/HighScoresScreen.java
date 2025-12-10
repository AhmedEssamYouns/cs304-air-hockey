package com.cs304.airhockey;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * High scores screen for the Air Hockey game.
 */
public class HighScoresScreen {

    private static class Entry {
        String player;
        int score;

        Entry(String player, int score) {
            this.player = player;
            this.score = score;
        }
    }

    private final List<Entry> scores = new ArrayList<>();

    public void addScore(String player, int score) {
        scores.add(new Entry(player, score));
        scores.sort((a, b) -> Integer.compare(b.score, a.score));
        if (scores.size() > 5) {
            scores.remove(scores.size() - 1);
        }
    }

    public void draw(TextRenderer r, int w, int h) {
        r.setColor(1f, 1f, 1f, 1f);
        r.draw("High Scores", w / 2 - 70, h - 80);

        if (scores.isEmpty()) {
            r.setColor(0.7f, 0.7f, 0.7f, 1f);
            r.draw("No scores yet. Play a game!", w / 2 - 100, h / 2);
        } else {
            int startY = h / 2 + 40;
            int i = 0;
            for (Entry e : scores) {
                String line = (i + 1) + ". " + e.player + " - " + e.score;
                r.setColor(0.9f, 0.9f, 0.9f, 1f);
                r.draw(line, w / 2 - 100, startY - i * 30);
                i++;
            }
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Press ENTER or ESC to return to menu", 40, 40);
    }
}
