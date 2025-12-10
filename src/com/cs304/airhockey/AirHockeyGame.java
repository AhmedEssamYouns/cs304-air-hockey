package com.cs304.airhockey;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * CS304 Air Hockey final project.
 * <p>
 * Screens:
 * - Main menu
 * - Player setup (enter names)
 * - Game
 * - High scores
 * - Instructions
 * <p>
 * Controls:
 * - Main menu: UP / DOWN, ENTER, ESC
 * - Player setup: type name, BACKSPACE, ENTER, ESC
 * - Game:
 * Left player  : W / S
 * Right player : UP / DOWN
 * Pause / Play : P or SPACE
 * Open menu    : ESC (pauses game)
 */
public class AirHockeyGame extends JFrame implements GLEventListener, KeyListener {

    private GLCanvas canvas;
    private FPSAnimator animator;
    private TextRenderer textRenderer;

    // ----- Screens -----
    private enum Screen {
        MAIN_MENU,
        PLAYER_SETUP,
        GAME,
        HIGH_SCORES,
        INSTRUCTIONS
    }

    private Screen currentScreen = Screen.MAIN_MENU;

    private final MainMenuScreen mainMenu = new MainMenuScreen();
    private final HighScoresScreen highScores = new HighScoresScreen();
    private final InstructionsScreen instructions = new InstructionsScreen();

    // Is there a running match that can be continued?
    private boolean gameInProgress = false;

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

    // smaller speed + per-frame update  => smoother movement
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

    // ----- Player names -----
    private String leftPlayerName = "Left Player";
    private String rightPlayerName = "Right Player";

    // temp input while typing names
    private String leftNameInput = "";
    private String rightNameInput = "";
    private boolean editingLeftName = true;

    // Window size (for TextRenderer)
    private int windowWidth = 800;
    private int windowHeight = 520;

    public static void main(String[] args) {
        new AirHockeyGame();
    }

    public AirHockeyGame() {
        super("CS304 - Air Hockey");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        GLProfile profile = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(profile);

        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.setFocusable(true);

        add(canvas, BorderLayout.CENTER);
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setVisible(true);

        animator = new FPSAnimator(canvas, 60, true);
        animator.start();

        // open menu in "no game yet" mode
        mainMenu.open(false);

        canvas.requestFocusInWindow();
    }

    // ==================== GLEventListener ====================

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1f);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-380, 380, -240, 240, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 18), true, true);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        switch (currentScreen) {
            case MAIN_MENU:
                drawMenu(gl);
                break;
            case PLAYER_SETUP:
                drawPlayerSetup(gl);
                break;
            case GAME:
                updateGame();
                drawGame(gl);
                break;
            case HIGH_SCORES:
                drawHighScores(gl);
                break;
            case INSTRUCTIONS:
                drawInstructions(gl);
                break;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        windowWidth = w;
        windowHeight = h;

        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-380, 380, -240, 240, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (animator != null && animator.isStarted()) {
            animator.stop();
        }
    }

    // ==================== GAME LOGIC ====================

    private void startNewMatch() {
        leftScore = 0;
        rightScore = 0;
        paused = false;

        // center paddles
        leftPaddleY = 0;
        rightPaddleY = 0;

        // reset key state
        wPressed = sPressed = upPressed = downPressed = false;

        resetPuck(Math.random() < 0.5 ? -1 : 1);

        gameInProgress = true;
        currentScreen = Screen.GAME;
    }

    private void endCurrentGame() {
        gameInProgress = false;
        paused = false;

        leftScore = 0;
        rightScore = 0;

        leftPaddleY = 0;
        rightPaddleY = 0;

        puckX = 0;
        puckY = 0;
        puckVX = 6;
        puckVY = 4;
    }

    private void updateGame() {
        if (!paused) {
            updatePaddles();
            updatePuck();
        }
    }

    // per-frame paddle movement using key state (smooth)
    private void updatePaddles() {
        if (wPressed) {
            leftPaddleY += paddleSpeed;
        }
        if (sPressed) {
            leftPaddleY -= paddleSpeed;
        }
        if (upPressed) {
            rightPaddleY += paddleSpeed;
        }
        if (downPressed) {
            rightPaddleY -= paddleSpeed;
        }

        leftPaddleY = clamp(leftPaddleY,
                WORLD_BOTTOM + paddleHalfH,
                WORLD_TOP - paddleHalfH);
        rightPaddleY = clamp(rightPaddleY,
                WORLD_BOTTOM + paddleHalfH,
                WORLD_TOP - paddleHalfH);
    }

    private void updatePuck() {
        puckX += puckVX;
        puckY += puckVY;

        // top / bottom wall collisions
        if (puckY + puckR > WORLD_TOP) {
            puckY = WORLD_TOP - puckR;
            puckVY = -puckVY;
        } else if (puckY - puckR < WORLD_BOTTOM) {
            puckY = WORLD_BOTTOM + puckR;
            puckVY = -puckVY;
        }

        // paddle collisions
        checkPaddleCollision();

        // goals
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

        // left paddle
        if (puckX - puckR < lpRight && puckX + puckR > lpLeft &&
                puckY + puckR > lpBottom && puckY - puckR < lpTop &&
                puckVX < 0) {

            puckX = lpRight + puckR;
            puckVX = -puckVX;

            double offset = puckY - leftPaddleY;
            puckVY += offset * 0.1;
        }

        // right paddle
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
            currentScreen = Screen.HIGH_SCORES;
        }
    }

    // ==================== DRAWING ====================

    private void drawGame(GL2 gl) {
        drawRink(gl);
        drawPaddles(gl);
        drawPuck(gl);
        drawGameHUD();
    }

    private void drawRink(GL2 gl) {
        // light animated background: small color pulse based on puckX
        float t = (float) ((Math.sin(puckX * 0.01) + 1.0) * 0.5); // 0..1
        float base = 0.05f;
        background(gl, base, base + 0.05f * t, 0.25f + 0.1f * t);

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

    private void drawGameHUD() {
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

    private void drawMenu(GL2 gl) {
        background(gl, 0.0f, 0.0f, 0.0f);

        if (textRenderer == null) return;
        textRenderer.beginRendering(windowWidth, windowHeight);

        mainMenu.draw(textRenderer, windowWidth, windowHeight);

        textRenderer.endRendering();
    }

    private void drawHighScores(GL2 gl) {
        background(gl, 0.0f, 0.0f, 0.0f);

        if (textRenderer == null) return;
        textRenderer.beginRendering(windowWidth, windowHeight);

        highScores.draw(textRenderer, windowWidth, windowHeight);

        textRenderer.endRendering();
    }

    private void drawInstructions(GL2 gl) {
        background(gl, 0f, 0f, 0f);

        if (textRenderer == null) return;
        textRenderer.beginRendering(windowWidth, windowHeight);

        instructions.draw(textRenderer, windowWidth, windowHeight);

        textRenderer.endRendering();
    }

    private void drawPlayerSetup(GL2 gl) {
        background(gl, 0f, 0f, 0f);

        if (textRenderer == null) return;

        textRenderer.beginRendering(windowWidth, windowHeight);

        textRenderer.setColor(1f, 1f, 1f, 1f);
        textRenderer.draw("Player Setup", windowWidth / 2 - 80, windowHeight - 80);

        // Left player
        if (editingLeftName) {
            textRenderer.setColor(1f, 1f, 0f, 1f);
        } else {
            textRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        String leftText = "Left player name: " +
                (leftNameInput.isEmpty() ? "_" : leftNameInput);
        textRenderer.draw(leftText, 80, windowHeight / 2 + 20);

        // Right player
        if (!editingLeftName) {
            textRenderer.setColor(1f, 1f, 0f, 1f);
        } else {
            textRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        String rightText = "Right player name: " +
                (rightNameInput.isEmpty() ? "_" : rightNameInput);
        textRenderer.draw(rightText, 80, windowHeight / 2 - 20);

        textRenderer.setColor(0.6f, 0.6f, 0.6f, 1f);
        textRenderer.draw("Type name, ENTER to confirm each, ESC to cancel",
                80, 80);

        textRenderer.endRendering();
    }

    // ==================== KeyListener ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (currentScreen == Screen.MAIN_MENU) {
            handleMenuKeys(code);
            return;
        }

        if (currentScreen == Screen.PLAYER_SETUP) {
            handlePlayerSetupKeys(code);
            return;
        }

        if (currentScreen == Screen.GAME) {
            handleGameKeyPressed(code);
            return;
        }

        if (currentScreen == Screen.HIGH_SCORES) {
            handleHighScoreKeys(code);
            return;
        }

        if (currentScreen == Screen.INSTRUCTIONS) {
            handleInstructionsKeys(code);
        }
    }

    private void handleMenuKeys(int code) {
        if (code == KeyEvent.VK_UP) {
            mainMenu.moveUp();
        } else if (code == KeyEvent.VK_DOWN) {
            mainMenu.moveDown();
        } else if (code == KeyEvent.VK_ENTER) {
            String action = mainMenu.getSelectedAction();
            switch (action) {
                case "start":
                    gameInProgress = false; // new match
                    leftNameInput = "";
                    rightNameInput = "";
                    editingLeftName = true;
                    currentScreen = Screen.PLAYER_SETUP;
                    break;

                case "continue":
                    if (gameInProgress) {
                        paused = false;
                        currentScreen = Screen.GAME;
                    }
                    break;

                case "endgame":
                    endCurrentGame();
                    mainMenu.open(false);
                    currentScreen = Screen.MAIN_MENU;
                    break;

                case "instructions":
                    currentScreen = Screen.INSTRUCTIONS;
                    break;

                case "highscores":
                    currentScreen = Screen.HIGH_SCORES;
                    break;

                case "quit":
                    System.exit(0);
                    break;
            }
        } else if (code == KeyEvent.VK_ESCAPE) {
            // When a game is in progress, ESC from menu just closes menu
            if (gameInProgress) {
                paused = false;
                currentScreen = Screen.GAME;
            } else {
                System.exit(0);
            }
        }
    }

    private void handlePlayerSetupKeys(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameInProgress);
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
                leftPlayerName = leftNameInput.isEmpty() ? "Left Player" : leftNameInput;
                rightPlayerName = rightNameInput.isEmpty() ? "Right Player" : rightNameInput;
                startNewMatch();
            }
        }
    }

    private void handleGameKeyPressed(int code) {
        if (code == KeyEvent.VK_W) {
            wPressed = true;
        } else if (code == KeyEvent.VK_S) {
            sPressed = true;
        } else if (code == KeyEvent.VK_UP) {
            upPressed = true;
        } else if (code == KeyEvent.VK_DOWN) {
            downPressed = true;
        } else if (code == KeyEvent.VK_P || code == KeyEvent.VK_SPACE) {
            paused = !paused;
        } else if (code == KeyEvent.VK_ESCAPE) {
            // open main menu, pause game
            paused = true;
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameInProgress);
        }
    }

    private void handleGameKeyReleased(int code) {
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

    private void handleHighScoreKeys(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) {
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameInProgress);
        }
    }

    private void handleInstructionsKeys(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) {
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameInProgress);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentScreen == Screen.GAME) {
            handleGameKeyReleased(e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // typing only used in PLAYER_SETUP
        if (currentScreen != Screen.PLAYER_SETUP) return;

        char c = e.getKeyChar();
        if (Character.isLetterOrDigit(c) || c == ' ') {
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

    // ==================== Utility drawing helpers ====================

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private void background(GL2 gl, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        fillRect(gl, -380, -240, 380, 240);
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
