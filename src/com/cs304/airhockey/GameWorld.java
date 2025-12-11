package com.cs304.airhockey;

import java.awt.event.KeyEvent;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Handles all in-game logic (puck, paddles, scores) and drawing.
 */
public class GameWorld {

    // Difficulty for AI mode
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    // Game type
    private enum GameType {
        TWO_PLAYERS,
        VS_AI,
        FOUR_PLAYERS_2V2,
        FOUR_PLAYERS_FFA
    }

    private enum Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    private enum LastHit {
        NONE,
        LEFT_MAIN,
        LEFT_SECOND,
        RIGHT_MAIN,
        RIGHT_SECOND,
        TOP,
        BOTTOM
    }

    private GameType gameType = GameType.TWO_PLAYERS;

    // ----- World bounds (rink) -----
    private final double WORLD_LEFT = -360;
    private final double WORLD_RIGHT = 360;
    private final double WORLD_BOTTOM = -220;
    private final double WORLD_TOP = 220;

    // ----- Paddles -----
    // vertical side paddles (used in all modes)
    private double paddleHalfW = 10;
    private double paddleHalfH = 60;

    private double leftPaddleX = -320;
    private double leftPaddleY = 0;      // main left paddle

    private double rightPaddleX = 320;
    private double rightPaddleY = 0;     // main right paddle

    // extra vertical paddles for 4-player 2v2
    private double leftPaddle2Y = -120;
    private double rightPaddle2Y = 120;

    // horizontal paddles for 4-player free-for-all
    private double horizontalPaddleHalfW = 60;
    private double horizontalPaddleHalfH = 10;

    private double topPaddleX = 0;
    private double topPaddleY = WORLD_TOP - 40;

    private double bottomPaddleX = 0;
    private double bottomPaddleY = WORLD_BOTTOM + 40;

    private double paddleSpeed = 6;

    // key state for smooth controls (used for human players)
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    // extra keys for 4-player modes
    // 2v2 second paddles
    private boolean tPressed = false;
    private boolean gPressed = false;
    private boolean iPressed = false;
    private boolean kPressed = false;

    // free-for-all horizontal paddles
    private boolean aPressed = false;
    private boolean dPressed = false;
    private boolean jPressed = false;
    private boolean lPressed = false;

    // ----- Puck -----
    private double puckX = 0;
    private double puckY = 0;
    private double puckR = 12;

    private double puckVX = 6;
    private double puckVY = 4;

    private LastHit lastHit = LastHit.NONE;

    // ----- Game state -----
    private int leftScore = 0;
    private int rightScore = 0;
    private int winningScore = 5;
    private boolean paused = false;
    private boolean gameInProgress = false;

    // 2-player / vsAI labels
    private String leftPlayerName = "Left Player";
    private String rightPlayerName = "Right Player";

    // 4-player 2v2 names (two per side)
    private String leftTeamP1Name = "Left P1";
    private String leftTeamP2Name = "Left P2";
    private String rightTeamP1Name = "Right P1";
    private String rightTeamP2Name = "Right P2";

    // 4-player free-for-all names and scores
    private String ffaLeftName = "Left Player";
    private String ffaRightName = "Right Player";
    private String ffaTopName = "Top Player";
    private String ffaBottomName = "Bottom Player";

    private int ffaLeftScore = 0;
    private int ffaRightScore = 0;
    private int ffaTopScore = 0;
    private int ffaBottomScore = 0;
    private int ffaWinningScore = 5;

    private boolean matchFinished = false;

    // ----- Single-player vs AI meta -----
    private boolean vsAi = false;
    private Difficulty aiDifficulty = Difficulty.MEDIUM;

    // score/lives/levels only used when vsAi == true
    private int playerScore = 0;
    private int playerLives = 3;
    private int level = 1;

    // ----- Round-start countdown (3..2..1) -----
    private boolean roundStarting = false;
    private int roundFramesTotal = 180;       // ~3 seconds at 60 FPS
    private int roundFramesRemaining = 0;
    private int nextServeDirection = 1;       // +1 = towards right, -1 = towards left

    // speed progression for AI mode levels
    private double puckSpeedMultiplier = 1.0;

    private final HighScoresScreen highScores;

    public GameWorld(HighScoresScreen highScores) {
        this.highScores = highScores;
    }

    // ==================== Public API ====================

    /**
     * Start a local 2-player match (no AI).
     */
    public void startNewMatch(String leftName, String rightName) {
        startNewMatch(leftName, rightName, false, Difficulty.MEDIUM);
    }

    /**
     * Start a new match, with option to play vs AI (2 players only).
     */
    public void startNewMatch(String leftName,
                              String rightName,
                              boolean vsAi,
                              Difficulty difficulty) {
        this.gameType = vsAi ? GameType.VS_AI : GameType.TWO_PLAYERS;

        this.leftPlayerName = sanitizeName(leftName, "Left Player");
        this.rightPlayerName = sanitizeName(rightName, "Right Player");

        this.vsAi = vsAi;
        this.aiDifficulty = difficulty;

        resetCommonState();
    }

    /**
     * Start a 4 players 2 vs 2 match (two paddles on left, two on right).
     */
    public void startNewMatch2v2(String left1, String left2,
                                 String right1, String right2) {
        this.gameType = GameType.FOUR_PLAYERS_2V2;
        this.vsAi = false;
        this.aiDifficulty = Difficulty.MEDIUM;

        this.leftTeamP1Name = sanitizeName(left1, "Left P1");
        this.leftTeamP2Name = sanitizeName(left2, "Left P2");
        this.rightTeamP1Name = sanitizeName(right1, "Right P1");
        this.rightTeamP2Name = sanitizeName(right2, "Right P2");

        // Team labels used in some HUD text
        this.leftPlayerName = leftTeamP1Name + " & " + leftTeamP2Name;
        this.rightPlayerName = rightTeamP1Name + " & " + rightTeamP2Name;

        resetCommonState();

        // nicer starting positions for paddles
        leftPaddleY = 80;
        leftPaddle2Y = -80;
        rightPaddleY = 80;
        rightPaddle2Y = -80;
    }

    /**
     * Start a 4 players free-for-all:
     * - Left side
     * - Right side
     * - Top
     * - Bottom
     */
    public void startNewFreeForAll(String leftName, String rightName,
                                   String topName, String bottomName) {
        this.gameType = GameType.FOUR_PLAYERS_FFA;
        this.vsAi = false;
        this.aiDifficulty = Difficulty.MEDIUM;

        this.ffaLeftName = sanitizeName(leftName, "Left Player");
        this.ffaRightName = sanitizeName(rightName, "Right Player");
        this.ffaTopName = sanitizeName(topName, "Top Player");
        this.ffaBottomName = sanitizeName(bottomName, "Bottom Player");

        resetCommonState();

        ffaLeftScore = 0;
        ffaRightScore = 0;
        ffaTopScore = 0;
        ffaBottomScore = 0;

        // paddles initial positions
        leftPaddleY = 0;
        rightPaddleY = 0;

        topPaddleX = 0;
        topPaddleY = WORLD_TOP - 40;
        bottomPaddleX = 0;
        bottomPaddleY = WORLD_BOTTOM + 40;
    }

    public void endCurrentGame() {
        gameInProgress = false;
        paused = false;
        matchFinished = false;

        leftScore = 0;
        rightScore = 0;

        ffaLeftScore = ffaRightScore = ffaTopScore = ffaBottomScore = 0;

        playerScore = 0;
        playerLives = 3;
        level = 1;

        leftPaddleY = 0;
        rightPaddleY = 0;
        leftPaddle2Y = -120;
        rightPaddle2Y = 120;

        topPaddleX = 0;
        bottomPaddleX = 0;

        puckX = 0;
        puckY = 0;
        puckVX = 6;
        puckVY = 4;

        roundStarting = false;
        roundFramesRemaining = 0;
        puckSpeedMultiplier = 1.0;

        lastHit = LastHit.NONE;

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
        } else if (code == KeyEvent.VK_T) {
            tPressed = true;
        } else if (code == KeyEvent.VK_G) {
            gPressed = true;
        } else if (code == KeyEvent.VK_I) {
            iPressed = true;
        } else if (code == KeyEvent.VK_K) {
            kPressed = true;
        } else if (code == KeyEvent.VK_A) {
            aPressed = true;
        } else if (code == KeyEvent.VK_D) {
            dPressed = true;
        } else if (code == KeyEvent.VK_J) {
            jPressed = true;
        } else if (code == KeyEvent.VK_L) {
            lPressed = true;
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
        } else if (code == KeyEvent.VK_T) {
            tPressed = false;
        } else if (code == KeyEvent.VK_G) {
            gPressed = false;
        } else if (code == KeyEvent.VK_I) {
            iPressed = false;
        } else if (code == KeyEvent.VK_K) {
            kPressed = false;
        } else if (code == KeyEvent.VK_A) {
            aPressed = false;
        } else if (code == KeyEvent.VK_D) {
            dPressed = false;
        } else if (code == KeyEvent.VK_J) {
            jPressed = false;
        } else if (code == KeyEvent.VK_L) {
            lPressed = false;
        }
    }

    public void update() {
        if (!gameInProgress || paused) return;

        updatePaddles();

        if (roundStarting) {
            if (roundFramesRemaining > 0) {
                roundFramesRemaining--;
            }
            if (roundFramesRemaining <= 0) {
                launchPuck();
            }
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
        if (matchFinished) {
            matchFinished = false;
            return true;
        }
        return false;
    }

    // ==================== Internal logic ====================

    private void resetCommonState() {
        leftScore = 0;
        rightScore = 0;

        paused = false;
        gameInProgress = true;
        matchFinished = false;

        leftPaddleY = 0;
        rightPaddleY = 0;
        leftPaddle2Y = -120;
        rightPaddle2Y = 120;

        topPaddleX = 0;
        bottomPaddleX = 0;

        // reset key state
        wPressed = sPressed = upPressed = downPressed = false;
        tPressed = gPressed = iPressed = kPressed = false;
        aPressed = dPressed = jPressed = lPressed = false;

        // reset meta
        puckSpeedMultiplier = 1.0;
        playerScore = 0;
        playerLives = 3;
        level = 1;

        // center puck and start countdown for first serve
        startRoundCountdown(Math.random() < 0.5 ? -1 : 1);

        lastHit = LastHit.NONE;

        // background music (no double-start issue)
        SoundManager.getInstance().playGameMusicLoop();
    }

    private String sanitizeName(String name, String fallback) {
        if (name == null) return fallback;
        String trimmed = name.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private void updatePaddles() {
        switch (gameType) {
            case VS_AI:
                if (wPressed) leftPaddleY += paddleSpeed;
                if (sPressed) leftPaddleY -= paddleSpeed;
                updateAiPaddle();
                break;

            case TWO_PLAYERS:
                if (wPressed) leftPaddleY += paddleSpeed;
                if (sPressed) leftPaddleY -= paddleSpeed;
                if (upPressed) rightPaddleY += paddleSpeed;
                if (downPressed) rightPaddleY -= paddleSpeed;
                break;

            case FOUR_PLAYERS_2V2:
                // left team
                if (wPressed) leftPaddleY += paddleSpeed;
                if (sPressed) leftPaddleY -= paddleSpeed;
                if (tPressed) leftPaddle2Y += paddleSpeed;
                if (gPressed) leftPaddle2Y -= paddleSpeed;

                // right team
                if (upPressed) rightPaddleY += paddleSpeed;
                if (downPressed) rightPaddleY -= paddleSpeed;
                if (iPressed) rightPaddle2Y += paddleSpeed;
                if (kPressed) rightPaddle2Y -= paddleSpeed;
                break;

            case FOUR_PLAYERS_FFA:
                // left vertical
                if (wPressed) leftPaddleY += paddleSpeed;
                if (sPressed) leftPaddleY -= paddleSpeed;
                // right vertical
                if (upPressed) rightPaddleY += paddleSpeed;
                if (downPressed) rightPaddleY -= paddleSpeed;
                // top horizontal: J = left, L = right
                if (jPressed) topPaddleX -= paddleSpeed;
                if (lPressed) topPaddleX += paddleSpeed;
                // bottom horizontal: A = left, D = right
                if (aPressed) bottomPaddleX -= paddleSpeed;
                if (dPressed) bottomPaddleX += paddleSpeed;
                break;
        }

        // clamp vertical paddles
        leftPaddleY = clamp(leftPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
        rightPaddleY = clamp(rightPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);

        if (gameType == GameType.FOUR_PLAYERS_2V2) {
            leftPaddle2Y = clamp(leftPaddle2Y, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
            rightPaddle2Y = clamp(rightPaddle2Y, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
        }

        if (gameType == GameType.FOUR_PLAYERS_FFA) {
            topPaddleX = clamp(topPaddleX, WORLD_LEFT + horizontalPaddleHalfW, WORLD_RIGHT - horizontalPaddleHalfW);
            bottomPaddleX = clamp(bottomPaddleX, WORLD_LEFT + horizontalPaddleHalfW, WORLD_RIGHT - horizontalPaddleHalfW);
        }
    }

    private void updateAiPaddle() {
        // Simple AI: try to follow puckY with speed based on difficulty + level
        double targetY = puckY;
        double dy = targetY - rightPaddleY;

        double baseSpeed;
        switch (aiDifficulty) {
            case EASY:
                baseSpeed = 4.0;
                break;
            case MEDIUM:
                baseSpeed = 7.0;
                break;
            case HARD:
                baseSpeed = 10.0;
                break;
            default:
                baseSpeed = 7.0;
        }

        // slightly faster each level
        double aiSpeed = baseSpeed + (level - 1) * 0.8;

        if (Math.abs(dy) > 3) {
            double step = Math.min(Math.abs(dy), aiSpeed);
            double dir = Math.signum(dy);

            double noiseFactor;
            switch (aiDifficulty) {
                case EASY:
                    noiseFactor = 0.6;
                    break;
                case MEDIUM:
                    noiseFactor = 0.3;
                    break;
                case HARD:
                    noiseFactor = 0.1;
                    break;
                default:
                    noiseFactor = 0.3;
            }
            double noise = (Math.random() - 0.5) * noiseFactor * 6.0;

            rightPaddleY += dir * step + noise;
        }

        rightPaddleY = clamp(rightPaddleY, WORLD_BOTTOM + paddleHalfH, WORLD_TOP - paddleHalfH);
    }

    private void updatePuck() {
        puckX += puckVX;
        puckY += puckVY;

        // For free-for-all mode, top/bottom are goals, so no bounce on them
        if (gameType != GameType.FOUR_PLAYERS_FFA) {
            if (puckY + puckR > WORLD_TOP) {
                puckY = WORLD_TOP - puckR;
                puckVY = -puckVY;
            } else if (puckY - puckR < WORLD_BOTTOM) {
                puckY = WORLD_BOTTOM + puckR;
                puckVY = -puckVY;
            }
        }

        checkPaddleCollision();

        // Left/right goals (all modes)
        if (puckX - puckR < WORLD_LEFT) {
            if (gameType == GameType.FOUR_PLAYERS_FFA) {
                handleFreeForAllGoal(Side.LEFT);
            } else if (vsAi) {
                handleAiGoal();
            } else {
                rightScore++;
                boolean someoneWon = checkWinTwoPlayerOrTeam();
                if (!someoneWon) {
                    startRoundCountdown(-1);
                }
            }
            return;
        } else if (puckX + puckR > WORLD_RIGHT) {
            if (gameType == GameType.FOUR_PLAYERS_FFA) {
                handleFreeForAllGoal(Side.RIGHT);
            } else if (vsAi) {
                handlePlayerGoal();
            } else {
                leftScore++;
                boolean someoneWon = checkWinTwoPlayerOrTeam();
                if (!someoneWon) {
                    startRoundCountdown(1);
                }
            }
            return;
        }

        // Top / bottom goals only in free-for-all
        if (gameType == GameType.FOUR_PLAYERS_FFA) {
            if (puckY + puckR > WORLD_TOP) {
                handleFreeForAllGoal(Side.TOP);
                return;
            } else if (puckY - puckR < WORLD_BOTTOM) {
                handleFreeForAllGoal(Side.BOTTOM);
            }
        }
    }

    // start a countdown for the next round, freezing the puck at center
    private void startRoundCountdown(int directionToRight) {
        roundStarting = true;
        nextServeDirection = directionToRight;
        roundFramesRemaining = roundFramesTotal;

        // place puck at center and freeze it
        puckX = 0;
        puckY = 0;
        puckVX = 0;
        puckVY = 0;

        lastHit = LastHit.NONE;
    }

    // actually launch the puck after countdown
    private void launchPuck() {
        double randomY = (Math.random() - 0.5) * 6;

        double baseSpeed = 6.0 * puckSpeedMultiplier;
        puckVX = baseSpeed * nextServeDirection;
        puckVY = randomY * puckSpeedMultiplier;

        roundStarting = false;
    }

    private void handlePlayerGoal() {
        leftScore++;
        playerScore += 100 * level;

        SoundManager.getInstance().playHit();

        if (leftScore >= winningScore) {
            // Level up!
            level++;
            leftScore = 0;
            rightScore = 0;

            // Slightly faster puck as level rises
            puckSpeedMultiplier *= 1.05;
        }

        // next serve from center towards AI (positive X)
        startRoundCountdown(-1);
    }

    private void handleAiGoal() {
        rightScore++;
        playerLives--;

        if (playerLives <= 0) {
            // Game over for player (vs AI)
            gameInProgress = false;
            paused = true;
            matchFinished = true;

            // record high score for player
            highScores.addScore(leftPlayerName, playerScore);

            // play game-over sound and pause bg music for 5 seconds
            SoundManager.getInstance().playGameOverThenResume(5000);
        } else {
            // serve towards player again (negative X)
            startRoundCountdown(1);
        }
    }

    private void handleFreeForAllGoal(Side side) {
        // award a point to the player who last hit the puck
        switch (lastHit) {
            case LEFT_MAIN:
            case LEFT_SECOND:
                if (side != Side.LEFT) {
                    ffaLeftScore++;
                }
                break;
            case RIGHT_MAIN:
            case RIGHT_SECOND:
                if (side != Side.RIGHT) {
                    ffaRightScore++;
                }
                break;
            case TOP:
                if (side != Side.TOP) {
                    ffaTopScore++;
                }
                break;
            case BOTTOM:
                if (side != Side.BOTTOM) {
                    ffaBottomScore++;
                }
                break;
            default:
                // nobody touched it recently, no score
                break;
        }

        SoundManager.getInstance().playHit();

        // reset puck to center with random direction
        startRoundCountdown(Math.random() < 0.5 ? -1 : 1);

        checkWinFreeForAll();
    }

    private void checkPaddleCollision() {
        // main left paddle
        checkVerticalPaddleCollision(leftPaddleX, leftPaddleY,
                paddleHalfW, paddleHalfH, true, LastHit.LEFT_MAIN);

        // second left paddle for 2v2
        if (gameType == GameType.FOUR_PLAYERS_2V2) {
            checkVerticalPaddleCollision(leftPaddleX, leftPaddle2Y,
                    paddleHalfW, paddleHalfH, true, LastHit.LEFT_SECOND);
        }

        // main right paddle (used in all modes)
        checkVerticalPaddleCollision(rightPaddleX, rightPaddleY,
                paddleHalfW, paddleHalfH, false, LastHit.RIGHT_MAIN);

        // second right paddle for 2v2
        if (gameType == GameType.FOUR_PLAYERS_2V2) {
            checkVerticalPaddleCollision(rightPaddleX, rightPaddle2Y,
                    paddleHalfW, paddleHalfH, false, LastHit.RIGHT_SECOND);
        }

        // horizontal paddles only in free-for-all
        if (gameType == GameType.FOUR_PLAYERS_FFA) {
            checkHorizontalPaddleCollision(topPaddleX, topPaddleY,
                    horizontalPaddleHalfW, horizontalPaddleHalfH, true, LastHit.TOP);
            checkHorizontalPaddleCollision(bottomPaddleX, bottomPaddleY,
                    horizontalPaddleHalfW, horizontalPaddleHalfH, false, LastHit.BOTTOM);
        }
    }

    private void checkVerticalPaddleCollision(double px, double py,
                                              double halfW, double halfH,
                                              boolean isLeftSide,
                                              LastHit hit) {
        double pLeft = px - halfW;
        double pRight = px + halfW;
        double pTop = py + halfH;
        double pBottom = py - halfH;

        if (puckX + puckR > pLeft && puckX - puckR < pRight &&
                puckY + puckR > pBottom && puckY - puckR < pTop) {

            if (isLeftSide && puckVX < 0) {
                puckX = pRight + puckR;
                puckVX = -puckVX;
            } else if (!isLeftSide && puckVX > 0) {
                puckX = pLeft - puckR;
                puckVX = -puckVX;
            } else {
                return;
            }

            double offset = puckY - py;
            puckVY += offset * 0.1;

            lastHit = hit;
            SoundManager.getInstance().playHit();
        }
    }

    private void checkHorizontalPaddleCollision(double px, double py,
                                                double halfW, double halfH,
                                                boolean isTop,
                                                LastHit hit) {
        double pLeft = px - halfW;
        double pRight = px + halfW;
        double pTop = py + halfH;
        double pBottom = py - halfH;

        if (puckX + puckR > pLeft && puckX - puckR < pRight &&
                puckY + puckR > pBottom && puckY - puckR < pTop) {

            if (isTop && puckVY > 0) {
                // moving up, hit bottom of top paddle
                puckY = pBottom - puckR;
                puckVY = -puckVY;
            } else if (!isTop && puckVY < 0) {
                // moving down, hit top of bottom paddle
                puckY = pTop + puckR;
                puckVY = -puckVY;
            } else {
                return;
            }

            double offset = puckX - px;
            puckVX += offset * 0.1;

            lastHit = hit;
            SoundManager.getInstance().playHit();
        }
    }

    private boolean checkWinTwoPlayerOrTeam() {
        if (vsAi || gameType == GameType.FOUR_PLAYERS_FFA) return false;

        if (leftScore >= winningScore || rightScore >= winningScore) {
            String winnerName;
            int winnerScore = Math.max(leftScore, rightScore);

            if (gameType == GameType.FOUR_PLAYERS_2V2) {
                if (leftScore > rightScore) {
                    winnerName = leftTeamP1Name + " & " + leftTeamP2Name;
                } else {
                    winnerName = rightTeamP1Name + " & " + rightTeamP2Name;
                }
            } else {
                winnerName = (leftScore > rightScore) ? leftPlayerName : rightPlayerName;
            }

            highScores.addScore(winnerName, winnerScore);

            gameInProgress = false;
            paused = true;
            matchFinished = true;

            SoundManager.getInstance().stopGameMusic();
            return true;
        }
        return false;
    }

    private void checkWinFreeForAll() {
        if (gameType != GameType.FOUR_PLAYERS_FFA) return;

        int maxScore = Math.max(Math.max(ffaLeftScore, ffaRightScore),
                Math.max(ffaTopScore, ffaBottomScore));

        if (maxScore >= ffaWinningScore) {
            String winnerName;
            if (ffaLeftScore == maxScore) {
                winnerName = ffaLeftName;
            } else if (ffaRightScore == maxScore) {
                winnerName = ffaRightName;
            } else if (ffaTopScore == maxScore) {
                winnerName = ffaTopName;
            } else {
                winnerName = ffaBottomName;
            }

            highScores.addScore(winnerName, maxScore);

            gameInProgress = false;
            paused = true;
            matchFinished = true;

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
        switch (gameType) {
            case TWO_PLAYERS:
            case VS_AI:
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
                break;

            case FOUR_PLAYERS_2V2:
                // Left team: blue shades
                gl.glColor3f(0.1f, 0.5f, 1.0f);
                fillRect(gl,
                        leftPaddleX - paddleHalfW,
                        leftPaddleY - paddleHalfH,
                        leftPaddleX + paddleHalfW,
                        leftPaddleY + paddleHalfH);

                gl.glColor3f(0.1f, 0.8f, 1.0f);
                fillRect(gl,
                        leftPaddleX - paddleHalfW,
                        leftPaddle2Y - paddleHalfH,
                        leftPaddleX + paddleHalfW,
                        leftPaddle2Y + paddleHalfH);

                // Right team: green shades
                gl.glColor3f(0.1f, 1.0f, 0.4f);
                fillRect(gl,
                        rightPaddleX - paddleHalfW,
                        rightPaddleY - paddleHalfH,
                        rightPaddleX + paddleHalfW,
                        rightPaddleY + paddleHalfH);

                gl.glColor3f(0.3f, 1.0f, 0.7f);
                fillRect(gl,
                        rightPaddleX - paddleHalfW,
                        rightPaddle2Y - paddleHalfH,
                        rightPaddleX + paddleHalfW,
                        rightPaddle2Y + paddleHalfH);
                break;

            case FOUR_PLAYERS_FFA:
                // left vertical (blue)
                gl.glColor3f(0.1f, 0.5f, 1.0f);
                fillRect(gl,
                        leftPaddleX - paddleHalfW,
                        leftPaddleY - paddleHalfH,
                        leftPaddleX + paddleHalfW,
                        leftPaddleY + paddleHalfH);

                // right vertical (green)
                gl.glColor3f(0.1f, 1.0f, 0.4f);
                fillRect(gl,
                        rightPaddleX - paddleHalfW,
                        rightPaddleY - paddleHalfH,
                        rightPaddleX + paddleHalfW,
                        rightPaddleY + paddleHalfH);

                // top horizontal (orange)
                gl.glColor3f(1.0f, 0.6f, 0.2f);
                fillRect(gl,
                        topPaddleX - horizontalPaddleHalfW,
                        topPaddleY - horizontalPaddleHalfH,
                        topPaddleX + horizontalPaddleHalfW,
                        topPaddleY + horizontalPaddleHalfH);

                // bottom horizontal (purple)
                gl.glColor3f(0.7f, 0.3f, 1.0f);
                fillRect(gl,
                        bottomPaddleX - horizontalPaddleHalfW,
                        bottomPaddleY - horizontalPaddleHalfH,
                        bottomPaddleX + horizontalPaddleHalfW,
                        bottomPaddleY + horizontalPaddleHalfH);
                break;
        }
    }

    private void drawPuck(GL2 gl) {
        gl.glColor3f(1.0f, 0.9f, 0.2f);
        drawCircle(gl, puckX, puckY, puckR, 32);
    }

    private void drawGameHUD(TextRenderer textRenderer, int windowWidth, int windowHeight) {
        if (textRenderer == null) return;

        textRenderer.beginRendering(windowWidth, windowHeight);

        textRenderer.setColor(1f, 1f, 1f, 1f);

        String topLine1;
        String topLine2 = null;
        String bottomLine;

        if (gameType == GameType.FOUR_PLAYERS_2V2) {
            topLine1 = "Left Team (" + leftTeamP1Name + " & " + leftTeamP2Name + "): " + leftScore +
                    "   Right Team (" + rightTeamP1Name + " & " + rightTeamP2Name + "): " + rightScore;

            bottomLine = "Left: P1=W/S, P2=T/G  ·  Right: P1=Up/Down, P2=I/K  ·  P: Pause  ·  ESC: Menu";
        } else if (gameType == GameType.FOUR_PLAYERS_FFA) {
            topLine1 = ffaLeftName + ": " + ffaLeftScore +
                    "   " + ffaRightName + ": " + ffaRightScore;
            topLine2 = ffaTopName + ": " + ffaTopScore +
                    "   " + ffaBottomName + ": " + ffaBottomScore;

            bottomLine = "Left=W/S  ·  Right=Up/Down  ·  Top=J/L  ·  Bottom=A/D  ·  P: Pause  ·  ESC: Menu";
        } else if (vsAi) {
            topLine1 = leftPlayerName + " (You): " + leftScore +
                    "   AI: " + rightScore +
                    "   Score: " + playerScore +
                    "   Lives: " + playerLives +
                    "   Lv: " + level +
                    " [" + aiDifficulty.name() + "]";

            bottomLine = "Controls: W/S move   |   P: Pause   |   ESC: Menu   ·  Beat the AI to level up!";
        } else {
            topLine1 = leftPlayerName + ": " + leftScore +
                    "   " + rightPlayerName + ": " + rightScore;

            bottomLine = "W/S: " + leftPlayerName +
                    "  |  Up/Down: " + rightPlayerName +
                    "  |  P: Pause  |  ESC: Menu";
        }

        // draw top HUD lines
        textRenderer.draw(topLine1, 20, windowHeight - 30);
        if (topLine2 != null) {
            textRenderer.draw(topLine2, 20, windowHeight - 60);
        }

        // bottom HUD line
        textRenderer.draw(bottomLine, 20, 20);

        // Round-start countdown in center (only while game running and not paused)
        if (gameInProgress && !paused && roundStarting && roundFramesRemaining > 0) {
            int third = roundFramesTotal / 3;
            int remaining = roundFramesRemaining;
            String label;
            if (remaining > 2 * third) {
                label = "3";
            } else if (remaining > third) {
                label = "2";
            } else {
                label = "1";
            }

            textRenderer.setColor(1f, 1f, 0.3f, 1f);
            int approxWidth = 40;
            int x = windowWidth / 2 - approxWidth / 2;
            int y = windowHeight / 2 + 40;
            textRenderer.draw(label, x, y);

            textRenderer.setColor(1f, 1f, 1f, 1f);
        }

        if (paused) {
            textRenderer.setColor(1f, 1f, 0f, 1f);
            textRenderer.draw("PAUSED", windowWidth / 2 - 70, windowHeight / 2);
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
