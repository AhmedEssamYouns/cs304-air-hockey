package com.cs304.airhockey;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

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
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * Main window and screen controller for the game.
 */
public class AirHockeyGame extends JFrame
        implements GLEventListener, KeyListener, PlayerSetupScreen.Listener {

    private GLCanvas canvas;
    private FPSAnimator animator;
    private TextRenderer textRenderer;

    // Background texture for main menu
    private Texture menuBgTexture;
    private static final String MENU_BG_PATH =
            "/imgs/menu_bg.png";

    // Simple flags for logging
    private boolean menuBgLoadedOk = false;
    private boolean menuBgAppliedOnce = false;

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

        // 🔹 Load main menu background texture with logging
        System.out.println("[MenuBG] init() - trying to load menu background from: " + MENU_BG_PATH);
        try {
            File file = new File(MENU_BG_PATH);
            if (file.exists()) {
                menuBgTexture = TextureIO.newTexture(file, true);
                menuBgTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                menuBgTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

                menuBgLoadedOk = true;
                System.out.println("[MenuBG] Loaded OK. Texture size: "
                        + menuBgTexture.getWidth() + "x" + menuBgTexture.getHeight());
            } else {
                System.err.println("[MenuBG] File not found at: " + MENU_BG_PATH);
            }
        } catch (IOException ex) {
            System.err.println("[MenuBG] Failed to load texture: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
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

        // Optional: free texture resources
        if (menuBgTexture != null) {
            GL2 gl = drawable.getGL().getGL2();
            System.out.println("[MenuBG] Disposing texture resources.");
            menuBgTexture.destroy(gl);
            menuBgTexture = null;
        }
    }

    // ==================== PlayerSetupScreen.Listener ====================

    @Override
    public void onCancel() {
        currentScreen = Screen.MODE_SELECT; // back to mode select instead of main
    }

    @Override
    public void onTwoPlayerNamesConfirmed(String leftName, String rightName) {
        gameWorld.startNewMatch(leftName, rightName); // 2-player
        currentScreen = Screen.GAME;
    }

    @Override
    public void onFourPlayers2v2NamesConfirmed(String left1, String left2,
                                               String right1, String right2) {
        gameWorld.startNewMatch2v2(left1, left2, right1, right2);
        currentScreen = Screen.GAME;
    }

    @Override
    public void onFourPlayersFfaNamesConfirmed(String left, String right,
                                               String top, String bottom) {
        gameWorld.startNewFreeForAll(left, right, top, bottom);
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
                case "pvp2":
                    playerSetup.configureTwoPlayers();
                    currentScreen = Screen.PLAYER_SETUP;
                    break;
                case "pvp4":
                    playerSetup.configureFourPlayers2v2();
                    currentScreen = Screen.PLAYER_SETUP;
                    break;
                case "ffa4":
                    playerSetup.configureFourPlayersFfa();
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
        if (menuBgTexture != null) {
            if (!menuBgAppliedOnce) {
                System.out.println("[MenuBG] Using textured background for MAIN_MENU (simple path).");
                menuBgAppliedOnce = true;
            }

            // Make sure nothing interferes
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glDisable(GL2.GL_DEPTH_TEST);
            gl.glDisable(GL2.GL_CULL_FACE);

            // Enable texture & blending (for PNG alpha)
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

            // Ensure we are in the same projection we set in init/reshape
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            // Use JOGL's texture coords (handles NPOT correctly)
            com.jogamp.opengl.util.texture.TextureCoords tc = menuBgTexture.getImageTexCoords();
            menuBgTexture.bind(gl);

            // If you want to see clearly if the quad is drawn, you can also log once:
            // System.out.println("[MenuBG] Drawing textured quad...");

            gl.glBegin(GL2.GL_QUADS);

            // bottom-left
            gl.glTexCoord2f(tc.left(), tc.bottom());
            gl.glVertex2d(-380, -240);

            // bottom-right
            gl.glTexCoord2f(tc.right(), tc.bottom());
            gl.glVertex2d(380, -240);

            // top-right
            gl.glTexCoord2f(tc.right(), tc.top());
            gl.glVertex2d(380, 240);

            // top-left
            gl.glTexCoord2f(tc.left(), tc.top());
            gl.glVertex2d(-380, 240);

            gl.glEnd();

            gl.glDisable(GL2.GL_BLEND);
            gl.glDisable(GL2.GL_TEXTURE_2D);
        } else {
            // Only log once that we're falling back
            if (!menuBgLoadedOk) {
                System.out.println("[MenuBG] No texture available at runtime, using gradient background.");
                menuBgLoadedOk = true;
            }

            // Fallback gradient if texture fails to load
            drawVerticalGradient(gl,
                    0.05f, 0.08f, 0.20f,  // top: deep blue
                    0.02f, 0.02f, 0.05f   // bottom: almost black
            );
        }
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
