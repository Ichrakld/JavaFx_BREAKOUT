package com.breakout.config;

/**
 * Singleton Configuration Class
 * Holds all game constants and settings
 *
 * Design Pattern: Singleton
 * - Ensures a single instance of configuration throughout the application
 * - Provides global access point to game settings
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
    private final double maxBallSpeed = 15.0;

    // Brick settings
    private final int brickRows = 5;
    private final int brickCols = 10;
    private final int brickWidth = 70;
    private final int brickHeight = 25;
    private final int brickPadding = 5;
    private final int brickOffsetTop = 50;
    private final int brickOffsetLeft = 35;

    // Game settings
    private final int initialLives = 3;
    private final int maxLives = 3;

    // Level settings
    private final int totalLevels = 5;

    // Power-up settings
    private final double powerUpDropChance = 0.25;  // 25% chance per brick
    private final double powerUpFallSpeed = 2.0;
    private final int powerUpSize = 25;
    private final double powerUpDuration = 10.0;    // seconds

    // Penalty settings
    private final double penaltyDropChance = 0.15;  // 15% chance per brick
    private final double fallingBrickSpeed = 1.5;
    private final double speedIncreaseMultiplier = 1.3;

    // Audio settings
    private boolean musicEnabled = true;
    private boolean soundEffectsEnabled = true;
    private double musicVolume = 0.5;
    private double sfxVolume = 0.7;

    // Database settings (MySQL)
    private final String dbHost = "localhost";
    private final int dbPort = 3306;
    private final String dbName = "breakout_game";
    private final String dbUser = "root";
    private final String dbPassword = "";  // Change this to your MySQL password

    private GameConfig() {}

    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    // Window Getters
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }

    // Paddle Getters
    public int getPaddleWidth() { return paddleWidth; }
    public int getPaddleHeight() { return paddleHeight; }
    public double getPaddleSpeed() { return paddleSpeed; }

    // Ball Getters
    public int getBallRadius() { return ballRadius; }
    public double getBallSpeed() { return ballSpeed; }
    public double getMaxBallSpeed() { return maxBallSpeed; }

    // Brick Getters
    public int getBrickRows() { return brickRows; }
    public int getBrickCols() { return brickCols; }
    public int getBrickWidth() { return brickWidth; }
    public int getBrickHeight() { return brickHeight; }
    public int getBrickPadding() { return brickPadding; }
    public int getBrickOffsetTop() { return brickOffsetTop; }
    public int getBrickOffsetLeft() { return brickOffsetLeft; }

    // Game Getters
    public int getInitialLives() { return initialLives; }
    public int getMaxLives() { return maxLives; }
    public int getTotalLevels() { return totalLevels; }

    // Power-up Getters
    public double getPowerUpDropChance() { return powerUpDropChance; }
    public double getPowerUpFallSpeed() { return powerUpFallSpeed; }
    public int getPowerUpSize() { return powerUpSize; }
    public double getPowerUpDuration() { return powerUpDuration; }

    // Penalty Getters
    public double getPenaltyDropChance() { return penaltyDropChance; }
    public double getFallingBrickSpeed() { return fallingBrickSpeed; }
    public double getSpeedIncreaseMultiplier() { return speedIncreaseMultiplier; }

    // Audio Getters/Setters
    public boolean isMusicEnabled() { return musicEnabled; }
    public void setMusicEnabled(boolean enabled) { this.musicEnabled = enabled; }
    public boolean isSoundEffectsEnabled() { return soundEffectsEnabled; }
    public void setSoundEffectsEnabled(boolean enabled) { this.soundEffectsEnabled = enabled; }
    public double getMusicVolume() { return musicVolume; }
    public void setMusicVolume(double volume) { this.musicVolume = Math.max(0, Math.min(1, volume)); }
    public double getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(double volume) { this.sfxVolume = Math.max(0, Math.min(1, volume)); }

    // Database Getters (MySQL)
    public String getDbHost() { return dbHost; }
    public int getDbPort() { return dbPort; }
    public String getDbName() { return dbName; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}