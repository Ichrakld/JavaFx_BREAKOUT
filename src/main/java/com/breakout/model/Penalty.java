package com.breakout.model;

import com.breakout.config.GameConfig;
import javafx.scene.paint.Color;

/**
 * Penalty Entity Class
 * Represents negative power-ups that fall from certain bricks
 *
 * Design Pattern: Strategy Pattern (via PenaltyType enum)
 * - Each penalty type defines its negative effect
 * - Effects are applied through the GameModel
 *
 * Penalty Types:
 * - SPEED_UP: Increases ball speed
 * - SHRINK_PADDLE: Makes paddle smaller
 * - MULTI_BALL: Doubles balls (harder to track)
 * - FALLING_BRICK: A brick falls that can hit paddle
 * - REVERSE_CONTROLS: Inverts paddle controls
 */
public class Penalty {
    private double x, y;
    private int size;
    private double fallSpeed;
    private boolean active;
    private PenaltyType type;
    private double remainingDuration;
    private GameConfig config;

    /**
     * Penalty types with their effects
     */
    public enum PenaltyType {
        SPEED_UP("⚡", Color.ORANGERED, "Speed Up!", true, 10.0),
        SHRINK_PADDLE("↔", Color.DARKRED, "Shrink Paddle", true, 8.0),
        DOUBLE_BALL("◎", Color.DARKORANGE, "Double Ball", false, 0),
        FALLING_BRICK("▼", Color.DARKGRAY, "Falling Brick!", false, 0),
        REVERSE_CONTROLS("⟷", Color.DARKVIOLET, "Reversed!", true, 6.0),
        BLIND_ZONE("▓", Color.BLACK, "Blind Zone", true, 5.0);

        private final String symbol;
        private final Color color;
        private final String description;
        private final boolean timed;
        private final double duration;

        PenaltyType(String symbol, Color color, String description, boolean timed, double duration) {
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
         * Get random penalty type
         */
        public static PenaltyType random() {
            PenaltyType[] types = values();
            return types[(int)(Math.random() * types.length)];
        }

        /**
         * Get weighted random penalty
         */
        public static PenaltyType weightedRandom() {
            double rand = Math.random();
            if (rand < 0.30) return SPEED_UP;           // 30%
            else if (rand < 0.55) return SHRINK_PADDLE; // 25%
            else if (rand < 0.75) return DOUBLE_BALL;   // 20%
            else if (rand < 0.88) return REVERSE_CONTROLS; // 13%
            else if (rand < 0.95) return FALLING_BRICK; // 7%
            else return BLIND_ZONE;                      // 5%
        }
    }

    public Penalty(double x, double y, PenaltyType type) {
        this.config = GameConfig.getInstance();
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = config.getPowerUpSize();
        this.fallSpeed = config.getPowerUpFallSpeed() * 1.2; // Falls faster than power-ups
        this.active = true;
        this.remainingDuration = type.getDuration();
    }

    /**
     * Create random penalty at position
     */
    public static Penalty createRandom(double x, double y) {
        return new Penalty(x, y, PenaltyType.weightedRandom());
    }

    /**
     * Update penalty position (falling)
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
     * Update remaining duration for timed penalties
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

        double penaltyLeft = x - size / 2.0;
        double penaltyRight = x + size / 2.0;
        double penaltyTop = y - size / 2.0;
        double penaltyBottom = y + size / 2.0;

        return penaltyRight > paddleLeft && penaltyLeft < paddleRight &&
                penaltyBottom > paddleTop && penaltyTop < paddleBottom;
    }

    /**
     * Collect penalty
     */
    public void collect() {
        active = false;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public boolean isActive() { return active; }
    public PenaltyType getType() { return type; }
    public double getRemainingDuration() { return remainingDuration; }
    public Color getColor() { return type.getColor(); }
    public String getSymbol() { return type.getSymbol(); }
}