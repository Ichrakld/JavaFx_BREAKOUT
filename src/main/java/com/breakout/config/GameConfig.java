package com.breakout.config;

/**
 * Singleton Configuration Class
 * Holds all game constants and settings
 */
public class GameConfig {
    private static GameConfig instance;
    
    // Window settings
    private final int windowWidth = 800;
    private final int windowHeight = 600;
    
    // Paddle settings
    private final int paddleWidth = 100;
    private final int paddleHeight = 15;
    private final double paddleSpeed = 8.0;
    
    // Ball settings
    private final int ballRadius = 10;
    private final double ballSpeed = 5.0;
    
    // Brick settings
    private final int brickRows = 5;
    private final int brickCols = 10;
    private final int brickWidth = 70;
    private final int brickHeight = 25;
    private final int brickPadding = 5;
    private final int brickOffsetTop = 50;
    private final int brickOffsetLeft = 35;
    
    // Game settings
    private final int initialLives = 5;
    private final int maxLives = 5;  // Fixed to 3 hearts only
    
    // Level settings
    private final int totalLevels = 5;
    
    private GameConfig() {}
    
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    // Getters
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public int getPaddleWidth() { return paddleWidth; }
    public int getPaddleHeight() { return paddleHeight; }
    public double getPaddleSpeed() { return paddleSpeed; }
    public int getBallRadius() { return ballRadius; }
    public double getBallSpeed() { return ballSpeed; }
    public int getBrickRows() { return brickRows; }
    public int getBrickCols() { return brickCols; }
    public int getBrickWidth() { return brickWidth; }
    public int getBrickHeight() { return brickHeight; }
    public int getBrickPadding() { return brickPadding; }
    public int getBrickOffsetTop() { return brickOffsetTop; }
    public int getBrickOffsetLeft() { return brickOffsetLeft; }
    public int getInitialLives() { return initialLives; }
    public int getMaxLives() { return maxLives; }
    public int getTotalLevels() { return totalLevels; }
}
