package com.breakout.model;

import com.breakout.audio.AudioManager;
import com.breakout.config.GameConfig;

/**
 * Ball Entity Class
 * Handles ball movement, physics, and collision
 *
 * Features:
 * - Smooth physics-based movement
 * - Angle-based paddle bouncing
 * - Speed modifications for penalties/power-ups
 * - Support for multiple balls (multi-ball power-up)
 */
public class Ball {
    private double x, y;
    private double dx, dy;
    private int radius;
    private boolean launched;
    private boolean isClone;  // For multi-ball
    private GameConfig config;

    public Ball() {
        this.config = GameConfig.getInstance();
        this.radius = config.getBallRadius();
        this.isClone = false;
        reset();
    }

    /**
     * Create a clone ball (for multi-ball power-up)
     */
    public Ball(Ball original) {
        this.config = GameConfig.getInstance();
        this.radius = original.radius;
        this.x = original.x;
        this.y = original.y;
        this.launched = true;
        this.isClone = true;

        // Clone with slightly different angle
        double speed = Math.sqrt(original.dx * original.dx + original.dy * original.dy);
        double angle = Math.atan2(original.dy, original.dx);
        angle += (Math.random() - 0.5) * Math.PI / 3; // Random deviation
        this.dx = speed * Math.cos(angle);
        this.dy = speed * Math.sin(angle);
    }

    /**
     * Reset ball to starting position (on paddle)
     */
    public void reset() {
        this.x = config.getWindowWidth() / 2.0;
        this.y = config.getWindowHeight() - 80;
        this.dx = 0;
        this.dy = 0;
        this.launched = false;
    }

    /**
     * Launch the ball with initial velocity
     */
    public void launch() {
        if (!launched) {
            double speed = config.getBallSpeed();
            double angle = Math.toRadians(-60 + Math.random() * 30);
            this.dx = speed * Math.sin(angle);
            this.dy = -speed * Math.cos(angle);
            this.launched = true;
        }
    }

    /**
     * Update ball position
     */
    public void update() {
        if (!launched) return;

        x += dx;
        y += dy;

        // Wall collision (left/right)
        if (x - radius <= 0) {
            x = radius;
            dx = Math.abs(dx);
            // PLAY SOUND HERE
            AudioManager.getInstance().playSfx(AudioManager.SoundEffect.BALL_HIT_WALL);
        } else if (x + radius >= config.getWindowWidth()) {
            x = config.getWindowWidth() - radius;
            dx = -Math.abs(dx);
            // PLAY SOUND HERE
            AudioManager.getInstance().playSfx(AudioManager.SoundEffect.BALL_HIT_WALL);
        }

        // Ceiling collision
        if (y - radius <= 0) {
            y = radius;
            dy = Math.abs(dy);
            // PLAY SOUND HERE
            AudioManager.getInstance().playSfx(AudioManager.SoundEffect.BALL_HIT_WALL);
        }
    }

    /**
     * Check if ball fell below screen
     */
    public boolean isBelowScreen() {
        return y - radius > config.getWindowHeight();
    }

    /**
     * Follow paddle position when not launched
     */
    public void followPaddle(double paddleX) {
        if (!launched) {
            this.x = paddleX;
            this.y = config.getWindowHeight() - 80;
        }
    }

    /**
     * Bounce off paddle with angle based on hit position
     */
    public void bounceOffPaddle(double paddleX, double paddleWidth) {
        double hitPos = (x - paddleX) / (paddleWidth / 2);
        hitPos = Math.max(-1, Math.min(1, hitPos));

        double speed = Math.sqrt(dx * dx + dy * dy);
        double maxAngle = Math.toRadians(60);
        double angle = hitPos * maxAngle;

        dx = speed * Math.sin(angle);
        dy = -Math.abs(speed * Math.cos(angle));

        if (dy > -2) dy = -2;

        // Move ball above paddle to prevent multiple collisions
        y = config.getWindowHeight() - 80 - radius;
    }

    /**
     * Reverse horizontal direction
     */
    public void reverseX() {
        dx = -dx;
    }

    /**
     * Reverse vertical direction
     */
    public void reverseY() {
        dy = -dy;
    }

    /**
     * Increase ball speed by factor
     */
    public void increaseSpeed(double factor) {
        dx *= factor;
        dy *= factor;
        capSpeed();
    }

    /**
     * Decrease ball speed by factor
     */
    public void decreaseSpeed(double factor) {
        dx /= factor;
        dy /= factor;

        // Ensure minimum speed
        double minSpeed = 2;
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed < minSpeed) {
            dx = (dx / currentSpeed) * minSpeed;
            dy = (dy / currentSpeed) * minSpeed;
        }
    }

    /**
     * Set absolute speed
     */
    public void setSpeed(double speed) {
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed > 0) {
            dx = (dx / currentSpeed) * speed;
            dy = (dy / currentSpeed) * speed;
        }
    }

    /**
     * Cap maximum speed
     */
    private void capSpeed() {
        double maxSpeed = config.getMaxBallSpeed();
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed > maxSpeed) {
            dx = (dx / currentSpeed) * maxSpeed;
            dy = (dy / currentSpeed) * maxSpeed;
        }
    }

    /**
     * Get current speed
     */
    public double getSpeed() {
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadius() { return radius; }
    public boolean isLaunched() { return launched; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public boolean isClone() { return isClone; }

    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setLaunched(boolean launched) { this.launched = launched; }
}