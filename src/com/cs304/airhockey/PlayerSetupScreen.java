package com.cs304.airhockey;

import java.awt.event.KeyEvent;

import com.jogamp.opengl.util.awt.TextRenderer;


public class PlayerSetupScreen {

    public enum Mode {
        TWO_PLAYERS,
        FOUR_PLAYERS_2V2,
        FOUR_PLAYERS_FFA
    }

    public interface Listener {
        void onCancel();

        void onTwoPlayerNamesConfirmed(String leftName, String rightName);

        void onFourPlayers2v2NamesConfirmed(String left1, String left2,
                                            String right1, String right2);

        void onFourPlayersFfaNamesConfirmed(String left, String right,
                                            String top, String bottom);
    }

    private final Listener listener;

    private Mode mode = Mode.TWO_PLAYERS;
    private String[] names = new String[]{"", "", "", ""};
    private int playerCount = 2;
    private int editingIndex = 0;

    public PlayerSetupScreen(Listener listener) {
        this.listener = listener;
    }

    public void configureTwoPlayers() {
        mode = Mode.TWO_PLAYERS;
        playerCount = 2;
        reset();
    }

    public void configureFourPlayers2v2() {
        mode = Mode.FOUR_PLAYERS_2V2;
        playerCount = 4;
        reset();
    }

    public void configureFourPlayersFfa() {
        mode = Mode.FOUR_PLAYERS_FFA;
        playerCount = 4;
        reset();
    }

    public void reset() {
        for (int i = 0; i < names.length; i++) {
            names[i] = "";
        }
        editingIndex = 0;
    }

    public void draw(TextRenderer r, int w, int h) {
        String title;
        switch (mode) {
            case FOUR_PLAYERS_2V2:
                title = "4 Players – 2 vs 2";
                break;
            case FOUR_PLAYERS_FFA:
                title = "4 Players – Free-for-all";
                break;
            default:
                title = "Player Setup";
        }

        r.setColor(1f, 1f, 1f, 1f);
        r.draw(title, w / 2 - 120, h - 80);

        int baseY = h / 2 + 40;
        int lineSpacing = 40;

        for (int i = 0; i < playerCount; i++) {
            boolean selected = (i == editingIndex);

            if (selected) {
                r.setColor(1f, 1f, 0f, 1f);
            } else {
                r.setColor(0.8f, 0.8f, 0.8f, 1f);
            }

            String label = getLabelForIndex(i);
            String value = names[i].isEmpty() ? "_" : names[i];
            String full = label + value;

            r.draw(full, 80, baseY - i * lineSpacing);
        }

        r.setColor(0.6f, 0.6f, 0.6f, 1f);
        r.draw("Type name, ENTER to confirm each, ESC to cancel", 80, 80);
    }

    private String getLabelForIndex(int idx) {
        switch (mode) {
            case TWO_PLAYERS:
                if (idx == 0) return "Left player name: ";
                else return "Right player name: ";
            case FOUR_PLAYERS_2V2:
                if (idx == 0) return "Left team - Player 1: ";
                if (idx == 1) return "Left team - Player 2: ";
                if (idx == 2) return "Right team - Player 1: ";
                return "Right team - Player 2: ";
            case FOUR_PLAYERS_FFA:
                if (idx == 0) return "Left side player: ";
                if (idx == 1) return "Right side player: ";
                if (idx == 2) return "Top player: ";
                return "Bottom player: ";
            default:
                return "Player " + (idx + 1) + ": ";
        }
    }

    private String getFallbackForIndex(int idx) {
        switch (mode) {
            case TWO_PLAYERS:
                return (idx == 0) ? "Left Player" : "Right Player";
            case FOUR_PLAYERS_2V2:
                if (idx == 0) return "Left P1";
                if (idx == 1) return "Left P2";
                if (idx == 2) return "Right P1";
                return "Right P2";
            case FOUR_PLAYERS_FFA:
                if (idx == 0) return "Left Player";
                if (idx == 1) return "Right Player";
                if (idx == 2) return "Top Player";
                return "Bottom Player";
            default:
                return "Player " + (idx + 1);
        }
    }

    public void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            listener.onCancel();
            return;
        }

        if (code == KeyEvent.VK_BACK_SPACE) {
            if (!names[editingIndex].isEmpty()) {
                names[editingIndex] =
                        names[editingIndex].substring(0, names[editingIndex].length() - 1);
            }
        } else if (code == KeyEvent.VK_ENTER) {
            if (editingIndex < playerCount - 1) {
                editingIndex++;
            } else {
                // finalize
                String[] finalNames = new String[playerCount];
                for (int i = 0; i < playerCount; i++) {
                    finalNames[i] = names[i].isEmpty()
                            ? getFallbackForIndex(i)
                            : names[i];
                }

                switch (mode) {
                    case TWO_PLAYERS:
                        listener.onTwoPlayerNamesConfirmed(finalNames[0], finalNames[1]);
                        break;
                    case FOUR_PLAYERS_2V2:
                        listener.onFourPlayers2v2NamesConfirmed(
                                finalNames[0], finalNames[1],
                                finalNames[2], finalNames[3]
                        );
                        break;
                    case FOUR_PLAYERS_FFA:
                        listener.onFourPlayersFfaNamesConfirmed(
                                finalNames[0], finalNames[1],
                                finalNames[2], finalNames[3]
                        );
                        break;
                }
            }
        }
    }

    public void handleKeyTyped(char c) {
        if (!Character.isLetterOrDigit(c) && c != ' ') {
            return;
        }

        if (names[editingIndex].length() < 12) {
            names[editingIndex] += c;
        }
    }
}
