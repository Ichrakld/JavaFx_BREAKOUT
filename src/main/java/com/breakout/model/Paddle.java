package com.breakout.model;

import com.breakout.config.GameConfig;

/**
 * Paddle Entity Class
 * Player-controlled paddle for bouncing the ball
 */
public class Paddle {
    private double x, y;
    private int width, height;
    private double speed;
    private double velocity;
    private GameConfig config;
    
    public Paddle() {
        this.config = GameConfig.getInstance();
        this.width = config.getPaddleWidth();
        this.height = config.getPaddleHeight();
        this.speed = config.getPaddleSpeed();
        reset();
    }
    
    /**
     * Reset paddle to center position
     */
    public void reset() {
        this.x = config.getWindowWidth() / 2.0;
        this.y = config.getWindowHeight() - 50;
        this.velocity = 0;
    }
    
    /**
     * Move paddle left
     */
    public void moveLeft() {
        velocity = -speed;
    }
    
    /**
     * Move paddle right
     */
    public void moveRight() {
        velocity = speed;
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
        
        // Keep paddle within bounds
        double halfWidth = width / 2.0;
        if (x - halfWidth < 0) {
            x = halfWidth;
        } else if (x + halfWidth > config.getWindowWidth()) {
            x = config.getWindowWidth() - halfWidth;
        }
    }
    
    /**
     * Check collision with ball
     */
    public boolean collidesWith(Ball ball) {
        double ballX = ball.getX();
        double ballY = ball.getY();
        int ballRadius = ball.getRadius();
        
        // Check if ball is within paddle bounds
        boolean withinX = ballX + ballRadius > x - width / 2.0 && 
                          ballX - ballRadius < x + width / 2.0;
        boolean withinY = ballY + ballRadius > y - height / 2.0 && 
                          ballY - ballRadius < y + height / 2.0;
        
        // Only collide if ball is moving downward
        return withinX && withinY && ball.getDy() > 0;
    }
    
    /**
     * Extend paddle width (power-up)
     */
    public void extend() {
        width = (int)(config.getPaddleWidth() * 1.5);
    }
    
    /**
     * Shrink paddle width (power-up)
     */
    public void shrink() {
        width = (int)(config.getPaddleWidth() * 0.7);
    }
    
    /**
     * Reset paddle to normal size
     */
    public void resetSize() {
        width = config.getPaddleWidth();
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
