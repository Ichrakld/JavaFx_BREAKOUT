package com.breakout.model;

import com.breakout.audio.AudioManager;
import com.breakout.config.GameConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Game Model - MVC Pattern
 * Contains all game logic and state
 *
 * Design Patterns Used:
 * - MVC: Model component holding game state
 * - Observer: State changes notify view
 * - State: GameState enum controls behavior
 *
 * Features v3.0:
 * - Power-ups system
 * - Penalties system
 * - Multiple balls support
 * - Falling bricks hazard
 * - Database integration
 * - Audio integration
 */
public class GameModel {
    private GameConfig config;
    private GameState state;
    private GameState previousState;
    private AudioManager audio;

    // Player profile
    private PlayerProfile playerProfile;
    private int selectedLevel;

    // Game entities
    private List<Ball> balls;
    private Paddle paddle;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<Penalty> penalties;
    private List<FallingBrick> fallingBricks;

    // Active effects
    private List<ActiveEffect> activeEffects;

    // Game statistics
    private int currentScore;
    private int lives;
    private int level;
    private double scoreMultiplier;
    private boolean hasShield;

    // Name input
    private StringBuilder nameInput;
    private final int maxNameLength = 15;

    public static class ActiveEffect {
        public final String name;
        public final String type;
        public double remainingTime;

        public ActiveEffect(String name, String type, double duration) {
            this.name = name;
            this.type = type;
            this.remainingTime = duration;
        }
    }

    public GameModel() {
        this.config = GameConfig.getInstance();
        this.state = GameState.NAME_INPUT;
        this.previousState = GameState.NAME_INPUT;
        this.audio = AudioManager.getInstance();
        this.playerProfile = new PlayerProfile();
        this.balls = new ArrayList<>();
        this.balls.add(new Ball());
        this.paddle = new Paddle();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.penalties = new ArrayList<>();
        this.fallingBricks = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.selectedLevel = 1;
        this.nameInput = new StringBuilder();
        this.scoreMultiplier = 1.0;
        this.hasShield = false;
    }

    // ==================== NAME INPUT ====================

    public void addCharToName(char c) {
        if (state == GameState.NAME_INPUT && nameInput.length() < maxNameLength) {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_') {
                nameInput.append(c);
            }
        }
    }

    public void removeCharFromName() {
        if (state == GameState.NAME_INPUT && nameInput.length() > 0) {
            nameInput.deleteCharAt(nameInput.length() - 1);
        }
    }

    public void confirmName() {
        if (state == GameState.NAME_INPUT) {
            String name = nameInput.toString().trim();
            if (name.isEmpty()) name = "Player";
            playerProfile.setPlayerName(name);
            state = GameState.MENU;
            audio.playMusic(AudioManager.MusicTrack.MENU);
        }
    }

    public String getNameInput() { return nameInput.toString(); }

    // ==================== LEVEL SELECTION ====================

    public void selectNextLevel() {
        if (state == GameState.MENU && selectedLevel < config.getTotalLevels()) {
            selectedLevel++;
            audio.playSfx(AudioManager.SoundEffect.MENU_SELECT);
        }
    }

    public void selectPreviousLevel() {
        if (state == GameState.MENU && selectedLevel > 1) {
            selectedLevel--;
            audio.playSfx(AudioManager.SoundEffect.MENU_SELECT);
        }
    }

    public int getSelectedLevel() { return selectedLevel; }
    public boolean canPlaySelectedLevel() { return playerProfile.isLevelUnlocked(selectedLevel); }

    // ==================== GAME INITIALIZATION ====================

    public void startSelectedLevel() {
        if (state == GameState.MENU && canPlaySelectedLevel()) {
            startLevel(selectedLevel);
            audio.playSfx(AudioManager.SoundEffect.MENU_CONFIRM);
        }
    }

    public void startLevel(int levelNum) {
        if (levelNum >= 1 && levelNum <= config.getTotalLevels()
                && playerProfile.isLevelUnlocked(levelNum)) {
            this.level = levelNum;
            this.selectedLevel = levelNum;
            this.currentScore = 0;
            this.lives = config.getInitialLives();
            this.scoreMultiplier = 1.0;
            this.hasShield = false;
            this.state = GameState.PLAYING;
            resetLevel();
            audio.playLevelMusic(level);
        }
    }

    public void resetLevel() {
        balls.clear();
        balls.add(new Ball());
        paddle.reset();
        powerUps.clear();
        penalties.clear();
        fallingBricks.clear();
        activeEffects.clear();
        scoreMultiplier = 1.0;
        hasShield = false;
        createBricks();
    }

    private void createBricks() {
        bricks.clear();
        int baseRows = config.getBrickRows();
        int rows = Math.min(baseRows + (level - 1), 8);
        int cols = config.getBrickCols();
        int brickWidth = config.getBrickWidth();
        int brickHeight = config.getBrickHeight();
        int padding = config.getBrickPadding();
        int offsetTop = config.getBrickOffsetTop();
        int offsetLeft = config.getBrickOffsetLeft();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetLeft + col * (brickWidth + padding);
                double y = offsetTop + row * (brickHeight + padding);

                Brick.BrickType type = determineBrickType(row, level);
                Brick brick = new Brick(x, y, brickWidth, brickHeight, type);

                if (type == Brick.BrickType.NORMAL || type == Brick.BrickType.HARD) {
                    if (Math.random() < config.getPowerUpDropChance()) {
                        brick.setPowerUp(true);
                    } else if (Math.random() < config.getPenaltyDropChance()) {
                        brick.setPenalty(true);
                    }
                }
                bricks.add(brick);
            }
        }
    }

    private Brick.BrickType determineBrickType(int row, int level) {
        double rand = Math.random();
        if (row == 0 && level >= 4 && rand < 0.2) return Brick.BrickType.GOLD;
        if (row == 0 && level >= 3) return Brick.BrickType.TOUGH;
        if (row <= 1 && level >= 2) return Brick.BrickType.HARD;
        if (rand < 0.05 && level >= 2) return Brick.BrickType.POWER;
        if (rand < 0.03 && level >= 3) return Brick.BrickType.PENALTY;
        if (row == 0 && level >= 1) return Brick.BrickType.HARD;
        return Brick.BrickType.NORMAL;
    }

    // ==================== GAME UPDATE ====================

    public void update() {
        if (state != GameState.PLAYING) return;

        double deltaTime = 0.016;
        paddle.update();
        updateBalls();
        updatePowerUps();
        updatePenalties();
        updateFallingBricks();
        updateActiveEffects(deltaTime);

        if (isLevelComplete()) completeLevel();
    }

    private void updateBalls() {
        Iterator<Ball> it = balls.iterator();
        List<Ball> ballsToRemove = new ArrayList<>();

        while (it.hasNext()) {
            Ball ball = it.next();
            ball.update();

            if (!ball.isLaunched()) ball.followPaddle(paddle.getX());

            checkPaddleCollision(ball);
            checkBrickCollisions(ball);
            checkFallingBrickCollisions(ball);

            if (ball.isBelowScreen()) {
                if (hasShield && !ball.isClone()) {
                    ball.setY(config.getWindowHeight() - 100);
                    ball.reverseY();
                    hasShield = false;
                    removeActiveEffect("Bottom Shield");
                } else if (ball.isClone()) {
                    ballsToRemove.add(ball);
                } else if (balls.size() > 1) {
                    ballsToRemove.add(ball);
                } else {
                    loseLife();
                    return;
                }
            }
        }
        balls.removeAll(ballsToRemove);

        if (balls.isEmpty()) {
            balls.add(new Ball());
            loseLife();
        }
    }

    private void checkPaddleCollision(Ball ball) {
        if (paddle.collidesWith(ball)) {
            if (paddle.isSticky() && !paddle.hasBallStuck()) {
                paddle.stickBall(ball);
            } else {
                ball.bounceOffPaddle(paddle.getX(), paddle.getWidth());
            }
            audio.playPaddleHit();
        }
    }

    private void checkBrickCollisions(Ball ball) {
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
                    currentScore += (int)(points * level * scoreMultiplier);
                    audio.playBrickDestroy();

                    // Spawn power-up or penalty
                    if (brick.hasPowerUp() || brick.getType() == Brick.BrickType.POWER) {
                        spawnPowerUp(brick.getCenterX(), brick.getCenterY());
                    } else if (brick.hasPenalty() || brick.getType() == Brick.BrickType.PENALTY) {
                        spawnPenalty(brick.getCenterX(), brick.getCenterY());
                    }
                } else {
                    audio.playBrickHit();
                }
                break;
            }
        }
    }

    private void checkFallingBrickCollisions(Ball ball) {
        Iterator<FallingBrick> it = fallingBricks.iterator();
        while (it.hasNext()) {
            FallingBrick fb = it.next();
            if (fb.isActive() && fb.collidesWithBall(ball)) {
                if (fb.hit()) {
                    currentScore += 25;
                    audio.playBrickDestroy();
                }
                ball.reverseY();
            }
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp pu = it.next();
            pu.update();

            if (pu.isActive() && pu.collidesWith(paddle)) {
                applyPowerUp(pu);
                pu.collect();
                audio.playPowerUp();
            }

            if (!pu.isActive()) it.remove();
        }
    }

    private void updatePenalties() {
        Iterator<Penalty> it = penalties.iterator();
        while (it.hasNext()) {
            Penalty pen = it.next();
            pen.update();

            if (pen.isActive() && pen.collidesWith(paddle)) {
                applyPenalty(pen);
                pen.collect();
                audio.playPenalty();
            }

            if (!pen.isActive()) it.remove();
        }
    }

    private void updateFallingBricks() {
        Iterator<FallingBrick> it = fallingBricks.iterator();
        while (it.hasNext()) {
            FallingBrick fb = it.next();
            fb.update();

            if (fb.isActive() && fb.collidesWithPaddle(paddle)) {
                fb.destroy();
                loseLife();
                audio.playLifeLost();
                return;
            }

            if (!fb.isActive()) it.remove();
        }
    }

    private void updateActiveEffects(double deltaTime) {
        Iterator<ActiveEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            ActiveEffect effect = it.next();
            effect.remainingTime -= deltaTime;

            if (effect.remainingTime <= 0) {
                expireEffect(effect);
                it.remove();
            }
        }
    }

    // ==================== POWER-UPS ====================

    private void spawnPowerUp(double x, double y) {
        powerUps.add(PowerUp.createRandom(x, y));
    }

    private void applyPowerUp(PowerUp pu) {
        PowerUp.PowerUpType type = pu.getType();

        switch (type) {
            case EXTEND_PADDLE:
                paddle.extend();
                addActiveEffect("Extend Paddle", "powerup", type.getDuration());
                break;
            case MULTI_BALL:
                if (!balls.isEmpty()) {
                    Ball original = balls.get(0);
                    if (original.isLaunched()) {
                        balls.add(new Ball(original));
                        balls.add(new Ball(original));
                    }
                }
                break;
            case SLOW_BALL:
                for (Ball b : balls) b.decreaseSpeed(1.3);
                addActiveEffect("Slow Ball", "powerup", type.getDuration());
                break;
            case EXTRA_LIFE:
                if (lives < config.getMaxLives()) lives++;
                break;
            case SCORE_BOOST:
                scoreMultiplier = 2.0;
                addActiveEffect("2x Score", "powerup", type.getDuration());
                break;
            case STICKY_PADDLE:
                paddle.setSticky(true);
                addActiveEffect("Sticky Paddle", "powerup", type.getDuration());
                break;
            case SHIELD:
                hasShield = true;
                addActiveEffect("Bottom Shield", "powerup", type.getDuration());
                break;
        }
    }

    // ==================== PENALTIES ====================

    private void spawnPenalty(double x, double y) {
        penalties.add(Penalty.createRandom(x, y));
    }

    private void applyPenalty(Penalty pen) {
        Penalty.PenaltyType type = pen.getType();

        switch (type) {
            case SPEED_UP:
                for (Ball b : balls) b.increaseSpeed(config.getSpeedIncreaseMultiplier());
                addActiveEffect("Speed Up!", "penalty", type.getDuration());
                break;
            case SHRINK_PADDLE:
                paddle.shrink();
                addActiveEffect("Shrink Paddle", "penalty", type.getDuration());
                break;
            case DOUBLE_BALL:
                if (!balls.isEmpty()) {
                    Ball original = balls.get(0);
                    if (original.isLaunched()) {
                        balls.add(new Ball(original));
                    }
                }
                break;
            case FALLING_BRICK:
                fallingBricks.add(FallingBrick.createRandom());
                break;
            case REVERSE_CONTROLS:
                paddle.setReversed(true);
                addActiveEffect("Reversed!", "penalty", type.getDuration());
                break;
            case BLIND_ZONE:
                addActiveEffect("Blind Zone", "penalty", type.getDuration());
                break;
        }
    }

    // ==================== EFFECTS MANAGEMENT ====================

    private void addActiveEffect(String name, String type, double duration) {
        removeActiveEffect(name);
        activeEffects.add(new ActiveEffect(name, type, duration));
    }

    private void removeActiveEffect(String name) {
        activeEffects.removeIf(e -> e.name.equals(name));
    }

    private void expireEffect(ActiveEffect effect) {
        switch (effect.name) {
            case "Extend Paddle":
            case "Shrink Paddle":
                paddle.resetSize();
                break;
            case "Slow Ball":
                for (Ball b : balls) b.increaseSpeed(1.3);
                break;
            case "Speed Up!":
                for (Ball b : balls) b.decreaseSpeed(config.getSpeedIncreaseMultiplier());
                break;
            case "2x Score":
                scoreMultiplier = 1.0;
                break;
            case "Sticky Paddle":
                paddle.setSticky(false);
                break;
            case "Reversed!":
                paddle.setReversed(false);
                break;
            case "Bottom Shield":
                hasShield = false;
                break;
        }
    }

    public List<ActiveEffect> getActiveEffects() { return activeEffects; }
    public boolean hasBlindZone() {
        return activeEffects.stream().anyMatch(e -> e.name.equals("Blind Zone"));
    }

    // ==================== GAME FLOW ====================

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            lives = 0;
            playerProfile.updateLevelScore(level, currentScore);
            state = GameState.GAME_OVER;
            audio.playGameOver();
        } else {
            audio.playLifeLost();
            balls.clear();
            balls.add(new Ball());
            paddle.reset();
            powerUps.clear();
            penalties.clear();
            activeEffects.clear();
            paddle.resetSize();
            paddle.setReversed(false);
            paddle.setSticky(false);
            scoreMultiplier = 1.0;
        }
    }

    private boolean isLevelComplete() {
        for (Brick brick : bricks) {
            if (brick.isActive() && brick.getType() != Brick.BrickType.UNBREAKABLE) {
                return false;
            }
        }
        return true;
    }

    private void completeLevel() {
        playerProfile.updateLevelScore(level, currentScore);
        if (level < config.getTotalLevels()) {
            playerProfile.unlockLevel(level + 1);
        }

        if (level >= config.getTotalLevels()) {
            state = GameState.VICTORY;
            audio.playVictory();
        } else {
            state = GameState.LEVEL_COMPLETE;
            audio.playLevelComplete();
        }
    }

    public void continueToNextLevel() {
        if (state == GameState.LEVEL_COMPLETE && level < config.getTotalLevels()) {
            level++;
            selectedLevel = level;
            currentScore = 0;
            lives = config.getInitialLives();
            resetLevel();
            state = GameState.PLAYING;
            audio.playLevelMusic(level);
        }
    }

    public void launchBall() {
        if (state == GameState.PLAYING) {
            if (paddle.hasBallStuck()) {
                paddle.releaseStuckBall();
            } else {
                for (Ball ball : balls) {
                    if (!ball.isLaunched()) {
                        ball.launch();
                        audio.playSfx(AudioManager.SoundEffect.BALL_LAUNCH);
                        break;
                    }
                }
            }
        }
    }

    // ==================== PAUSE/RESUME ====================

    public void pauseGame() {
        if (state == GameState.PLAYING) {
            previousState = state;
            state = GameState.PAUSED;
            audio.pauseMusic();
        }
    }

    public void resumeGame() {
        if (state == GameState.PAUSED) {
            state = previousState;
            audio.resumeMusic();
        }
    }

    public void togglePause() {
        if (state == GameState.PLAYING) pauseGame();
        else if (state == GameState.PAUSED) resumeGame();
    }

    public boolean isPaused() { return state == GameState.PAUSED; }

    // ==================== MENU ====================

    public void returnToMenu() {
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            playerProfile.updateLevelScore(level, currentScore);
        }
        state = GameState.MENU;
        balls.clear();
        balls.add(new Ball());
        paddle.reset();
        bricks.clear();
        powerUps.clear();
        penalties.clear();
        fallingBricks.clear();
        activeEffects.clear();
        audio.playMusic(AudioManager.MusicTrack.MENU);
    }

    public void restartLevel() {
        currentScore = 0;
        lives = config.getInitialLives();
        resetLevel();
        state = GameState.PLAYING;
        audio.playLevelMusic(level);
    }

    public void showLeaderboard() {
        if (state == GameState.MENU) {
            previousState = state;
            state = GameState.LEADERBOARD;
        }
    }

    public void hideLeaderboard() {
        if (state == GameState.LEADERBOARD) {
            state = GameState.MENU;
        }
    }

    // ==================== GETTERS ====================

    public GameState getState() { return state; }
    public Ball getBall() { return balls.isEmpty() ? null : balls.get(0); }
    public List<Ball> getBalls() { return balls; }
    public Paddle getPaddle() { return paddle; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<Penalty> getPenalties() { return penalties; }
    public List<FallingBrick> getFallingBricks() { return fallingBricks; }
    public int getCurrentScore() { return currentScore; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    public int getMaxLives() { return config.getMaxLives(); }
    public PlayerProfile getPlayerProfile() { return playerProfile; }
    public int getTotalLevels() { return config.getTotalLevels(); }
    public int getScore() { return currentScore; }
    public int getHighScore() { return playerProfile.getTotalScore(); }
    public double getScoreMultiplier() { return scoreMultiplier; }
    public boolean hasShield() { return hasShield; }

    public void setState(GameState state) { this.state = state; }
}