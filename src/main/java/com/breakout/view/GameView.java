package com.breakout.view;

import com.breakout.model.Ball;
import com.breakout.model.Brick;
import com.breakout.model.GameModel;
import com.breakout.model.GameState;
import com.breakout.model.Paddle;
import com.breakout.model.PlayerProfile;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Game View - MVC Pattern
 * Handles all rendering using JavaFX Canvas
 * 
 * SCREENS:
 * - Name Input: Enter player name
 * - Menu: Level selection with scores
 * - Playing: Game with 3 hearts
 * - Paused/GameOver/Victory overlays
 */
public class GameView extends StackPane {
    private Canvas canvas;
    private GraphicsContext gc;
    private GameModel model;
    
    // Animation
    private double animationTime = 0;
    
    public GameView(GameModel model, double width, double height) {
        this.model = model;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    /**
     * Main render method - called every frame
     */
    public void render() {
        animationTime += 0.016; // ~60 FPS
        
        // Clear screen
        gc.setFill(Color.rgb(20, 20, 40));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        GameState state = model.getState();
        
        switch (state) {
            case NAME_INPUT:
                renderNameInput();
                break;
            case MENU:
                renderMenu();
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
        }
    }
    
    // ==================== NAME INPUT SCREEN ====================
    
    private void renderNameInput() {
        // Gradient background
        LinearGradient bg = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(30, 30, 80)),
            new Stop(1, Color.rgb(10, 10, 40))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Title
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText("BREAKOUT", canvas.getWidth() / 2, 120);
        
        // Subtitle
        gc.setFill(Color.LIGHTBLUE);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Enhanced Edition", canvas.getWidth() / 2, 160);
        
        // Enter name prompt
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("Enter Your Name", canvas.getWidth() / 2, canvas.getHeight() / 2 - 60);
        
        // Name input box
        double boxWidth = 300;
        double boxHeight = 50;
        double boxX = (canvas.getWidth() - boxWidth) / 2;
        double boxY = canvas.getHeight() / 2 - 30;
        
        // Box background
        gc.setFill(Color.rgb(40, 40, 80));
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        // Box border (animated)
        double pulse = Math.sin(animationTime * 3) * 0.3 + 0.7;
        gc.setStroke(Color.rgb(100, 150, 255, pulse));
        gc.setLineWidth(3);
        gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        // Name text
        String nameText = model.getNameInput();
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(nameText + (System.currentTimeMillis() % 1000 < 500 ? "_" : ""), 
                    canvas.getWidth() / 2, boxY + 33);
        
        // Instructions
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Press ENTER to continue", canvas.getWidth() / 2, canvas.getHeight() / 2 + 80);
        
        // Decorative bricks
        renderDecorativeBricks(canvas.getHeight() - 150);
    }
    
    // ==================== MENU WITH LEVEL SELECTION ====================
    
    private void renderMenu() {
        // Gradient background
        LinearGradient bg = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(20, 20, 60)),
            new Stop(1, Color.rgb(10, 10, 30))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        PlayerProfile profile = model.getPlayerProfile();
        
        // Welcome message with player name
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.fillText("Welcome, " + profile.getPlayerName() + "!", canvas.getWidth() / 2, 60);
        
        // Title
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText("SELECT LEVEL", canvas.getWidth() / 2, 120);
        // Total score
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText("Total Score: " + profile.getTotalScore(),
                canvas.getWidth() / 2, 170);
        // Level buttons
        int totalLevels = model.getTotalLevels();
        int selectedLevel = model.getSelectedLevel();
        double startY = 180;
        double levelHeight = 70;
        
        for (int i = 1; i <= totalLevels; i++) {
            double y = startY + (i - 1) * levelHeight;
            boolean unlocked = profile.isLevelUnlocked(i);
            boolean selected = (i == selectedLevel);
            int levelScore = profile.getLevelScore(i);
            
            renderLevelButton(i, y, unlocked, selected, levelScore);
        }
        
        // Controls
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("↑↓ Select Level  |  ENTER Start  |  1-5 Quick Select", 
                    canvas.getWidth() / 2, canvas.getHeight() - 40);
    }
    
    /**
     * Render a single level button
     */
    private void renderLevelButton(int level, double y, boolean unlocked, 
                                    boolean selected, int score) {
        double buttonWidth = 500;
        double buttonHeight = 55;
        double x = (canvas.getWidth() - buttonWidth) / 2;
        
        // Button background
        if (selected && unlocked) {
            // Selected - highlighted
            gc.setFill(Color.rgb(50, 80, 150));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(3);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        } else if (unlocked) {
            // Unlocked but not selected
            gc.setFill(Color.rgb(40, 50, 80));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(Color.rgb(80, 100, 150));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        } else {
            // Locked
            gc.setFill(Color.rgb(30, 30, 40));
            gc.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            gc.setStroke(Color.rgb(60, 60, 70));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
        }
        
        // Level number and stars/lock
        gc.setTextAlign(TextAlignment.LEFT);
        if (unlocked) {
            gc.setFill(selected ? Color.WHITE : Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.fillText("Level " + level, x + 20, y + 35);
            
            // Stars based on score
            String stars = getStarsForScore(score, level);
            gc.setFill(Color.GOLD);
            gc.fillText(stars, x + 150, y + 35);
            
            // Best score
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(selected ? Color.YELLOW : Color.LIGHTBLUE);
            gc.setFont(Font.font("Arial", 18));
            gc.fillText("Best: " + score, x + buttonWidth - 20, y + 35);
        } else {
            // Locked level
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.fillText("Level " + level, x + 20, y + 35);
            
            // Lock icon
            gc.fillText("🔒 LOCKED", x + 150, y + 35);
        }
        
        gc.setTextAlign(TextAlignment.CENTER);
    }
    
    /**
     * Get star rating based on score
     */
    private String getStarsForScore(int score, int level) {
        // Return 5 empty stars if score is 0
        if (score == 0) return "☆☆☆☆☆";

        // Define thresholds based on level
        // You can adjust these multipliers (100, 200, etc.) to balance your game difficulty
        int threshold1 = level * 100;
        int threshold2 = level * 200;
        int threshold3 = level * 300;
        int threshold4 = level * 400;
        int threshold5 = level * 500;

        // Check from highest to lowest
        if (score >= threshold5) return "★★★★★";
        if (score >= threshold4) return "★★★★☆";
        if (score >= threshold3) return "★★★☆☆";
        if (score >= threshold2) return "★★☆☆☆";
        if (score >= threshold1) return "★☆☆☆☆";

        // Default if score is positive but below threshold1
        return "☆☆☆☆☆";
    }
    
    // ==================== GAME RENDERING ====================
    
    private void renderGame() {
        renderBricks();
        renderPaddle();
        renderBall();
    }
    
    private void renderBricks() {
        for (Brick brick : model.getBricks()) {
            if (brick.isActive()) {
                Color baseColor = brick.getColor();
                gc.setFill(baseColor);
                gc.fillRoundRect(brick.getX(), brick.getY(), 
                                 brick.getWidth(), brick.getHeight(), 5, 5);
                
                gc.setFill(baseColor.brighter());
                gc.fillRoundRect(brick.getX() + 2, brick.getY() + 2, 
                                 brick.getWidth() - 4, brick.getHeight() / 3, 3, 3);
                
                gc.setStroke(baseColor.darker());
                gc.setLineWidth(1);
                gc.strokeRoundRect(brick.getX(), brick.getY(), 
                                   brick.getWidth(), brick.getHeight(), 5, 5);
            }
        }
    }
    
    private void renderPaddle() {
        Paddle paddle = model.getPaddle();
        double x = paddle.getX() - paddle.getWidth() / 2.0;
        double y = paddle.getY() - paddle.getHeight() / 2.0;
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(100, 150, 255)),
            new Stop(0.5, Color.rgb(50, 100, 200)),
            new Stop(1, Color.rgb(30, 60, 150))
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, paddle.getWidth(), paddle.getHeight(), 10, 10);
        
        gc.setFill(Color.rgb(150, 200, 255, 0.5));
        gc.fillRoundRect(x + 5, y + 2, paddle.getWidth() - 10, 4, 3, 3);
        
        gc.setStroke(Color.rgb(80, 130, 220));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, paddle.getWidth(), paddle.getHeight(), 10, 10);
    }
    
    private void renderBall() {
        Ball ball = model.getBall();
        double x = ball.getX();
        double y = ball.getY();
        int radius = ball.getRadius();
        
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE),
            new Stop(0.5, Color.rgb(255, 200, 100)),
            new Stop(1, Color.rgb(255, 150, 50))
        );
        
        gc.setFill(gradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        gc.setFill(Color.rgb(255, 200, 100, 0.3));
        gc.fillOval(x - radius - 3, y - radius - 3, radius * 2 + 6, radius * 2 + 6);
    }
    
    // ==================== HUD ====================
    
    private void renderHUD() {
        PlayerProfile profile = model.getPlayerProfile();
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        
        // Player name and level
        gc.fillText(profile.getPlayerName() + " - Level " + model.getLevel(), 20, 25);
        
        // Current score
        gc.fillText("Score: " + model.getCurrentScore(), 20, 45);
        
        // Best score for this level
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 14));
        int bestScore = profile.getLevelScore(model.getLevel());
        gc.fillText("Best: " + bestScore, 150, 45);
        
        // Lives as 3 hearts (fixed)
        renderLivesAsHearts();
        
        // Controls hint
        gc.setFont(Font.font("Arial", 12));
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("P: Pause | M: Menu | SPACE: Launch", canvas.getWidth() / 2, canvas.getHeight() - 10);
    }
    
    /**
     * Render exactly 3 hearts for lives
     */
    private void renderLivesAsHearts() {
        int lives = model.getLives();
        int maxLives = 5; // Always 3 hearts
        double startX = canvas.getWidth() - 200;
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
        int bestScore = profile.getLevelScore(model.getLevel());
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("Best Score: " + bestScore, canvas.getWidth() / 2, canvas.getHeight() / 2 + 50);
        
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
        
        // Stars
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
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.fillText("VICTORY!", canvas.getWidth() / 2, canvas.getHeight() / 2 - 100);
        
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
    
    // ==================== DECORATIVE ELEMENTS ====================
    
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
