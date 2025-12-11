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
 * Main window and screen controller for the game.
 */
public class AirHockeyGame extends JFrame
        implements GLEventListener, KeyListener, PlayerSetupScreen.Listener {

    private GLCanvas canvas;
    private FPSAnimator animator;
    private TextRenderer textRenderer;

    // ----- Screens -----
    private enum Screen {
        MAIN_MENU,
        MODE_SELECT,
        AI_DIFFICULTY,
        PLAYER_SETUP,
        GAME,
        HIGH_SCORES,
        INSTRUCTIONS,
        SETTINGS
    }

    private Screen currentScreen = Screen.MAIN_MENU;

    private final MainMenuScreen mainMenu;
    private final GameModeScreen gameModeScreen;
    private final AiDifficultyScreen aiDifficultyScreen;
    private final HighScoresScreen highScores;
    private final InstructionsScreen instructions;
    private final GameWorld gameWorld;
    private final PlayerSetupScreen playerSetup;
    private final SettingsScreen settings;

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

        mainMenu = new MainMenuScreen();
        gameModeScreen = new GameModeScreen();
        aiDifficultyScreen = new AiDifficultyScreen();
        highScores = new HighScoresScreen();
        instructions = new InstructionsScreen();
        gameWorld = new GameWorld(highScores);
        playerSetup = new PlayerSetupScreen(this);
        settings = new SettingsScreen();

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

        mainMenu.open(false);
        canvas.requestFocusInWindow();

        // Start background music on app launch
        SoundManager.getInstance().playGameMusicLoop();
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

        // 🔠 big UI font for all screens
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 32), true, true);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        switch (currentScreen) {
            case MAIN_MENU:
                drawMenuBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                mainMenu.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case MODE_SELECT:
                drawGenericSoftBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                gameModeScreen.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case AI_DIFFICULTY:
                drawGenericSoftBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                aiDifficultyScreen.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case PLAYER_SETUP:
                drawGenericSoftBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                playerSetup.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case GAME:
                gameWorld.update();
                gameWorld.draw(gl, textRenderer, windowWidth, windowHeight);

                if (gameWorld.consumeMatchFinished()) {
                    currentScreen = Screen.HIGH_SCORES;
                    mainMenu.open(false);
                }
                break;

            case HIGH_SCORES:
                drawHighScoresBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                highScores.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case INSTRUCTIONS:
                drawInstructionsBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                instructions.draw(textRenderer, windowWidth, windowHeight);
                textRenderer.endRendering();
                break;

            case SETTINGS:
                drawSettingsBackground(gl);
                if (textRenderer == null) return;
                textRenderer.beginRendering(windowWidth, windowHeight);
                settings.draw(textRenderer, windowWidth, windowHeight);
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
        SoundManager.getInstance().stopGameMusic();
    }

    // ==================== PlayerSetupScreen.Listener ====================

    @Override
    public void onCancel() {
        currentScreen = Screen.MODE_SELECT; // back to mode select instead of main
    }

    @Override
    public void onNamesConfirmed(String leftName, String rightName) {
        gameWorld.startNewMatch(leftName, rightName); // 2-player
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
            case MODE_SELECT:
                handleModeSelectKeys(code);
                break;
            case AI_DIFFICULTY:
                handleAiDifficultyKeys(code);
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
            case SETTINGS:
                handleSettingsKeys(code);
                break;
        }
    }

    private void handleMenuKeys(int code) {
        if (code == KeyEvent.VK_UP) {
            mainMenu.moveUp();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_DOWN) {
            mainMenu.moveDown();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_ENTER) {
            SoundManager.getInstance().playClick();
            String action = mainMenu.getSelectedAction();
            switch (action) {
                case "play":
                    gameModeScreen.reset();
                    currentScreen = Screen.MODE_SELECT;
                    break;
                case "continue":
                    if (gameWorld.isGameInProgress()) {
                        gameWorld.setPaused(false);
                        currentScreen = Screen.GAME;
                    }
                    break;
                case "settings":
                    currentScreen = Screen.SETTINGS;
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
            if (gameWorld.isGameInProgress()) {
                gameWorld.setPaused(false);
                currentScreen = Screen.GAME;
            } else {
                System.exit(0);
            }
        }
    }

    private void handleModeSelectKeys(int code) {
        if (code == KeyEvent.VK_UP) {
            gameModeScreen.moveUp();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_DOWN) {
            gameModeScreen.moveDown();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_ENTER) {
            SoundManager.getInstance().playClick();
            String action = gameModeScreen.getSelectedAction();
            switch (action) {
                case "pvp":
                    playerSetup.reset();
                    currentScreen = Screen.PLAYER_SETUP;
                    break;
                case "ai":
                    aiDifficultyScreen.reset();
                    currentScreen = Screen.AI_DIFFICULTY;
                    break;
                case "back":
                    currentScreen = Screen.MAIN_MENU;
                    break;
            }
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentScreen = Screen.MAIN_MENU;
        }
    }

    private void handleAiDifficultyKeys(int code) {
        if (code == KeyEvent.VK_UP) {
            aiDifficultyScreen.moveUp();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_DOWN) {
            aiDifficultyScreen.moveDown();
            SoundManager.getInstance().playClick();
        } else if (code == KeyEvent.VK_ENTER) {
            SoundManager.getInstance().playClick();
            String action = aiDifficultyScreen.getSelectedAction();
            switch (action) {
                case "easy":
                    gameWorld.startNewMatch("Player 1", "AI (Easy)", true, GameWorld.Difficulty.EASY);
                    currentScreen = Screen.GAME;
                    break;
                case "medium":
                    gameWorld.startNewMatch("Player 1", "AI (Medium)", true, GameWorld.Difficulty.MEDIUM);
                    currentScreen = Screen.GAME;
                    break;
                case "hard":
                    gameWorld.startNewMatch("Player 1", "AI (Hard)", true, GameWorld.Difficulty.HARD);
                    currentScreen = Screen.GAME;
                    break;
                case "back":
                    currentScreen = Screen.MODE_SELECT;
                    break;
            }
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentScreen = Screen.MODE_SELECT;
        }
    }

    private void handleGameKeyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
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

    private void handleSettingsKeys(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            SoundManager.getInstance().playClick();
            boolean enabled = SoundManager.getInstance().toggleSoundEnabled();
            if (enabled) {
                SoundManager.getInstance().playGameMusicLoop();
            }
        } else if (code == KeyEvent.VK_ESCAPE) {
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

    // ==================== Background helpers ====================

    private void drawMenuBackground(GL2 gl) {
        drawVerticalGradient(gl,
                0.05f, 0.08f, 0.20f,  // top: deep blue
                0.02f, 0.02f, 0.05f   // bottom: almost black
        );
    }

    private void drawSettingsBackground(GL2 gl) {
        drawVerticalGradient(gl,
                0.10f, 0.05f, 0.20f,  // top: purple
                0.02f, 0.03f, 0.08f   // bottom: dark indigo
        );
    }

    private void drawInstructionsBackground(GL2 gl) {
        drawVerticalGradient(gl,
                0.18f, 0.10f, 0.05f,  // top: warm orange/brown
                0.05f, 0.02f, 0.02f   // bottom: dark warm
        );
    }

    private void drawHighScoresBackground(GL2 gl) {
        drawVerticalGradient(gl,
                0.08f, 0.10f, 0.16f,
                0.01f, 0.02f, 0.04f
        );
    }

    private void drawGenericSoftBackground(GL2 gl) {
        drawVerticalGradient(gl,
                0.06f, 0.08f, 0.16f,
                0.01f, 0.02f, 0.05f
        );
    }

    private void drawVerticalGradient(GL2 gl,
                                      float rTop, float gTop, float bTop,
                                      float rBottom, float gBottom, float bBottom) {
        gl.glBegin(GL2.GL_QUADS);

        gl.glColor3f(rTop, gTop, bTop);
        gl.glVertex2d(-380, 240);
        gl.glVertex2d(380, 240);

        gl.glColor3f(rBottom, gBottom, bBottom);
        gl.glVertex2d(380, -240);
        gl.glVertex2d(-380, -240);

        gl.glEnd();
    }
}
