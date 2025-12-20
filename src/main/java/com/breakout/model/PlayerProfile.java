package com.breakout.model;

import com.breakout.config.GameConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Player Profile Class
 * Stores player name and scores for each level
 */
public class PlayerProfile {
    private String playerName;
    private Map<Integer, Integer> levelScores;      // Best score per level
    private Map<Integer, Boolean> levelUnlocked;    // Which levels are accessible
    private int totalLevels;
    
    public PlayerProfile() {
        this.playerName = "Player";
        this.totalLevels = GameConfig.getInstance().getTotalLevels();
        this.levelScores = new HashMap<>();
        this.levelUnlocked = new HashMap<>();
        
        // Initialize all levels
        for (int i = 1; i <= totalLevels; i++) {
            levelScores.put(i, 0);
            levelUnlocked.put(i, i == 1); // Only level 1 unlocked initially
        }
    }
    
    /**
     * Set player name
     */
    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
        }
    }
    
    /**
     * Get player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Update score for a level (only if better than previous)
     */
    public void updateLevelScore(int level, int score) {
        if (level >= 1 && level <= totalLevels) {
            int currentBest = levelScores.getOrDefault(level, 0);
            if (score > currentBest) {
                levelScores.put(level, score);
            }
        }
    }
    
    /**
     * Get best score for a level
     */
    public int getLevelScore(int level) {
        return levelScores.getOrDefault(level, 0);
    }
    
    /**
     * Unlock a level
     */
    public void unlockLevel(int level) {
        if (level >= 1 && level <= totalLevels) {
            levelUnlocked.put(level, true);
        }
    }
    
    /**
     * Check if a level is unlocked
     */
    public boolean isLevelUnlocked(int level) {
        return levelUnlocked.getOrDefault(level, false);
    }
    
    /**
     * Get total score across all levels
     */
    public int getTotalScore() {
        int total = 0;
        for (int score : levelScores.values()) {
            total += score;
        }
        return total;
    }
    
    /**
     * Unlock all levels (cheat/debug)
     */
    public void unlockAllLevels() {
        for (int i = 1; i <= totalLevels; i++) {
            levelUnlocked.put(i, true);
        }
    }
    
    /**
     * Get number of levels completed (with score > 0)
     */
    public int getLevelsCompleted() {
        int count = 0;
        for (int score : levelScores.values()) {
            if (score > 0) count++;
        }
        return count;
    }
    
    /**
     * Get total number of levels
     */
    public int getTotalLevels() {
        return totalLevels;
    }
}
