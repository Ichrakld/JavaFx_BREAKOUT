package com.breakout.model;

import com.breakout.config.GameConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Model - MVC Pattern
 * Contains all game logic and state
 * 
 * FEATURES:
 * - Player profile with name
 * - Level selection from menu
 * - Separate score tracking per level
 * - Fixed 3 hearts/lives
 */
public class GameModel {
    private GameConfig config;
    private GameState state;
    private GameState previousState;
    
    // Player profile
    private PlayerProfile playerProfile;
    private int selectedLevel;  // Currently selected level in menu
    
    // Game entities
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
    
    // Current game statistics
    private int currentScore;   // Score for current level attempt
    private int lives;
    private int level;          // Current playing level
    
    // Name input
    private StringBuilder nameInput;
    private final int maxNameLength = 15;
    
    public GameModel() {
        this.config = GameConfig.getInstance();
        this.state = GameState.NAME_INPUT;
        this.previousState = GameState.NAME_INPUT;
        this.playerProfile = new PlayerProfile();
        this.ball = new Ball();
        this.paddle = new Paddle();
        this.bricks = new ArrayList<>();
        this.selectedLevel = 1;
        this.nameInput = new StringBuilder();
    }
    
    // ==================== NAME INPUT ====================
    
    /**
     * Add character to name input
     */
    public void addCharToName(char c) {
        if (state == GameState.NAME_INPUT && nameInput.length() < maxNameLength) {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_') {
                nameInput.append(c);
            }
        }
    }
    
    /**
     * Remove last character from name input
     */
    public void removeCharFromName() {
        if (state == GameState.NAME_INPUT && nameInput.length() > 0) {
            nameInput.deleteCharAt(nameInput.length() - 1);
        }
    }
    
    /**
     * Confirm name and go to menu
     */
    public void confirmName() {
        if (state == GameState.NAME_INPUT) {
            String name = nameInput.toString().trim();
            if (name.isEmpty()) {
                name = "Player";
            }
            playerProfile.setPlayerName(name);
            state = GameState.MENU;
        }
    }
    
    /**
     * Get current name input
     */
    public String getNameInput() {
        return nameInput.toString();
    }
    
    // ==================== LEVEL SELECTION ====================
    
    /**
     * Select next level in menu
     */
    public void selectNextLevel() {
        if (state == GameState.MENU) {
            if (selectedLevel < config.getTotalLevels()) {
                selectedLevel++;
            }
        }
    }
    
    /**
     * Select previous level in menu
     */
    public void selectPreviousLevel() {
        if (state == GameState.MENU) {
            if (selectedLevel > 1) {
                selectedLevel--;
            }
        }
    }
    
    /**
     * Get currently selected level
     */
    public int getSelectedLevel() {
        return selectedLevel;
    }
    
    /**
     * Check if selected level can be played
     */
    public boolean canPlaySelectedLevel() {
        return playerProfile.isLevelUnlocked(selectedLevel);
    }
    
    // ==================== GAME INITIALIZATION ====================
    
    /**
     * Start playing the selected level
     */
    public void startSelectedLevel() {
        if (state == GameState.MENU && canPlaySelectedLevel()) {
            this.level = selectedLevel;
            this.currentScore = 0;
            this.lives = config.getInitialLives(); // Always 3 lives
            this.state = GameState.PLAYING;
            resetLevel();
        }
    }
    
    /**
     * Start a specific level
     */
    public void startLevel(int levelNum) {
        if (levelNum >= 1 && levelNum <= config.getTotalLevels() 
            && playerProfile.isLevelUnlocked(levelNum)) {
            this.level = levelNum;
            this.selectedLevel = levelNum;
            this.currentScore = 0;
            this.lives = config.getInitialLives();
            this.state = GameState.PLAYING;
            resetLevel();
        }
    }
    
    /**
     * Reset current level (ball, paddle, bricks)
     */
    public void resetLevel() {
        ball.reset();
        paddle.reset();
        createBricks();
    }
    
    /**
     * Create bricks for current level
     */
    private void createBricks() {
        bricks.clear();
        
        int baseRows = config.getBrickRows();
        int rows = baseRows + (level - 1); // More rows per level
        int cols = config.getBrickCols();
        int brickWidth = config.getBrickWidth();
        int brickHeight = config.getBrickHeight();
        int padding = config.getBrickPadding();
        int offsetTop = config.getBrickOffsetTop();
        int offsetLeft = config.getBrickOffsetLeft();
        
        // Cap rows to prevent overflow
        rows = Math.min(rows, 8);
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetLeft + col * (brickWidth + padding);
                double y = offsetTop + row * (brickHeight + padding);
                
                Brick.BrickType type = determineBrickType(row, level);
                bricks.add(new Brick(x, y, brickWidth, brickHeight, type));
            }
        }
    }
    
    /**
     * Determine brick type based on row position and level
     */
    private Brick.BrickType determineBrickType(int row, int level) {
        // Higher levels have more tough bricks
        if (row == 0 && level >= 3) {
            return Brick.BrickType.TOUGH;
        } else if (row <= 1 && level >= 2) {
            return Brick.BrickType.HARD;
        } else if (row == 0 && level >= 1) {
            return Brick.BrickType.HARD;
        }
        return Brick.BrickType.NORMAL;
    }
    
    // ==================== GAME UPDATE ====================
    
    /**
     * Main game update loop
     */
    public void update() {
        if (state != GameState.PLAYING) return;
        
        paddle.update();
        ball.update();
        
        if (!ball.isLaunched()) {
            ball.followPaddle(paddle.getX());
        }
        
        checkPaddleCollision();
        checkBrickCollisions();
        
        if (ball.isBelowScreen()) {
            loseLife();
        }
        
        if (isLevelComplete()) {
            completeLevel();
        }
    }
    
    private void checkPaddleCollision() {
        if (paddle.collidesWith(ball)) {
            ball.bounceOffPaddle(paddle.getX(), paddle.getWidth());
        }
    }
    
    private void checkBrickCollisions() {
        for (Brick brick : bricks) {
            if (brick.isActive() && brick.collidesWith(ball)) {
                String side = brick.getCollisionSide(ball);
                if (side.equals("left") || side.equals("right")) {
                    ball.reverseX();
                } else {
                    ball.reverseY();
                }
                
                int points = brick.hit();
                if (points > 0) {
                    currentScore += points * level;
                }
                
                break;
            }
        }
    }
    
    /**
     * Handle losing a life
     */
    private void loseLife() {
        lives--;
        
        if (lives <= 0) {
            lives = 0;
            // Save score even on game over
            playerProfile.updateLevelScore(level, currentScore);
            state = GameState.GAME_OVER;
        } else {
            ball.reset();
            paddle.reset();
        }
    }
    
    /**
     * Check if all breakable bricks are destroyed
     */
    private boolean isLevelComplete() {
        for (Brick brick : bricks) {
            if (brick.isActive() && brick.getType() != Brick.BrickType.UNBREAKABLE) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Handle level completion
     */
    private void completeLevel() {
        // Save score for this level
        playerProfile.updateLevelScore(level, currentScore);
        
        // Unlock next level
        if (level < config.getTotalLevels()) {
            playerProfile.unlockLevel(level + 1);
        }
        
        // Check if all levels completed
        if (level >= config.getTotalLevels()) {
            state = GameState.VICTORY;
        } else {
            state = GameState.LEVEL_COMPLETE;
        }
    }
    
    /**
     * Continue to next level after level complete screen
     */
    public void continueToNextLevel() {
        if (state == GameState.LEVEL_COMPLETE && level < config.getTotalLevels()) {
            level++;
            selectedLevel = level;
            currentScore = 0;
            lives = config.getInitialLives(); // Reset to 3 lives
            resetLevel();
            state = GameState.PLAYING;
        }
    }
    
    /**
     * Launch the ball
     */
    public void launchBall() {
        if (state == GameState.PLAYING && !ball.isLaunched()) {
            ball.launch();
        }
    }
    
    // ==================== PAUSE/RESUME ====================
    
    public void pauseGame() {
        if (state == GameState.PLAYING) {
            previousState = state;
            state = GameState.PAUSED;
        }
    }
    
    public void resumeGame() {
        if (state == GameState.PAUSED) {
            state = previousState;
        }
    }
    
    public void togglePause() {
        if (state == GameState.PLAYING) {
            pauseGame();
        } else if (state == GameState.PAUSED) {
            resumeGame();
        }
    }
    
    public boolean isPaused() {
        return state == GameState.PAUSED;
    }
    
    // ==================== MENU ====================
    
    /**
     * Return to main menu
     */
    public void returnToMenu() {
        // Save current score if playing
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            playerProfile.updateLevelScore(level, currentScore);
        }
        
        state = GameState.MENU;
        ball.reset();
        paddle.reset();
        bricks.clear();
    }
    
    /**
     * Restart current level
     */
    public void restartLevel() {
        currentScore = 0;
        lives = config.getInitialLives();
        resetLevel();
        state = GameState.PLAYING;
    }
    
    // ==================== GETTERS ====================
    
    public GameState getState() { return state; }
    public Ball getBall() { return ball; }
    public Paddle getPaddle() { return paddle; }
    public List<Brick> getBricks() { return bricks; }
    public int getCurrentScore() { return currentScore; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    public int getMaxLives() { return config.getMaxLives(); }
    public PlayerProfile getPlayerProfile() { return playerProfile; }
    public int getTotalLevels() { return config.getTotalLevels(); }
    
    // For compatibility
    public int getScore() { return currentScore; }
    public int getHighScore() { return playerProfile.getTotalScore(); }
    
    public void setState(GameState state) { this.state = state; }
}
