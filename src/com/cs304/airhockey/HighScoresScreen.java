package com.cs304.airhockey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Simple high scores screen.
 */
public class HighScoresScreen {

    private static class Entry {
        String name;
        int score;

        Entry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private final List<Entry> scores = new ArrayList<>();

    public void addScore(String name, int score) {
        scores.add(new Entry(name, score));
        // sort descending by score
        Collections.sort(scores, new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                return Integer.compare(b.score, a.score);
            }
        });

        // keep only top 10
        while (scores.size() > 10) {
            scores.remove(scores.size() - 1);
        }
    }

    public void draw(TextRenderer r, int w, int h) {
        r.setColor(1f, 1f, 1f, 1f);
        r.draw("High Scores", w / 2 - 60, h - 80);

        if (scores.isEmpty()) {
            r.setColor(0.7f, 0.7f, 0.7f, 1f);
            r.draw("No games played yet.", w / 2 - 80, h / 2);
        } else {
            int y = h - 130;
            int rank = 1;
            for (Entry e : scores) {
                r.setColor(0.9f, 0.9f, 0.9f, 1f);
                String line = rank + ". " + e.name + "  -  " + e.score;
                r.draw(line, w / 2 - 100, y);
                y -= 30;
                rank++;
                if (rank > 10) break;
            }
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Press ENTER or ESC to go back", 40, 40);
    }
}
