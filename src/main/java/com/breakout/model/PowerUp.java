package com.breakout.model;

import com.breakout.config.GameConfig;
import javafx.scene.paint.Color;

/**
 * PowerUp Entity Class
 * Represents collectible power-ups that fall from destroyed bricks
 *
 * Design Pattern: Strategy Pattern (via PowerUpType enum)
 * - Each power-up type defines its own effect
 * - Effects are applied through the GameModel
 *
 * Power-up Types:
 * - EXTEND_PADDLE: Makes paddle wider
 * - MULTI_BALL: Adds extra ball
 * - SLOW_BALL: Reduces ball speed
 * - EXTRA_LIFE: Grants additional life
 * - SCORE_BOOST: Double points temporarily
 * - STICKY_PADDLE: Ball sticks to paddle
 */
public class PowerUp {
    private double x, y;
    private int size;
    private double fallSpeed;
    private boolean active;
    private PowerUpType type;
    private double remainingDuration;  // For timed power-ups
    private GameConfig config;

    /**
     * Power-up types with their effects
     */
    public enum PowerUpType {
        EXTEND_PADDLE("E", Color.LIMEGREEN, "Extend Paddle", true, 10.0),
        MULTI_BALL("M", Color.CYAN, "Multi-Ball", false, 0),
        SLOW_BALL("S", Color.LIGHTBLUE, "Slow Ball", true, 8.0),
        EXTRA_LIFE("♥", Color.RED, "Extra Life", false, 0),
        SCORE_BOOST("2x", Color.GOLD, "Double Points", true, 15.0),
        STICKY_PADDLE("▬", Color.PURPLE, "Sticky Paddle", true, 12.0),
        SHIELD("◊", Color.ORANGE, "Bottom Shield", true, 20.0);

        private final String symbol;
        private final Color color;
        private final String description;
        private final boolean timed;
        private final double duration;

        PowerUpType(String symbol, Color color, String description, boolean timed, double duration) {
            this.symbol = symbol;
            this.color = color;
            this.description = description;
            this.timed = timed;
            this.duration = duration;
        }

        public String getSymbol() { return symbol; }
        public Color getColor() { return color; }
        public String getDescription() { return description; }
        public boolean isTimed() { return timed; }
        public double getDuration() { return duration; }

        /**
         * Get random power-up type
         */
        public static PowerUpType random() {
            PowerUpType[] types = values();
            return types[(int)(Math.random() * types.length)];
        }

        /**
         * Get weighted random power-up (rarer = less likely)
         */
        public static PowerUpType weightedRandom() {
            double rand = Math.random();
            if (rand < 0.25) return EXTEND_PADDLE;      // 25%
            else if (rand < 0.45) return SLOW_BALL;     // 20%
            else if (rand < 0.60) return SCORE_BOOST;   // 15%
            else if (rand < 0.75) return MULTI_BALL;    // 15%
            else if (rand < 0.85) return STICKY_PADDLE; // 10%
            else if (rand < 0.93) return SHIELD;        // 8%
            else return EXTRA_LIFE;                     // 7%
        }
    }

    public PowerUp(double x, double y, PowerUpType type) {
        this.config = GameConfig.getInstance();
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = config.getPowerUpSize();
        this.fallSpeed = config.getPowerUpFallSpeed();
        this.active = true;
        this.remainingDuration = type.getDuration();
    }

    /**
     * Create random power-up at position
     */
    public static PowerUp createRandom(double x, double y) {
        return new PowerUp(x, y, PowerUpType.weightedRandom());
    }

    /**
     * Update power-up position (falling)
     */
    public void update() {
        if (!active) return;
        y += fallSpeed;

        // Deactivate if falls below screen
        if (y > GameConfig.getInstance().getWindowHeight() + size) {
            active = false;
        }
    }

    /**
     * Update remaining duration for timed power-ups
     * @return true if still active, false if expired
     */
    public boolean updateDuration(double deltaTime) {
        if (!type.isTimed()) return true;

        remainingDuration -= deltaTime;
        return remainingDuration > 0;
    }

    /**
     * Check collision with paddle
     */
    public boolean collidesWith(Paddle paddle) {
        if (!active) return false;

        double paddleLeft = paddle.getX() - paddle.getWidth() / 2.0;
        double paddleRight = paddle.getX() + paddle.getWidth() / 2.0;
        double paddleTop = paddle.getY() - paddle.getHeight() / 2.0;
        double paddleBottom = paddle.getY() + paddle.getHeight() / 2.0;

        double powerUpLeft = x - size / 2.0;
        double powerUpRight = x + size / 2.0;
        double powerUpTop = y - size / 2.0;
        double powerUpBottom = y + size / 2.0;

        return powerUpRight > paddleLeft && powerUpLeft < paddleRight &&
                powerUpBottom > paddleTop && powerUpTop < paddleBottom;
    }

    /**
     * Collect power-up
     */
    public void collect() {
        active = false;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public boolean isActive() { return active; }
    public PowerUpType getType() { return type; }
    public double getRemainingDuration() { return remainingDuration; }
    public Color getColor() { return type.getColor(); }
    public String getSymbol() { return type.getSymbol(); }
}