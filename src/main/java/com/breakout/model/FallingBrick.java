package com.breakout.model;

import com.breakout.config.GameConfig;
import javafx.scene.paint.Color;

/**
 * FallingBrick Entity Class
 * Represents a brick that falls from the play area
 *
 * Behavior:
 * - Falls towards the paddle
 * - If it hits the paddle, player loses a life
 * - Can be destroyed by the ball before reaching paddle
 * - Visual warning when spawned
 */
public class FallingBrick {
    private double x, y;
    private int width, height;
    private double fallSpeed;
    private boolean active;
    private int hitPoints;
    private Color color;
    private double rotation;
    private double rotationSpeed;
    private GameConfig config;

    public FallingBrick(double x, double y, int width, int height) {
        this.config = GameConfig.getInstance();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fallSpeed = config.getFallingBrickSpeed();
        this.active = true;
        this.hitPoints = 1;
        this.color = Color.DARKGRAY;
        this.rotation = 0;
        this.rotationSpeed = (Math.random() - 0.5) * 4; // Random rotation
    }

    /**
     * Create falling brick from existing brick
     */
    public static FallingBrick fromBrick(Brick brick) {
        FallingBrick fb = new FallingBrick(
                brick.getX() + brick.getWidth() / 2.0,
                brick.getY() + brick.getHeight() / 2.0,
                brick.getWidth(),
                brick.getHeight()
        );
        fb.color = brick.getColor().darker();
        return fb;
    }

    /**
     * Create random falling brick at top of screen
     */
    public static FallingBrick createRandom() {
        GameConfig config = GameConfig.getInstance();
        double x = Math.random() * (config.getWindowWidth() - config.getBrickWidth())
                + config.getBrickWidth() / 2.0;
        return new FallingBrick(x, 50, config.getBrickWidth(), config.getBrickHeight());
    }

    /**
     * Update falling brick position
     */
    public void update() {
        if (!active) return;

        y += fallSpeed;
        rotation += rotationSpeed;

        // Accelerate slightly
        fallSpeed += 0.02;

        // Deactivate if falls below screen
        if (y > config.getWindowHeight() + height) {
            active = false;
        }
    }

    /**
     * Check collision with paddle
     */
    public boolean collidesWithPaddle(Paddle paddle) {
        if (!active) return false;

        double paddleLeft = paddle.getX() - paddle.getWidth() / 2.0;
        double paddleRight = paddle.getX() + paddle.getWidth() / 2.0;
        double paddleTop = paddle.getY() - paddle.getHeight() / 2.0;
        double paddleBottom = paddle.getY() + paddle.getHeight() / 2.0;

        double brickLeft = x - width / 2.0;
        double brickRight = x + width / 2.0;
        double brickTop = y - height / 2.0;
        double brickBottom = y + height / 2.0;

        return brickRight > paddleLeft && brickLeft < paddleRight &&
                brickBottom > paddleTop && brickTop < paddleBottom;
    }

    /**
     * Check collision with ball
     */
    public boolean collidesWithBall(Ball ball) {
        if (!active) return false;

        double ballX = ball.getX();
        double ballY = ball.getY();
        int ballRadius = ball.getRadius();

        double brickLeft = x - width / 2.0;
        double brickRight = x + width / 2.0;
        double brickTop = y - height / 2.0;
        double brickBottom = y + height / 2.0;

        // Find closest point on brick to ball
        double closestX = Math.max(brickLeft, Math.min(ballX, brickRight));
        double closestY = Math.max(brickTop, Math.min(ballY, brickBottom));

        double distanceX = ballX - closestX;
        double distanceY = ballY - closestY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        return distance < ballRadius;
    }

    /**
     * Hit the falling brick
     * @return true if destroyed
     */
    public boolean hit() {
        hitPoints--;
        if (hitPoints <= 0) {
            active = false;
            return true;
        }
        return false;
    }

    /**
     * Destroy the brick
     */
    public void destroy() {
        active = false;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isActive() { return active; }
    public Color getColor() { return color; }
    public double getRotation() { return rotation; }
}