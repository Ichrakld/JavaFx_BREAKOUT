package com.breakout.model;

import com.breakout.config.GameConfig;

/**
 * Paddle Entity Class
 * Player-controlled paddle for bouncing the ball
 *
 * Features:
 * - Smooth movement
 * - Size modification (power-ups/penalties)
 * - Sticky paddle mode
 * - Reversed controls mode
 */
public class Paddle {
    private double x, y;
    private int width, height;
    private int originalWidth;
    private double speed;
    private double velocity;
    private boolean reversed;      // Reversed controls penalty
    private boolean sticky;        // Sticky paddle power-up
    private Ball stuckBall;        // Ball stuck to paddle
    private GameConfig config;

    public Paddle() {
        this.config = GameConfig.getInstance();
        this.width = config.getPaddleWidth();
        this.originalWidth = width;
        this.height = config.getPaddleHeight();
        this.speed = config.getPaddleSpeed();
        this.reversed = false;
        this.sticky = false;
        this.stuckBall = null;
        reset();
    }

    /**
     * Reset paddle to center position
     */
    public void reset() {
        this.x = config.getWindowWidth() / 2.0;
        this.y = config.getWindowHeight() - 50;
        this.velocity = 0;
        this.reversed = false;
        this.sticky = false;
        this.stuckBall = null;
    }

    /**
     * Move paddle left
     */
    public void moveLeft() {
        if (reversed) {
            velocity = speed;
        } else {
            velocity = -speed;
        }
    }

    /**
     * Move paddle right
     */
    public void moveRight() {
        if (reversed) {
            velocity = -speed;
        } else {
            velocity = speed;
        }
    }

    /**
     * Stop paddle movement
     */
    public void stop() {
        velocity = 0;
    }

    /**
     * Update paddle position
     */
    public void update() {
        x += velocity;

        double halfWidth = width / 2.0;
        if (x - halfWidth < 0) {
            x = halfWidth;
        } else if (x + halfWidth > config.getWindowWidth()) {
            x = config.getWindowWidth() - halfWidth;
        }

        // Update stuck ball position
        if (stuckBall != null) {
            stuckBall.setX(x);
        }
    }

    /**
     * Check collision with ball
     */
    public boolean collidesWith(Ball ball) {
        double ballX = ball.getX();
        double ballY = ball.getY();
        int ballRadius = ball.getRadius();

        boolean withinX = ballX + ballRadius > x - width / 2.0 &&
                ballX - ballRadius < x + width / 2.0;
        boolean withinY = ballY + ballRadius > y - height / 2.0 &&
                ballY - ballRadius < y + height / 2.0;

        return withinX && withinY && ball.getDy() > 0;
    }

    // ==================== SIZE MODIFICATIONS ====================

    /**
     * Extend paddle width (power-up)
     */
    public void extend() {
        width = (int)(originalWidth * 1.5);
    }

    /**
     * Shrink paddle width (penalty)
     */
    public void shrink() {
        width = (int)(originalWidth * 0.6);
    }

    /**
     * Reset paddle to normal size
     */
    public void resetSize() {
        width = originalWidth;
    }

    // ==================== SPECIAL MODES ====================

    /**
     * Enable/disable reversed controls
     */
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * Check if controls are reversed
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * Enable/disable sticky mode
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
        if (!sticky) {
            releaseStuckBall();
        }
    }

    /**
     * Check if paddle is sticky
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Stick a ball to paddle
     */
    public void stickBall(Ball ball) {
        if (sticky && stuckBall == null) {
            stuckBall = ball;
            ball.setY(y - height / 2.0 - ball.getRadius());
        }
    }

    /**
     * Release stuck ball
     */
    public void releaseStuckBall() {
        if (stuckBall != null) {
            stuckBall.launch();
            stuckBall = null;
        }
    }

    /**
     * Check if ball is stuck
     */
    public boolean hasBallStuck() {
        return stuckBall != null;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getVelocity() { return velocity; }
}