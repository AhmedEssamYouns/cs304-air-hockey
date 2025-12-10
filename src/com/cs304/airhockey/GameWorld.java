package com.cs304.airhockey;

import java.awt.event.KeyEvent;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Handles all in-game logic (puck, paddles, scores) and drawing.
 */
public class GameWorld {

    // ----- World bounds (rink) -----
    private final double WORLD_LEFT = -360;
    private final double WORLD_RIGHT = 360;
    private final double WORLD_BOTTOM = -220;
    private final double WORLD_TOP = 220;

    // ----- Paddles -----
    private double paddleHalfW = 10;
    private double paddleHalfH = 60;

    private double leftPaddleX = -320;
    private double leftPaddleY = 0;

    private double rightPaddleX = 320;
    private double rightPaddleY = 0;

    private double paddleSpeed = 6;

    // key state for smooth controls
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    // ----- Puck -----
    private double puckX = 0;
    private double puckY = 0;
    private double puckR = 12;

    private double puckVX = 6;
    private double puckVY = 4;

    // ----- Game state -----
    private int leftScore = 0;
    private int rightScore = 0;
    private int winningScore = 5;
    private boolean paused = false;
    private boolean gameInProgress = false;

    private String leftPlayerName = "Left Player";
    private String rightPlayerName = "Right Player";

    private boolean matchFinished = false;

    private final HighScoresScreen highScores;

    public GameWorld(HighScoresScreen highScores) {
        this.highScores = highScores;
    }

    // ==================== Public API ====================

    public void startNewMatch(String leftName, String rightName) {
        this.leftPlayerName = (leftName == null || leftName.trim().isEmpty())
                ? "Left Player"
                : leftName.trim();

        this.rightPlayerName = (rightName == null || rightName.trim().isEmpty())
                ? "Right Player"
                : rightName.trim();

        leftScore = 0;
        rightScore = 0;
        paused = false;
        gameInProgress = true;
        matchFinished = false;

        leftPaddleY = 0;
        rightPaddleY = 0;

        wPressed = sPressed = upPressed = downPressed = false;

        resetPuck(Math.random() < 0.5 ? -1 : 1);

        // 🔊 start music when game starts
        SoundManager.getInstance().playGameMusicLoop();
    }

    public void endCurrentGame() {
        gameInProgress = false;
        paused = false;
        matchFinished = false;

        leftScore = 0;
        rightScore = 0;

        leftPaddleY = 0;
        rightPaddleY = 0;

        puckX = 0;
        puckY = 0;
        puckVX = 6;
        puckVY = 4;

        // stop music
        SoundManager.getInstance().stopGameMusic();
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    public void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_W) {
            wPressed = true;
        } else if (code == KeyEvent.VK_S) {
            sPressed = true;
        } else if (code == KeyEvent.VK_UP) {
            upPressed = true;
        } else if (code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
    }

    public void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W) {
            wPressed = false;
        } else if (code == KeyEvent.VK_S) {
            sPressed = false;
        } else if (code == KeyEvent.VK_UP) {
            upPressed = false;
        } else if (code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
    }

    public void update() {
        if (!gameInProgress || paused) return;
        updatePaddles();
        updatePuck();
    }

    public void draw(GL2 gl, TextRenderer textRenderer, int windowWidth, int windowHeight) {
        drawRink(gl);
        drawPaddles(gl);
        drawPuck(gl);
        drawGameHUD(textRenderer, windowWidth, windowHeight);
    }

    public boolean consumeMatchFinished() {
        if (matchFinished) {
            matchFinished = false;
            return true;
        }
        return false;
    }

    // ==================== Internal logic ====================

    private void updatePaddles() {
        if (wPressed) leftPaddleY += paddleSpeed;
        if (sPressed) leftPaddleY -= paddleSpeed;
        if (upPressed) rightPaddleY += paddleSpeed;
        if (downPressed) rightPaddleY -= paddleSpeed;

        leftPaddleY = clamp(leftPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
        rightPaddleY = clamp(rightPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
    }

    private void updatePuck() {
        puckX += puckVX;
        puckY += puckVY;

        if (puckY + puckR > WORLD_TOP) {
            puckY = WORLD_TOP - puckR;
            puckVY = -puckVY;
        } else if (puckY - puckR < WORLD_BOTTOM) {
            puckY = WORLD_BOTTOM + puckR;
            puckVY = -puckVY;
        }

        checkPaddleCollision();

        if (puckX - puckR < WORLD_LEFT) {
            rightScore++;
            resetPuck(-1);
            checkWin();
        } else if (puckX + puckR > WORLD_RIGHT) {
            leftScore++;
            resetPuck(1);
            checkWin();
        }
    }

    private void resetPuck(int directionToRight) {
        puckX = 0;
        puckY = 0;
        double randomY = (Math.random() - 0.5) * 6;
        puckVX = 6 * directionToRight;
        puckVY = randomY;
    }

    private void checkPaddleCollision() {
        double lpLeft = leftPaddleX - paddleHalfW;
        double lpRight = leftPaddleX + paddleHalfW;
        double lpTop = leftPaddleY + paddleHalfH;
        double lpBottom = leftPaddleY - paddleHalfH;

        double rpLeft = rightPaddleX - paddleHalfW;
        double rpRight = rightPaddleX + paddleHalfW;
        double rpTop = rightPaddleY + paddleHalfH;
        double rpBottom = rightPaddleY - paddleHalfH;

        if (puckX - puckR < lpRight && puckX + puckR > lpLeft &&
                puckY + puckR > lpBottom && puckY - puckR < lpTop &&
                puckVX < 0) {

            puckX = lpRight + puckR;
            puckVX = -puckVX;

            double offset = puckY - leftPaddleY;
            puckVY += offset * 0.1;
        }

        if (puckX + puckR > rpLeft && puckX - puckR < rpRight &&
                puckY + puckR > rpBottom && puckY - puckR < rpTop &&
                puckVX > 0) {

            puckX = rpLeft - puckR;
            puckVX = -puckVX;

            double offset = puckY - rightPaddleY;
            puckVY += offset * 0.1;
        }
    }

    private void checkWin() {
        if (leftScore >= winningScore || rightScore >= winningScore) {
            String winnerName = (leftScore > rightScore) ? leftPlayerName : rightPlayerName;
            int winnerScore = Math.max(leftScore, rightScore);

            highScores.addScore(winnerName, winnerScore);

            gameInProgress = false;
            paused = true;
            matchFinished = true;

            // stop music when match ends
            SoundManager.getInstance().stopGameMusic();
        }
    }

    // ==================== Drawing helpers ====================

    private void drawRink(GL2 gl) {
        float t = (float) ((Math.sin(puckX * 0.01) + 1.0) * 0.5);
        float base = 0.05f;

        gl.glColor3f(base, base + 0.05f * t, 0.25f + 0.1f * t);
        fillRect(gl, -380, -240, 380, 240);

        gl.glColor3f(0.9f, 0.9f, 0.9f);
        gl.glLineWidth(3);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(WORLD_LEFT, WORLD_BOTTOM);
        gl.glVertex2d(WORLD_RIGHT, WORLD_BOTTOM);
        gl.glVertex2d(WORLD_RIGHT, WORLD_TOP);
        gl.glVertex2d(WORLD_LEFT, WORLD_TOP);
        gl.glEnd();

        gl.glColor3f(0.8f, 0.2f, 0.2f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(0, WORLD_BOTTOM);
        gl.glVertex2d(0, WORLD_TOP);
        gl.glEnd();

        gl.glColor3f(0.8f, 0.2f, 0.2f);
        drawCircleOutline(gl, 0, 0, 60, 48);
    }

    private void drawPaddles(GL2 gl) {
        gl.glColor3f(0.1f, 0.5f, 1.0f);
        fillRect(gl,
                leftPaddleX - paddleHalfW,
                leftPaddleY - paddleHalfH,
                leftPaddleX + paddleHalfW,
                leftPaddleY + paddleHalfH);

        gl.glColor3f(0.1f, 1.0f, 0.4f);
        fillRect(gl,
                rightPaddleX - paddleHalfW,
                rightPaddleY - paddleHalfH,
                rightPaddleX + paddleHalfW,
                rightPaddleY + paddleHalfH);
    }

    private void drawPuck(GL2 gl) {
        gl.glColor3f(1.0f, 0.9f, 0.2f);
        drawCircle(gl, puckX, puckY, puckR, 32);
    }

    private void drawGameHUD(TextRenderer textRenderer, int windowWidth, int windowHeight) {
        if (textRenderer == null) return;

        textRenderer.beginRendering(windowWidth, windowHeight);
        textRenderer.setColor(1f, 1f, 1f, 1f);

        String scoreText = leftPlayerName + ": " + leftScore +
                "   " + rightPlayerName + ": " + rightScore;
        textRenderer.draw(scoreText, 20, windowHeight - 30);

        String pauseText = "W/S: " + leftPlayerName +
                "  |  Up/Down: " + rightPlayerName +
                "  |  P: Pause  |  ESC: Menu";
        textRenderer.draw(pauseText, 20, 20);

        if (paused) {
            textRenderer.setColor(1f, 1f, 0f, 1f);
            textRenderer.draw("PAUSED", windowWidth / 2 - 50, windowHeight / 2);
        }

        textRenderer.endRendering();
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private void fillRect(GL2 gl, double x1, double y1, double x2, double y2) {
        double left = Math.min(x1, x2);
        double right = Math.max(x1, x2);
        double bottom = Math.min(y1, y2);
        double top = Math.max(y1, y2);
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex2d(left, bottom);
        gl.glVertex2d(right, bottom);
        gl.glVertex2d(right, top);
        gl.glVertex2d(left, top);
        gl.glEnd();
    }

    private void drawCircle(GL2 gl, double cx, double cy, double r, int segments) {
        gl.glBegin(GL2.GL_POLYGON);
        for (int i = 0; i < segments; i++) {
            double t = 2.0 * Math.PI * i / segments;
            double x = cx + r * Math.cos(t);
            double y = cy + r * Math.sin(t);
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }

    private void drawCircleOutline(GL2 gl, double cx, double cy, double r, int segments) {
        gl.glBegin(GL2.GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) {
            double t = 2.0 * Math.PI * i / segments;
            double x = cx + r * Math.cos(t);
            double y = cy + r * Math.sin(t);
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }
}
