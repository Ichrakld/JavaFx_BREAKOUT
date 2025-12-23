package com.breakout.database;

import com.breakout.config.GameConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager for Breakout Game (MySQL Version)
 *
 * Design Pattern: Singleton + DAO (Data Access Object)
 * - Manages MySQL database connection
 * - Provides CRUD operations for player scores
 * - Ensures data persistence across game sessions
 *
 * Database Schema:
 * - players: Stores player information
 * - scores: Stores level scores per player
 * - game_sessions: Tracks play sessions
 *
 * MySQL Configuration:
 * - Host: localhost (configurable in GameConfig)
 * - Port: 3306
 * - Database: breakout_game
 * - User: root (configurable)
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private GameConfig config;

    private DatabaseManager() {
        this.config = GameConfig.getInstance();
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize database connection and create tables
     */
    private void initializeDatabase() {
        try {
            // First, connect without database to create it if needed
            String baseUrl = "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            try (Connection tempConn = DriverManager.getConnection(
                    baseUrl, config.getDbUser(), config.getDbPassword())) {

                // Create database if not exists
                try (Statement stmt = tempConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + config.getDbName() +
                            " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }
            }

            // Now connect to the actual database
            connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getDbUser(),
                    config.getDbPassword()
            );

            createTables();
            System.out.println("MySQL Database connected successfully: " + config.getDbName());

        } catch (SQLException e) {
            System.err.println("MySQL connection failed: " + e.getMessage());
            System.err.println("Make sure MySQL is running and credentials are correct.");
            System.err.println("Host: " + config.getDbHost() + ", Port: " + config.getDbPort());
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private void createTables() throws SQLException {
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        String createScoresTable = """
            CREATE TABLE IF NOT EXISTS scores (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_id INT NOT NULL,
                level INT NOT NULL,
                score INT NOT NULL,
                stars INT DEFAULT 0,
                achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
                UNIQUE KEY unique_player_level (player_id, level)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS game_sessions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_id INT NOT NULL,
                total_score INT DEFAULT 0,
                levels_completed INT DEFAULT 0,
                total_time_seconds INT DEFAULT 0,
                played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        // Create leaderboard view
        String dropLeaderboardView = "DROP VIEW IF EXISTS leaderboard";

        String createLeaderboardView = """
            CREATE VIEW leaderboard AS
            SELECT p.name, 
                   COALESCE(SUM(s.score), 0) as total_score,
                   COUNT(DISTINCT s.level) as levels_completed,
                   COALESCE(SUM(s.stars), 0) as total_stars
            FROM players p
            LEFT JOIN scores s ON p.id = s.player_id
            GROUP BY p.id, p.name
            ORDER BY total_score DESC
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createScoresTable);
            stmt.execute(createSessionsTable);
            stmt.execute(dropLeaderboardView);
            stmt.execute(createLeaderboardView);
        }
    }

    /**
     * Check if connection is valid
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reconnect if connection lost
     */
    private void ensureConnection() {
        if (!isConnected()) {
            initializeDatabase();
        }
    }

    // ==================== PLAYER OPERATIONS ====================

    /**
     * Get or create a player by name
     * @return player ID
     */
    public int getOrCreatePlayer(String playerName) {
        ensureConnection();
        if (connection == null) return -1;

        String selectSql = "SELECT id FROM players WHERE name = ?";
        String insertSql = "INSERT INTO players (name) VALUES (?)";
        String updateSql = "UPDATE players SET last_played = CURRENT_TIMESTAMP WHERE id = ?";

        try {
            // Check if player exists
            try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
                pstmt.setString(1, playerName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int playerId = rs.getInt("id");
                    // Update last played
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, playerId);
                        updateStmt.executeUpdate();
                    }
                    return playerId;
                }
            }

            // Create new player
            try (PreparedStatement pstmt = connection.prepareStatement(insertSql,
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, playerName);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting/creating player: " + e.getMessage());
        }
        return -1;
    }

    // ==================== SCORE OPERATIONS ====================

    /**
     * Save or update level score (only if better)
     * Uses INSERT ... ON DUPLICATE KEY UPDATE for MySQL
     */
    public void saveScore(int playerId, int level, int score, int stars) {
        ensureConnection();
        if (connection == null || playerId < 0) return;

        // MySQL upsert syntax
        String upsertSql = """
            INSERT INTO scores (player_id, level, score, stars, achieved_at) 
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
                score = IF(VALUES(score) > score, VALUES(score), score),
                stars = IF(VALUES(score) > score, VALUES(stars), stars),
                achieved_at = IF(VALUES(score) > score, CURRENT_TIMESTAMP, achieved_at)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(upsertSql)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, level);
            pstmt.setInt(3, score);
            pstmt.setInt(4, stars);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    /**
     * Get best score for a specific level
     */
    public int getLevelScore(int playerId, int level) {
        ensureConnection();
        if (connection == null) return 0;

        String sql = "SELECT score FROM scores WHERE player_id = ? AND level = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, level);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("score");
            }
        } catch (SQLException e) {
            System.err.println("Error getting level score: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get all level scores for a player
     */
    public int[] getAllLevelScores(int playerId, int totalLevels) {
        ensureConnection();
        int[] scores = new int[totalLevels];
        if (connection == null) return scores;

        String sql = "SELECT level, score FROM scores WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int level = rs.getInt("level");
                if (level >= 1 && level <= totalLevels) {
                    scores[level - 1] = rs.getInt("score");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all scores: " + e.getMessage());
        }
        return scores;
    }

    /**
     * Get total score for a player
     */
    public int getTotalScore(int playerId) {
        ensureConnection();
        if (connection == null) return 0;

        String sql = "SELECT COALESCE(SUM(score), 0) as total FROM scores WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total score: " + e.getMessage());
        }
        return 0;
    }

    // ==================== LEADERBOARD OPERATIONS ====================

    /**
     * Get top players from leaderboard
     */
    public List<LeaderboardEntry> getLeaderboard(int limit) {
        ensureConnection();
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (connection == null) return entries;

        String sql = """
            SELECT p.name, 
                   COALESCE(SUM(s.score), 0) as total_score,
                   COUNT(DISTINCT s.level) as levels_completed
            FROM players p
            LEFT JOIN scores s ON p.id = s.player_id
            GROUP BY p.id, p.name
            ORDER BY total_score DESC
            LIMIT ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                        rank++,
                        rs.getString("name"),
                        rs.getInt("total_score"),
                        rs.getInt("levels_completed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting leaderboard: " + e.getMessage());
        }
        return entries;
    }

    /**
     * Get player rank in leaderboard
     */
    public int getPlayerRank(int playerId) {
        ensureConnection();
        if (connection == null) return 0;

        String sql = """
            SELECT COUNT(*) + 1 as player_rank
            FROM (
                SELECT player_id, SUM(score) as total
                FROM scores
                GROUP BY player_id
            ) rankings
            WHERE total > (
                SELECT COALESCE(SUM(score), 0)
                FROM scores
                WHERE player_id = ?
            )
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("player_rank");
            }
        } catch (SQLException e) {
            System.err.println("Error getting player rank: " + e.getMessage());
        }
        return 0;
    }

    // ==================== SESSION OPERATIONS ====================

    /**
     * Save game session
     */
    public void saveSession(int playerId, int totalScore, int levelsCompleted, int timeSeconds) {
        ensureConnection();
        if (connection == null || playerId < 0) return;

        String sql = "INSERT INTO game_sessions (player_id, total_score, levels_completed, total_time_seconds) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, totalScore);
            pstmt.setInt(3, levelsCompleted);
            pstmt.setInt(4, timeSeconds);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    /**
     * Get player statistics
     */
    public PlayerStats getPlayerStats(int playerId) {
        ensureConnection();
        if (connection == null) return new PlayerStats(0, 0, 0, 0);

        String sql = """
            SELECT 
                COUNT(*) as total_sessions,
                COALESCE(SUM(total_score), 0) as lifetime_score,
                COALESCE(MAX(levels_completed), 0) as max_level,
                COALESCE(SUM(total_time_seconds), 0) as total_time
            FROM game_sessions 
            WHERE player_id = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new PlayerStats(
                        rs.getInt("total_sessions"),
                        rs.getInt("lifetime_score"),
                        rs.getInt("max_level"),
                        rs.getInt("total_time")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting player stats: " + e.getMessage());
        }
        return new PlayerStats(0, 0, 0, 0);
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("MySQL connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    // ==================== DATA CLASSES ====================

    /**
     * Leaderboard entry data class
     */
    public static class LeaderboardEntry {
        public final int rank;
        public final String playerName;
        public final int totalScore;
        public final int levelsCompleted;

        public LeaderboardEntry(int rank, String playerName, int totalScore, int levelsCompleted) {
            this.rank = rank;
            this.playerName = playerName;
            this.totalScore = totalScore;
            this.levelsCompleted = levelsCompleted;
        }
    }

    /**
     * Player statistics data class
     */
    public static class PlayerStats {
        public final int totalSessions;
        public final int lifetimeScore;
        public final int maxLevel;
        public final int totalTimeSeconds;

        public PlayerStats(int totalSessions, int lifetimeScore, int maxLevel, int totalTimeSeconds) {
            this.totalSessions = totalSessions;
            this.lifetimeScore = lifetimeScore;
            this.maxLevel = maxLevel;
            this.totalTimeSeconds = totalTimeSeconds;
        }
    }
}