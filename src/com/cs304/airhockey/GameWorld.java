package com.cs304.airhockey;

import java.awt.event.KeyEvent;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;


public class GameWorld {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    // ----- World bounds -----
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
    private double paddleSpeed = 10; 

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    // ----- Puck -----
    private double puckX = 0;
    private double puckY = 0;
    private double puckR = 12;
    private double puckVX = 8; 
    private double puckVY = 6; 

    // ----- Game state -----
    private int leftScore = 0;
    private int rightScore = 0;
    private int winningScore = 5;
    private boolean paused = false;
    private boolean gameInProgress = false;

    private String leftPlayerName = "Left Player";
    private String rightPlayerName = "Right Player";

    private boolean matchFinished = false;

    private boolean vsAi = false;
    private Difficulty aiDifficulty = Difficulty.MEDIUM;
    private int playerScore = 0;
    private int playerLives = 3;
    private int level = 1;

    private boolean roundStarting = false;
    private int roundFramesTotal = 180;
    private int roundFramesRemaining = 0;
    private int nextServeDirection = 1;

    private double puckSpeedMultiplier = 1.0;

    private final HighScoresScreen highScores;

    public GameWorld(HighScoresScreen highScores) {
        this.highScores = highScores;
    }

    public void startNewMatch(String leftName, String rightName) {
        startNewMatch(leftName, rightName, false, Difficulty.MEDIUM);
    }

    public void startNewMatch(String leftName,
                              String rightName,
                              boolean vsAi,
                              Difficulty difficulty) {
        this.leftPlayerName = (leftName == null || leftName.trim().isEmpty()) ? "Left Player" : leftName.trim();
        this.rightPlayerName = (rightName == null || rightName.trim().isEmpty()) ? "Right Player" : rightName.trim();

        this.vsAi = vsAi;
        this.aiDifficulty = difficulty;

        leftScore = 0;
        rightScore = 0;
        paused = false;
        gameInProgress = true;
        matchFinished = false;

        leftPaddleY = 0;
        rightPaddleY = 0;

        wPressed = sPressed = upPressed = downPressed = false;
        puckSpeedMultiplier = 1.0;

        if (vsAi) {
            playerScore = 0;
            playerLives = 3;
            level = 1;
        }

        startRoundCountdown(Math.random() < 0.5 ? -1 : 1);
        SoundManager.getInstance().playGameMusicLoop();
    }

    public void endCurrentGame() {
        gameInProgress = false;
        paused = false;
        matchFinished = false;
        leftScore = rightScore = 0;
        playerScore = 0;
        playerLives = 3;
        level = 1;
        leftPaddleY = 0;
        rightPaddleY = 0;
        puckX = puckY = 0;
        puckVX = 8;
        puckVY = 6; 
        roundStarting = false;
        roundFramesRemaining = 0;
        puckSpeedMultiplier = 1.0;
        SoundManager.getInstance().stopGameMusic();
    }

    public boolean isGameInProgress() { return gameInProgress; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public void togglePause() { this.paused = !this.paused; }

    public void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_W) wPressed = true;
        else if (code == KeyEvent.VK_S) sPressed = true;
        else if (code == KeyEvent.VK_UP) upPressed = true;
        else if (code == KeyEvent.VK_DOWN) downPressed = true;
    }

    public void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W) wPressed = false;
        else if (code == KeyEvent.VK_S) sPressed = false;
        else if (code == KeyEvent.VK_UP) upPressed = false;
        else if (code == KeyEvent.VK_DOWN) downPressed = false;
    }

    public void update() {
        if (!gameInProgress || paused) return;
        updatePaddles();

        if (roundStarting) {
            if (roundFramesRemaining > 0) roundFramesRemaining--;
            if (roundFramesRemaining <= 0) launchPuck();
        } else {
            updatePuck();
        }
    }

    public void draw(GL2 gl, TextRenderer textRenderer, int windowWidth, int windowHeight) {
        drawRink(gl);
        drawPaddles(gl);
        drawPuck(gl);
        drawGameHUD(textRenderer, windowWidth, windowHeight);
    }

    public boolean consumeMatchFinished() {
        if (matchFinished) { matchFinished = false; return true; }
        return false;
    }

    // ==================== Internal logic ====================

    private void updatePaddles() {
        if (wPressed) leftPaddleY += paddleSpeed;
        if (sPressed) leftPaddleY -= paddleSpeed;

        if (vsAi) updateAiPaddle();
        else {
            if (upPressed) rightPaddleY += paddleSpeed;
            if (downPressed) rightPaddleY -= paddleSpeed;
        }

        leftPaddleY = clamp(leftPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
        rightPaddleY = clamp(rightPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
    }

    private void updateAiPaddle() {
        double targetY = puckY;
        double dy = targetY - rightPaddleY;

        double baseSpeed;
        switch (aiDifficulty) {
            case EASY: baseSpeed = 6; break;
            case MEDIUM: baseSpeed = 9; break;
            case HARD: baseSpeed = 12; break;
            default: baseSpeed = 9;
        }

        double aiSpeed = baseSpeed + (level - 1) * 1.0;

        if (Math.abs(dy) > 3) {
            double step = Math.min(Math.abs(dy), aiSpeed);
            double dir = Math.signum(dy);
            double noise = (Math.random() - 0.5) * 0.3 * 6;
            rightPaddleY += dir * step + noise;
        }
    }

    private void updatePuck() {
        puckX += puckVX;
        puckY += puckVY;

        if (puckY + puckR > WORLD_TOP) { puckY = WORLD_TOP - puckR; puckVY = -puckVY; }
        else if (puckY - puckR < WORLD_BOTTOM) { puckY = WORLD_BOTTOM + puckR; puckVY = -puckVY; }

        checkPaddleCollision();

        if (puckX - puckR < WORLD_LEFT) {
            if (vsAi) handleAiGoal(); else { rightScore++; if (!checkWinTwoPlayer()) startRoundCountdown(-1); }
        } else if (puckX + puckR > WORLD_RIGHT) {
            if (vsAi) handlePlayerGoal(); else { leftScore++; if (!checkWinTwoPlayer()) startRoundCountdown(1); }
        }
    }

    private void startRoundCountdown(int directionToRight) {
        roundStarting = true;
        nextServeDirection = directionToRight;
        roundFramesRemaining = roundFramesTotal;
        puckX = puckY = 0;
        puckVX = puckVY = 0;
    }

    private void launchPuck() {
        double randomY = (Math.random() - 0.5) * 6;
        double baseSpeed = 8.0 * puckSpeedMultiplier; 
        puckVX = baseSpeed * nextServeDirection;
        puckVY = randomY * puckSpeedMultiplier;
        roundStarting = false;
    }

    private void handlePlayerGoal() {
        leftScore++;
        playerScore += 100 * level;
        SoundManager.getInstance().playHit();
        if (leftScore >= winningScore) { level++; leftScore = rightScore = 0; puckSpeedMultiplier *= 1.05; }
        startRoundCountdown(-1);
    }

    private void handleAiGoal() {
        rightScore++;
        playerLives--;
        if (playerLives <= 0) {
            gameInProgress = false; paused = true; matchFinished = true;
            highScores.addScore(leftPlayerName, playerScore);
            SoundManager.getInstance().playGameOverThenResume(5000);
        } else startRoundCountdown(1);
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

        // left paddle
        if (puckX - puckR < lpRight && puckX + puckR > lpLeft && puckY + puckR > lpBottom && puckY - puckR < lpTop && puckVX < 0) {
            puckX = lpRight + puckR; puckVX = -puckVX; puckVY += (puckY - leftPaddleY) * 0.1;
            SoundManager.getInstance().playHit();
        }

        // right paddle
        if (puckX + puckR > rpLeft && puckX - puckR < rpRight && puckY + puckR > rpBottom && puckY - puckR < rpTop && puckVX > 0) {
            puckX = rpLeft - puckR; puckVX = -puckVX; puckVY += (puckY - rightPaddleY) * 0.1;
            SoundManager.getInstance().playHit();
        }
    }

    private boolean checkWinTwoPlayer() {
        if (vsAi) return false;
        if (leftScore >= winningScore || rightScore >= winningScore) {
            String winnerName = (leftScore > rightScore) ? leftPlayerName : rightPlayerName;
            int winnerScore = Math.max(leftScore, rightScore);
            highScores.addScore(winnerName, winnerScore);
            gameInProgress = false;
            paused = true;
            matchFinished = true;
            SoundManager.getInstance().stopGameMusic();
            return true;
        }
        return false;
    }

    // ==================== Drawing helpers ====================

    private void drawRink(GL2 gl) {
        float t = (float)((Math.sin(puckX * 0.01) + 1.0) * 0.5);
        float base = 0.05f;
        gl.glColor3f(base, base + 0.05f * t, 0.25f + 0.1f * t);
        fillRect(gl, -380, -240, 380, 240);
        gl.glColor3f(0.9f, 0.9f, 0.9f); gl.glLineWidth(3);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(WORLD_LEFT, WORLD_BOTTOM);
        gl.glVertex2d(WORLD_RIGHT, WORLD_BOTTOM);
        gl.glVertex2d(WORLD_RIGHT, WORLD_TOP);
        gl.glVertex2d(WORLD_LEFT, WORLD_TOP);
        gl.glEnd();
    }

    private void drawPaddles(GL2 gl) {
        gl.glColor3f(0.1f, 0.5f, 1.0f);
        fillRect(gl, leftPaddleX - paddleHalfW, leftPaddleY - paddleHalfH, leftPaddleX + paddleHalfW, leftPaddleY + paddleHalfH);
        gl.glColor3f(0.1f, 1.0f, 0.4f);
        fillRect(gl, rightPaddleX - paddleHalfW, rightPaddleY - paddleHalfH, rightPaddleX + paddleHalfW, rightPaddleY + paddleHalfH);
    }

    private void drawPuck(GL2 gl) {
        gl.glColor3f(1.0f, 0.9f, 0.2f);
        drawCircle(gl, puckX, puckY, puckR, 32);
    }

    private void drawGameHUD(TextRenderer textRenderer, int windowWidth, int windowHeight) {
        if (textRenderer == null) return;
        textRenderer.beginRendering(windowWidth, windowHeight);
        textRenderer.setColor(1f,1f,1f,1f);
        String topLine = vsAi ? leftPlayerName+" (You): "+leftScore+"   AI: "+rightScore+"   Score: "+playerScore+"   Lives: "+playerLives+"   Lv: "+level+" ["+aiDifficulty.name()+"]" :
                leftPlayerName+": "+leftScore+"   "+rightPlayerName+": "+rightScore;
        textRenderer.draw(topLine, 20, windowHeight-30);
        textRenderer.endRendering();
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private void fillRect(GL2 gl, double x1, double y1, double x2, double y2) {
        double left = Math.min(x1,x2), right=Math.max(x1,x2), bottom=Math.min(y1,y2), top=Math.max(y1,y2);
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex2d(left,bottom); gl.glVertex2d(right,bottom); gl.glVertex2d(right,top); gl.glVertex2d(left,top);
        gl.glEnd();
    }

    private void drawCircle(GL2 gl, double cx, double cy, double r, int segments) {
        gl.glBegin(GL2.GL_POLYGON);
        for(int i=0;i<segments;i++){
            double t = 2.0*Math.PI*i/segments;
            double x = cx + r*Math.cos(t);
            double y = cy + r*Math.sin(t);
            gl.glVertex2d(x,y);
        }
        gl.glEnd();
    }
}
