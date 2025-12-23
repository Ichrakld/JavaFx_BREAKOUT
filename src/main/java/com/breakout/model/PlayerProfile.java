package com.breakout.model;

import com.breakout.config.GameConfig;
import com.breakout.database.DatabaseManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Player Profile Class
 * Stores player name and scores for each level
 *
 * Features:
 * - Database persistence for scores
 * - Level unlock tracking
 * - Star rating system
 */
public class PlayerProfile {
    private String playerName;
    private int playerId;  // Database ID
    private Map<Integer, Integer> levelScores;
    private Map<Integer, Boolean> levelUnlocked;
    private int totalLevels;
    private DatabaseManager db;

    public PlayerProfile() {
        this.playerName = "Player";
        this.playerId = -1;
        this.totalLevels = GameConfig.getInstance().getTotalLevels();
        this.levelScores = new HashMap<>();
        this.levelUnlocked = new HashMap<>();
        this.db = DatabaseManager.getInstance();

        // Initialize all levels
        for (int i = 1; i <= totalLevels; i++) {
            levelScores.put(i, 0);
            levelUnlocked.put(i, i == 1);
        }
    }

    /**
     * Set player name and load from database
     */
    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();

            // Get or create player in database
            this.playerId = db.getOrCreatePlayer(this.playerName);

            // Load existing scores from database
            loadScoresFromDatabase();
        }
    }

    /**
     * Load scores from database
     */
    private void loadScoresFromDatabase() {
        if (playerId > 0) {
            int[] scores = db.getAllLevelScores(playerId, totalLevels);
            for (int i = 0; i < scores.length; i++) {
                int level = i + 1;
                levelScores.put(level, scores[i]);

                // Unlock next level if score > 0
                if (scores[i] > 0 && level < totalLevels) {
                    levelUnlocked.put(level + 1, true);
                }
            }
        }
    }

    /**
     * Get player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Get player database ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Update score for a level (only if better)
     */
    public void updateLevelScore(int level, int score) {
        if (level >= 1 && level <= totalLevels) {
            int currentBest = levelScores.getOrDefault(level, 0);
            if (score > currentBest) {
                levelScores.put(level, score);

                // Save to database
                if (playerId > 0) {
                    int stars = calculateStars(score, level);
                    db.saveScore(playerId, level, score, stars);
                }
            }
        }
    }

    /**
     * Calculate star rating for score
     */
    private int calculateStars(int score, int level) {
        if (score == 0) return 0;
        int threshold = level * 100;
        if (score >= threshold * 5) return 5;
        if (score >= threshold * 4) return 4;
        if (score >= threshold * 3) return 3;
        if (score >= threshold * 2) return 2;
        if (score >= threshold) return 1;
        return 0;
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
        if (playerId > 0) {
            return db.getTotalScore(playerId);
        }
        int total = 0;
        for (int score : levelScores.values()) {
            total += score;
        }
        return total;
    }

    /**
     * Get player rank in leaderboard
     */
    public int getRank() {
        if (playerId > 0) {
            return db.getPlayerRank(playerId);
        }
        return 0;
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