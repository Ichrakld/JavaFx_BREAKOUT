package com.breakout.model;

import javafx.scene.paint.Color;

/**
 * Brick Entity Class
 * Represents breakable bricks with different types and hit points
 */
public class Brick {
    private double x, y;
    private int width, height;
    private int hitPoints;
    private int maxHitPoints;
    private boolean active;
    private BrickType type;
    private int points;
    
    /**
     * Brick types with different behaviors
     */
    public enum BrickType {
        NORMAL(1, 10, Color.LIGHTGREEN),
        HARD(2, 25, Color.ORANGE),
        TOUGH(3, 50, Color.RED),
        UNBREAKABLE(-1, 0, Color.GRAY);
        
        private final int hitPoints;
        private final int points;
        private final Color color;
        
        BrickType(int hitPoints, int points, Color color) {
            this.hitPoints = hitPoints;
            this.points = points;
            this.color = color;
        }
        
        public int getHitPoints() { return hitPoints; }
        public int getPoints() { return points; }
        public Color getColor() { return color; }
    }
    
    public Brick(double x, double y, int width, int height, BrickType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.hitPoints = type.getHitPoints();
        this.maxHitPoints = type.getHitPoints();
        this.points = type.getPoints();
        this.active = true;
    }
    
    /**
     * Hit the brick, reduce hit points
     * @return points earned (0 if not destroyed)
     */
    public int hit() {
        if (!active || type == BrickType.UNBREAKABLE) {
            return 0;
        }
        
        hitPoints--;
        
        if (hitPoints <= 0) {
            active = false;
            return points;
        }
        
        return 0;
    }
    
    /**
     * Check collision with ball
     */
    public boolean collidesWith(Ball ball) {
        if (!active) return false;
        
        double ballX = ball.getX();
        double ballY = ball.getY();
        int ballRadius = ball.getRadius();
        
        // Find closest point on brick to ball center
        double closestX = Math.max(x, Math.min(ballX, x + width));
        double closestY = Math.max(y, Math.min(ballY, y + height));
        
        // Calculate distance from ball center to closest point
        double distanceX = ballX - closestX;
        double distanceY = ballY - closestY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        return distance < ballRadius;
    }
    
    /**
     * Determine collision side for bounce direction
     * Returns: "top", "bottom", "left", "right", or "none"
     */
    public String getCollisionSide(Ball ball) {
        double ballX = ball.getX();
        double ballY = ball.getY();
        
        double brickCenterX = x + width / 2.0;
        double brickCenterY = y + height / 2.0;
        
        double dx = ballX - brickCenterX;
        double dy = ballY - brickCenterY;
        
        double widthRatio = dx / (width / 2.0);
        double heightRatio = dy / (height / 2.0);
        
        if (Math.abs(widthRatio) > Math.abs(heightRatio)) {
            return dx > 0 ? "right" : "left";
        } else {
            return dy > 0 ? "bottom" : "top";
        }
    }
    
    /**
     * Get current color based on hit points remaining
     */
    public Color getColor() {
        if (type == BrickType.UNBREAKABLE) {
            return type.getColor();
        }
        
        // Fade color as brick takes damage
        Color baseColor = type.getColor();
        double ratio = (double) hitPoints / maxHitPoints;
        return baseColor.deriveColor(0, 1, ratio * 0.5 + 0.5, 1);
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isActive() { return active; }
    public BrickType getType() { return type; }
    public int getHitPoints() { return hitPoints; }
    public int getPoints() { return points; }
}
