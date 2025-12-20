package com.breakout.model;

import com.breakout.config.GameConfig;

/**
 * Ball Entity Class
 * Handles ball movement, physics, and collision
 */
public class Ball {
    private double x, y;
    private double dx, dy;
    private int radius;
    private boolean launched;
    private GameConfig config;
    
    public Ball() {
        this.config = GameConfig.getInstance();
        this.radius = config.getBallRadius();
        reset();
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
            // Random angle between -45 and 45 degrees
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
        } else if (x + radius >= config.getWindowWidth()) {
            x = config.getWindowWidth() - radius;
            dx = -Math.abs(dx);
        }
        
        // Ceiling collision
        if (y - radius <= 0) {
            y = radius;
            dy = Math.abs(dy);
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
        // Calculate hit position relative to paddle center (-1 to 1)
        double hitPos = (x - paddleX) / (paddleWidth / 2);
        hitPos = Math.max(-1, Math.min(1, hitPos));
        
        // Adjust angle based on hit position
        double speed = Math.sqrt(dx * dx + dy * dy);
        double maxAngle = Math.toRadians(60);
        double angle = hitPos * maxAngle;
        
        dx = speed * Math.sin(angle);
        dy = -Math.abs(speed * Math.cos(angle));
        
        // Ensure ball moves upward
        if (dy > -2) dy = -2;
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
     * Increase ball speed
     */
    public void increaseSpeed(double factor) {
        dx *= factor;
        dy *= factor;
        
        // Cap maximum speed
        double maxSpeed = 12;
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed > maxSpeed) {
            dx = (dx / currentSpeed) * maxSpeed;
            dy = (dy / currentSpeed) * maxSpeed;
        }
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadius() { return radius; }
    public boolean isLaunched() { return launched; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    
    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
