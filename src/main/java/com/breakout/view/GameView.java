package com.breakout.view;

import com.breakout.audio.AudioManager;
import com.breakout.database.DatabaseManager;
import com.breakout.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Game View - MVC Pattern
 * Handles all rendering using JavaFX Canvas
 *
 * Features v3.0:
 * - Background images per level
 * - Power-up and penalty rendering
 * - Multiple ball support
 * - Falling bricks rendering
 * - Active effects HUD
 * - Leaderboard display
 * - Audio status indicators
 */
public class GameView extends StackPane {
    private Canvas canvas;
    private GraphicsContext gc;
    private GameModel model;
    private AudioManager audio;

    // Background images per level
    private Map<Integer, Image> backgroundImages;
    private Image menuBackground;

    // Animation
    private double animationTime = 0;

    // Colors
    private static final Color BG_DARK = Color.rgb(20, 20, 40);
    private static final Color BG_GRADIENT_TOP = Color.rgb(30, 30, 80);
    private static final Color BG_GRADIENT_BOTTOM = Color.rgb(10, 10, 40);

    public GameView(GameModel model, double width, double height) {
        this.model = model;
        this.audio = AudioManager.getInstance();
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        this.backgroundImages = new HashMap<>();

        getChildren().add(canvas);
        gc.setTextAlign(TextAlignment.LEFT);

        loadBackgroundImages();
    }

    /**
     * Load background images for each level
     */
    private void loadBackgroundImages() {
        try {
            // Try to load background images
            for (int i = 1; i <= 5; i++) {
                try {
                    Image img = new Image(getClass().getResourceAsStream("/images/level" + i + "_bg.png"));
                    if (!img.isError()) {
                        backgroundImages.put(i, img);
                    }
                } catch (Exception e) {
                    // Background not found, will use gradient
                }
            }

            try {
                menuBackground = new Image(getClass().getResourceAsStream("/images/menu_bg.png"));
            } catch (Exception e) {
                // Menu background not found
            }
        } catch (Exception e) {
            System.out.println("Background images not found, using gradients");
        }
    }

    /**
     * Main render method - called every frame
     */
    public void render() {
        animationTime += 0.016;

        GameState state = model.getState();

        switch (state) {
            case NAME_INPUT:
                renderNameInput();
                break;
            case MENU:
                renderMenu();
                break;
            case LEADERBOARD:
                renderMenu();
                renderLeaderboardOverlay();
                break;
            case PLAYING:
                renderGame();
                renderHUD();
                break;
            case PAUSED:
                renderGame();
                renderHUD();
                renderPauseOverlay();
                break;
            case GAME_OVER:
                renderGame();
                renderGameOverOverlay();
                break;
            case LEVEL_COMPLETE:
                renderGame();
                renderLevelCompleteOverlay();
                break;
            case VICTORY:
                renderVictoryScreen();
                break;
            default:
                break;
        }
    }

    // ==================== BACKGROUND RENDERING ====================

    private void renderBackground(int level) {
        Image bg = backgroundImages.get(level);
        if (bg != null && !bg.isError()) {
            gc.drawImage(bg, 0, 0, canvas.getWidth(), canvas.getHeight());
            // Add semi-transparent overlay for better visibility
            gc.setFill(Color.rgb(0, 0, 0, 0.3));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            // Gradient background based on level
            Color topColor = getLevelColor(level).darker().darker();
            Color bottomColor = BG_GRADIENT_BOTTOM;

            LinearGradient bg_gradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, topColor),
                    new Stop(1, bottomColor)
            );
            gc.setFill(bg_gradient);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Add animated stars
            renderStars();
        }
    }

    private void renderStars() {
        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        for (int i = 0; i < 50; i++) {
            double x = (i * 37 + animationTime * 5) % canvas.getWidth();
            double y = (i * 23) % canvas.getHeight();
            double size = 1 + Math.sin(animationTime + i) * 0.5;
            gc.fillOval(x, y, size, size);
        }
    }

    private Color getLevelColor(int level) {
        return switch (level) {
            case 1 -> Color.DODGERBLUE;
            case 2 -> Color.LIMEGREEN;
            case 3 -> Color.ORANGE;
            case 4 -> Color.CRIMSON;
            case 5 -> Color.PURPLE;
            default -> Color.DODGERBLUE;
        };
    }

    // ==================== NAME INPUT SCREEN ====================

    private void renderNameInput() {
        LinearGradient bg = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, BG_GRADIENT_TOP),
                new Stop(1, BG_GRADIENT_BOTTOM)
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderStars();

        gc.setTextAlign(TextAlignment.CENTER);

        // Title with glow effect
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText("BREAKOUT", canvas.getWidth() / 2, 120);

        gc.setFill(Color.LIGHTBLUE);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Enhanced Edition v3.0", canvas.getWidth() / 2, 160);

        // Enter name prompt
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("Enter Your Name", canvas.getWidth() / 2, canvas.getHeight() / 2 - 60);

        // Name input box
        double boxWidth = 300;
        double boxHeight = 50;
        double boxX = (canvas.getWidth() - boxWidth) / 2;
        double boxY = canvas.getHeight() / 2 - 30;

        gc.setFill(Color.rgb(40, 40, 80));
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

        double pulse = Math.sin(animationTime * 3) * 0.3 + 0.7;
        gc.setStroke(Color.rgb(100, 150, 255, pulse));
        gc.setLineWidth(3);
        gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

        String nameText = model.getNameInput();
        String cursor = System.currentTimeMillis() % 1000 < 500 ? "_" : "";
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(nameText + cursor, canvas.getWidth() / 2, boxY + 33);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Press ENTER to continue", canvas.getWidth() / 2, canvas.getHeight() / 2 + 80);

        renderDecorativeBricks(canvas.getHeight() - 150);
    }

    // ==================== MENU ====================

    private void renderMenu() {
        LinearGradient bg = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(20, 20, 60)),
                new Stop(1, Color.rgb(10, 10, 30))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderStars();

        gc.setTextAlign(TextAlignment.CENTER);

        PlayerProfile profile = model.getPlayerProfile();

        // Welcome message
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.fillText("Welcome, " + profile.getPlayerName() + "!", canvas.getWidth() / 2, 50);

        // Title
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        gc.fillText("SELECT LEVEL", canvas.getWidth() / 2, 100);

        // Total score and rank
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Total Score: " + profile.getTotalScore() + "  |  Rank: #" + profile.getRank(),
                canvas.getWidth() / 2, 135);

        // Level buttons
        int totalLevels = model.getTotalLevels();
        int selectedLevel = model.getSelectedLevel();
        double startY = 160;
        double levelHeight = 65;

        for (int i = 1; i <= totalLevels; i++) {
            double y = startY + (i - 1) * levelHeight;
            boolean unlocked = profile.isLevelUnlocked(i);
            boolean selected = (i == selectedLevel);
            int levelScore = profile.getLevelScore(i);
            renderLevelButton(i, y, unlocked, selected, levelScore);
        }

        // Audio status
        renderAudioStatus();

        // Controls
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("↑↓ Select | ENTER Start | L Leaderboard | F1 Music | F2 SFX",
                canvas.getWidth() / 2, canvas.getHeight() - 20);
    }

    private void renderLevelButton(int level, double y, boolean unlocked,
                                   boolean selected, int score) {
        double buttonWidth = 500;
        double buttonHeight = 50;
        double x = (canvas.getWidth() - buttonWidth) / 2;

        // Button background
        if (selected && unlocked) {
            gc.setFill(getLevelColor(level).deriveColor(0, 0.7, 0.5, 1));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        } else if (unlocked) {
            gc.setFill(Color.rgb(40, 50, 80));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(getLevelColor(level).darker());
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        } else {
            gc.setFill(Color.rgb(30, 30, 40));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(Color.rgb(60, 60, 70));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        if (unlocked) {
            gc.setFill(selected ? Color.WHITE : Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText("Level " + level, x + 20, y + 32);

            String stars = getStarsForScore(score, level);
            gc.setFill(Color.GOLD);
            gc.fillText(stars, x + 140, y + 32);

            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(selected ? Color.YELLOW : Color.LIGHTBLUE);
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("Best: " + score, x + buttonWidth - 20, y + 32);
        } else {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText("Level " + level, x + 20, y + 32);
            gc.fillText("🔒 LOCKED", x + 140, y + 32);
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void renderAudioStatus() {
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Arial", 14));

        // Music status
        gc.setFill(audio.isMusicEnabled() ? Color.LIMEGREEN : Color.GRAY);
        gc.fillText("♪ Music: " + (audio.isMusicEnabled() ? "ON" : "OFF"),
                canvas.getWidth() - 20, 30);

        // SFX status
        gc.setFill(audio.isSfxEnabled() ? Color.LIMEGREEN : Color.GRAY);
        gc.fillText("♫ SFX: " + (audio.isSfxEnabled() ? "ON" : "OFF"),
                canvas.getWidth() - 20, 50);

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private String getStarsForScore(int score, int level) {
        if (score == 0) return "☆☆☆☆☆";
        int threshold = level * 100;
        if (score >= threshold * 5) return "★★★★★";
        if (score >= threshold * 4) return "★★★★☆";
        if (score >= threshold * 3) return "★★★☆☆";
        if (score >= threshold * 2) return "★★☆☆☆";
        if (score >= threshold) return "★☆☆☆☆";
        return "☆☆☆☆☆";
    }

    // ==================== LEADERBOARD ====================

    private void renderLeaderboardOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.85));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText("🏆 LEADERBOARD 🏆", canvas.getWidth() / 2, 80);

        List<DatabaseManager.LeaderboardEntry> entries =
                DatabaseManager.getInstance().getLeaderboard(10);

        double startY = 140;
        double rowHeight = 40;

        // Header
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("RANK", 150, startY);
        gc.fillText("PLAYER", 250, startY);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("SCORE", 550, startY);
        gc.fillText("LEVELS", 650, startY);

        startY += 10;
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeLine(100, startY, 700, startY);

        startY += 30;

        for (DatabaseManager.LeaderboardEntry entry : entries) {
            Color rowColor = entry.rank <= 3 ?
                    switch (entry.rank) {
                        case 1 -> Color.GOLD;
                        case 2 -> Color.SILVER;
                        case 3 -> Color.rgb(205, 127, 50);
                        default -> Color.WHITE;
                    } : Color.WHITE;

            gc.setFill(rowColor);
            gc.setFont(Font.font("Arial", entry.rank <= 3 ? FontWeight.BOLD : FontWeight.NORMAL, 18));

            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("#" + entry.rank, 150, startY);
            gc.fillText(entry.playerName, 250, startY);
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(String.valueOf(entry.totalScore), 550, startY);
            gc.fillText(entry.levelsCompleted + "/5", 650, startY);

            startY += rowHeight;
        }

        if (entries.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No scores yet!", canvas.getWidth() / 2, 250);
        }

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Press ESC or L to return", canvas.getWidth() / 2, canvas.getHeight() - 50);
    }

    // ==================== GAME RENDERING ====================

    private void renderGame() {
        renderBackground(model.getLevel());

        // Render shield if active
        if (model.hasShield()) {
            renderShield();
        }

        // Render blind zone if active
        if (model.hasBlindZone()) {
            renderBlindZone();
        }

        renderBricks();
        renderFallingBricks();
        renderPowerUps();
        renderPenalties();
        renderPaddle();
        renderBalls();
    }

    private void renderShield() {
        gc.setFill(Color.rgb(255, 165, 0, 0.3 + Math.sin(animationTime * 5) * 0.1));
        gc.fillRect(0, canvas.getHeight() - 20, canvas.getWidth(), 20);
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(3);
        gc.strokeLine(0, canvas.getHeight() - 20, canvas.getWidth(), canvas.getHeight() - 20);
    }

    private void renderBlindZone() {
        // Create a blind zone in the middle of the screen
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        double zoneHeight = 100;
        double zoneY = canvas.getHeight() / 2 - zoneHeight / 2;
        gc.fillRect(0, zoneY, canvas.getWidth(), zoneHeight);
    }

    private void renderBricks() {
        for (Brick brick : model.getBricks()) {
            if (brick.isActive()) {
                Color baseColor = brick.getColor();

                // Main brick
                gc.setFill(baseColor);
                gc.fillRoundRect(brick.getX(), brick.getY(),
                        brick.getWidth(), brick.getHeight(), 5, 5);

                // Highlight
                gc.setFill(baseColor.brighter());
                gc.fillRoundRect(brick.getX() + 2, brick.getY() + 2,
                        brick.getWidth() - 4, brick.getHeight() / 3, 3, 3);

                // Border
                gc.setStroke(baseColor.darker());
                gc.setLineWidth(1);
                gc.strokeRoundRect(brick.getX(), brick.getY(),
                        brick.getWidth(), brick.getHeight(), 5, 5);

                // Power-up/Penalty indicator
                if (brick.hasPowerUp()) {
                    gc.setFill(Color.CYAN);
                    gc.fillOval(brick.getX() + brick.getWidth() - 10, brick.getY() + 2, 8, 8);
                } else if (brick.hasPenalty()) {
                    gc.setFill(Color.RED);
                    gc.fillOval(brick.getX() + brick.getWidth() - 10, brick.getY() + 2, 8, 8);
                }
            }
        }
    }

    private void renderFallingBricks() {
        for (FallingBrick fb : model.getFallingBricks()) {
            if (fb.isActive()) {
                gc.save();

                // Rotate around center
                gc.translate(fb.getX(), fb.getY());
                gc.rotate(fb.getRotation());

                // Draw brick
                gc.setFill(fb.getColor());
                gc.fillRoundRect(-fb.getWidth()/2, -fb.getHeight()/2,
                        fb.getWidth(), fb.getHeight(), 5, 5);

                // Warning glow
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRoundRect(-fb.getWidth()/2, -fb.getHeight()/2,
                        fb.getWidth(), fb.getHeight(), 5, 5);

                gc.restore();
            }
        }
    }

    private void renderPowerUps() {
        for (PowerUp pu : model.getPowerUps()) {
            if (pu.isActive()) {
                double x = pu.getX();
                double y = pu.getY();
                int size = pu.getSize();

                // Glow effect
                gc.setFill(pu.getColor().deriveColor(0, 1, 1, 0.3));
                gc.fillOval(x - size/2 - 5, y - size/2 - 5, size + 10, size + 10);

                // Main circle
                gc.setFill(pu.getColor());
                gc.fillOval(x - size/2, y - size/2, size, size);

                // Symbol
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(pu.getSymbol(), x, y + 5);
            }
        }
    }

    private void renderPenalties() {
        for (Penalty pen : model.getPenalties()) {
            if (pen.isActive()) {
                double x = pen.getX();
                double y = pen.getY();
                int size = pen.getSize();

                // Warning glow
                gc.setFill(Color.rgb(255, 0, 0, 0.3 + Math.sin(animationTime * 10) * 0.2));
                gc.fillOval(x - size/2 - 5, y - size/2 - 5, size + 10, size + 10);

                // Main circle (hexagon-like)
                gc.setFill(pen.getColor());
                gc.fillOval(x - size/2, y - size/2, size, size);

                // Border
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeOval(x - size/2, y - size/2, size, size);

                // Symbol
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(pen.getSymbol(), x, y + 4);
            }
        }
    }

    private void renderPaddle() {
        Paddle paddle = model.getPaddle();
        double x = paddle.getX() - paddle.getWidth() / 2.0;
        double y = paddle.getY() - paddle.getHeight() / 2.0;

        // Base color based on effects
        Color baseColor = paddle.isReversed() ? Color.DARKVIOLET :
                paddle.isSticky() ? Color.PURPLE : Color.DODGERBLUE;

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.brighter()),
                new Stop(0.5, baseColor),
                new Stop(1, baseColor.darker())
        );

        gc.setFill(gradient);
        gc.fillRoundRect(x, y, paddle.getWidth(), paddle.getHeight(), 10, 10);

        // Highlight
        gc.setFill(baseColor.brighter().brighter());
        gc.setGlobalAlpha(0.5);
        gc.fillRoundRect(x + 5, y + 2, paddle.getWidth() - 10, 4, 3, 3);
        gc.setGlobalAlpha(1.0);

        // Border
        gc.setStroke(baseColor.darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, paddle.getWidth(), paddle.getHeight(), 10, 10);
    }

    private void renderBalls() {
        for (Ball ball : model.getBalls()) {
            double x = ball.getX();
            double y = ball.getY();
            int radius = ball.getRadius();

            // Trail effect for moving balls
            if (ball.isLaunched()) {
                gc.setFill(Color.rgb(255, 200, 100, 0.2));
                gc.fillOval(x - radius - ball.getDx() * 2, y - radius - ball.getDy() * 2,
                        radius * 2, radius * 2);
            }

            // Glow
            gc.setFill(Color.rgb(255, 200, 100, 0.3));
            gc.fillOval(x - radius - 3, y - radius - 3, radius * 2 + 6, radius * 2 + 6);

            // Main ball
            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.WHITE),
                    new Stop(0.5, ball.isClone() ? Color.CYAN : Color.rgb(255, 200, 100)),
                    new Stop(1, ball.isClone() ? Color.DARKCYAN : Color.rgb(255, 150, 50))
            );

            gc.setFill(gradient);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    // ==================== HUD ====================

    private void renderHUD() {
        PlayerProfile profile = model.getPlayerProfile();

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);

        // Player info
        gc.setFill(Color.WHITE);
        gc.fillText(profile.getPlayerName() + " - Level " + model.getLevel(), 20, 25);

        // Current score with multiplier
        String scoreText = "Score: " + model.getCurrentScore();
        if (model.getScoreMultiplier() > 1) {
            scoreText += " (x" + (int)model.getScoreMultiplier() + ")";
            gc.setFill(Color.GOLD);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText(scoreText, 20, 45);

        // Lives
        renderLivesAsHearts();

        // Active effects
        renderActiveEffects();

        // Controls hint
        gc.setFont(Font.font("Arial", 12));
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("P: Pause | M: Menu | SPACE: Launch", canvas.getWidth() / 2, canvas.getHeight() - 10);

        // Ball count if > 1
        if (model.getBalls().size() > 1) {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Balls: " + model.getBalls().size(), canvas.getWidth() / 2, 25);
        }
    }

    private void renderLivesAsHearts() {
        int lives = model.getLives();
        int maxLives = model.getMaxLives();
        double startX = canvas.getWidth() - 180;
        double y = 25;

        for (int i = 0; i < maxLives; i++) {
            if (i < lives) {
                drawHeart(startX + i * 30, y, 24, Color.RED, true);
            } else {
                drawHeart(startX + i * 30, y, 24, Color.DARKGRAY, false);
            }
        }
    }

    private void drawHeart(double x, double y, double size, Color color, boolean filled) {
        double[] xPoints = new double[20];
        double[] yPoints = new double[20];

        for (int i = 0; i < 20; i++) {
            double t = i * Math.PI * 2 / 20;
            xPoints[i] = x + size * 0.5 * (16 * Math.pow(Math.sin(t), 3)) / 16;
            yPoints[i] = y - size * 0.5 * (13 * Math.cos(t) - 5 * Math.cos(2*t)
                    - 2 * Math.cos(3*t) - Math.cos(4*t)) / 16;
        }

        if (filled) {
            gc.setFill(color);
            gc.fillPolygon(xPoints, yPoints, 20);
            gc.setFill(Color.rgb(255, 150, 150, 0.5));
            gc.fillOval(x - size * 0.25, y - size * 0.3, size * 0.3, size * 0.25);
        } else {
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokePolygon(xPoints, yPoints, 20);
        }
    }

    private void renderActiveEffects() {
        List<GameModel.ActiveEffect> effects = model.getActiveEffects();
        if (effects.isEmpty()) return;

        double y = 70;
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.LEFT);

        for (GameModel.ActiveEffect effect : effects) {
            Color bgColor = effect.type.equals("powerup") ?
                    Color.rgb(0, 100, 0, 0.7) : Color.rgb(100, 0, 0, 0.7);

            gc.setFill(bgColor);
            gc.fillRoundRect(15, y - 12, 130, 18, 5, 5);

            gc.setFill(Color.WHITE);
            gc.fillText(effect.name + " " + String.format("%.1fs", effect.remainingTime), 20, y);

            y += 22;
        }
    }

    // ==================== OVERLAYS ====================

    private void renderPauseOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        gc.fillText("PAUSED", canvas.getWidth() / 2, canvas.getHeight() / 2 - 50);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Press SPACE or P to Resume", canvas.getWidth() / 2, canvas.getHeight() / 2 + 20);
        gc.fillText("Press R to Restart Level", canvas.getWidth() / 2, canvas.getHeight() / 2 + 55);
        gc.fillText("Press M to Return to Menu", canvas.getWidth() / 2, canvas.getHeight() / 2 + 90);
    }

    private void renderGameOverOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText("GAME OVER", canvas.getWidth() / 2, canvas.getHeight() / 2 - 80);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.fillText("Level " + model.getLevel() + " Score: " + model.getCurrentScore(),
                canvas.getWidth() / 2, canvas.getHeight() / 2);

        PlayerProfile profile = model.getPlayerProfile();
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Best Score: " + profile.getLevelScore(model.getLevel()),
                canvas.getWidth() / 2, canvas.getHeight() / 2 + 50);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Press ENTER or R to Retry", canvas.getWidth() / 2, canvas.getHeight() / 2 + 110);
        gc.fillText("Press M to Return to Menu", canvas.getWidth() / 2, canvas.getHeight() / 2 + 140);
    }

    private void renderLevelCompleteOverlay() {
        gc.setFill(Color.rgb(0, 50, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.LIME);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.fillText("LEVEL " + model.getLevel() + " COMPLETE!",
                canvas.getWidth() / 2, canvas.getHeight() / 2 - 60);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        gc.fillText("Score: " + model.getCurrentScore(), canvas.getWidth() / 2, canvas.getHeight() / 2);

        String stars = getStarsForScore(model.getCurrentScore(), model.getLevel());
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 48));
        gc.fillText(stars, canvas.getWidth() / 2, canvas.getHeight() / 2 + 50);

        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Press ENTER for Level " + (model.getLevel() + 1),
                canvas.getWidth() / 2, canvas.getHeight() / 2 + 100);
        gc.fillText("Press M to Return to Menu", canvas.getWidth() / 2, canvas.getHeight() / 2 + 135);
    }

    private void renderVictoryScreen() {
        LinearGradient bg = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 40, 0)),
                new Stop(1, Color.rgb(20, 15, 0))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Animated stars
        gc.setFill(Color.GOLD);
        for (int i = 0; i < 30; i++) {
            double x = (i * 47 + animationTime * 20) % canvas.getWidth();
            double y = (i * 31 + Math.sin(animationTime + i) * 50) % canvas.getHeight();
            gc.fillText("★", x, y);
        }

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText("🏆 VICTORY! 🏆", canvas.getWidth() / 2, canvas.getHeight() / 2 - 100);

        PlayerProfile profile = model.getPlayerProfile();

        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 28));
        gc.fillText("Congratulations, " + profile.getPlayerName() + "!",
                canvas.getWidth() / 2, canvas.getHeight() / 2 - 40);
        gc.fillText("You completed all levels!", canvas.getWidth() / 2, canvas.getHeight() / 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.fillText("Total Score: " + profile.getTotalScore(),
                canvas.getWidth() / 2, canvas.getHeight() / 2 + 60);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Press ENTER or M to Return to Menu",
                canvas.getWidth() / 2, canvas.getHeight() / 2 + 120);
    }

    // ==================== DECORATIVE ====================

    private void renderDecorativeBricks(double y) {
        Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN};
        double startX = canvas.getWidth() / 2 - 175;

        for (int i = 0; i < 5; i++) {
            gc.setFill(colors[i]);
            gc.fillRoundRect(startX + i * 75, y, 65, 25, 5, 5);
            gc.setFill(colors[i].brighter());
            gc.fillRoundRect(startX + i * 75 + 2, y + 2, 61, 8, 3, 3);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }
}