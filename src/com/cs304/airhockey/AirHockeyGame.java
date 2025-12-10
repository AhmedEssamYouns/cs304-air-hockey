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
 *
 * Screens:
 *  - Main menu
 *  - Player setup (enter names)
 *  - Game
 *  - High scores
 *  - Instructions
 *
 * Controls:
 *  - Main menu: UP / DOWN, ENTER, ESC
 *  - Player setup: type name, BACKSPACE, ENTER, ESC
 *  - Game:
 *      Left player  : W / S
 *      Right player : UP / DOWN
 *      Pause / Play : P or SPACE
 *      Open menu    : ESC (pauses game)
 */
public class AirHockeyGame extends JFrame
        implements GLEventListener, KeyListener, PlayerSetupScreen.Listener {

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

    private final MainMenuScreen mainMenu;
    private final HighScoresScreen highScores;
    private final InstructionsScreen instructions;
    private final GameWorld gameWorld;
    private final PlayerSetupScreen playerSetup;

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

        // Create screens / world
        mainMenu = new MainMenuScreen();
        highScores = new HighScoresScreen();
        instructions = new InstructionsScreen();
        gameWorld = new GameWorld(highScores);
        playerSetup = new PlayerSetupScreen(this);

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
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                mainMenu.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case PLAYER_SETUP:
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                playerSetup.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case GAME:
                gameWorld.update();
                gameWorld.draw(gl, textRenderer, windowWidth, windowHeight);

                // If the match has finished, move to high scores
                if (gameWorld.consumeMatchFinished()) {
                    currentScreen = Screen.HIGH_SCORES;
                    mainMenu.open(false);
                }
                break;

            case HIGH_SCORES:
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                highScores.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case INSTRUCTIONS:
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                instructions.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
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

    // ==================== PlayerSetupScreen.Listener ====================

    @Override
    public void onCancel() {
        currentScreen = Screen.MAIN_MENU;
        mainMenu.open(gameWorld.isGameInProgress());
    }

    @Override
    public void onNamesConfirmed(String leftName, String rightName) {
        gameWorld.startNewMatch(leftName, rightName);
        currentScreen = Screen.GAME;
    }

    // ==================== KeyListener ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (currentScreen) {
            case MAIN_MENU:
                handleMenuKeys(code);
                break;

            case PLAYER_SETUP:
                playerSetup.handleKeyPressed(code);
                break;

            case GAME:
                handleGameKeyPressed(code);
                break;

            case HIGH_SCORES:
                handleHighScoreKeys(code);
                break;

            case INSTRUCTIONS:
                handleInstructionsKeys(code);
                break;
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
                    playerSetup.reset();
                    currentScreen = Screen.PLAYER_SETUP;
                    break;
                case "continue":
                    if (gameWorld.isGameInProgress()) {
                        gameWorld.setPaused(false);
                        currentScreen = Screen.GAME;
                    }
                    break;
                case "endgame":
                    gameWorld.endCurrentGame();
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
            if (gameWorld.isGameInProgress()) {
                gameWorld.setPaused(false);
                currentScreen = Screen.GAME;
            } else {
                System.exit(0);
            }
        }
    }

    private void handleGameKeyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            // open main menu, pause game
            gameWorld.setPaused(true);
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameWorld.isGameInProgress());
        } else if (code == KeyEvent.VK_P || code == KeyEvent.VK_SPACE) {
            gameWorld.togglePause();
        } else {
            gameWorld.handleKeyPressed(code);
        }
    }

    private void handleHighScoreKeys(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) {
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameWorld.isGameInProgress());
        }
    }

    private void handleInstructionsKeys(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER) {
            currentScreen = Screen.MAIN_MENU;
            mainMenu.open(gameWorld.isGameInProgress());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentScreen == Screen.GAME) {
            gameWorld.handleKeyReleased(e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentScreen == Screen.PLAYER_SETUP) {
            playerSetup.handleKeyTyped(e.getKeyChar());
        }
    }
}
