package com.cs304.airhockey;

import java.awt.event.KeyEvent;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Screen to let both players enter their names before a match.
 */
public class PlayerSetupScreen {

    public interface Listener {
        void onCancel();
        void onNamesConfirmed(String leftName, String rightName);
    }

    private final Listener listener;

    private String leftNameInput = "";
    private String rightNameInput = "";
    private boolean editingLeftName = true;

    public PlayerSetupScreen(Listener listener) {
        this.listener = listener;
    }

    public void reset() {
        leftNameInput = "";
        rightNameInput = "";
        editingLeftName = true;
    }

    public void draw(TextRenderer r, int w, int h) {
        r.setColor(1f, 1f, 1f, 1f);
        r.draw("Player Setup", w / 2 - 80, h - 80);

        // Left player
        if (editingLeftName) {
            r.setColor(1f, 1f, 0f, 1f);
        } else {
            r.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        String leftText = "Left player name: " +
                (leftNameInput.isEmpty() ? "_" : leftNameInput);
        r.draw(leftText, 80, h / 2 + 20);

        // Right player
        if (!editingLeftName) {
            r.setColor(1f, 1f, 0f, 1f);
        } else {
            r.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        String rightText = "Right player name: " +
                (rightNameInput.isEmpty() ? "_" : rightNameInput);
        r.draw(rightText, 80, h / 2 - 20);

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Type name, ENTER to confirm each, ESC to cancel", 80, 80);
    }

    public void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            listener.onCancel();
            return;
        }

        if (code == KeyEvent.VK_BACK_SPACE) {
            if (editingLeftName && !leftNameInput.isEmpty()) {
                leftNameInput = leftNameInput.substring(0, leftNameInput.length() - 1);
            } else if (!editingLeftName && !rightNameInput.isEmpty()) {
                rightNameInput = rightNameInput.substring(0, rightNameInput.length() - 1);
            }
        } else if (code == KeyEvent.VK_ENTER) {
            if (editingLeftName) {
                editingLeftName = false;
            } else {
                String left = leftNameInput.isEmpty() ? "Left Player" : leftNameInput;
                String right = rightNameInput.isEmpty() ? "Right Player" : rightNameInput;
                listener.onNamesConfirmed(left, right);
            }
        }
    }

    public void handleKeyTyped(char c) {
        if (!Character.isLetterOrDigit(c) && c != ' ') {
            return;
        }

        if (editingLeftName) {
            if (leftNameInput.length() < 12) {
                leftNameInput += c;
            }
        } else {
            if (rightNameInput.length() < 12) {
                rightNameInput += c;
            }
        }
    }
}
