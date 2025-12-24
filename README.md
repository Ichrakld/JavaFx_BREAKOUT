# 🎮 Breakout Enhanced v3.0 - JavaFX Game

A classic Breakout game with **power-ups**, **penalties**, **database persistence**, **background music**, and **custom backgrounds**.

## ✨ New Features in v3.0

### 🎁 Power-Ups (Récompenses)
Collect falling power-ups to gain advantages:

| Power-Up | Symbol | Effect | Duration |
|----------|--------|--------|----------|
| Extend Paddle | E | Makes paddle 50% wider | 10s |
| Multi-Ball | M | Spawns 2 additional balls | Instant |
| Slow Ball | S | Reduces ball speed | 8s |
| Extra Life | ♥ | Adds one life | Instant |
| Score Boost | 2x | Double points | 15s |
| Sticky Paddle | ▬ | Ball sticks to paddle | 12s |
| Shield | ◊ | Prevents ball loss once | 20s |

### ⚠️ Penalties (Pénalités)
Avoid or survive these negative effects:

| Penalty | Symbol | Effect | Duration |
|---------|--------|--------|----------|
| Speed Up | ⚡ | Increases ball speed by 30% | 10s |
| Shrink Paddle | ↔ | Makes paddle 40% smaller | 8s |
| Double Ball | ◎ | Spawns extra ball (harder to track) | Instant |
| Falling Brick | ▼ | A brick falls toward paddle! | Instant |
| Reverse Controls | ⟷ | Inverts left/right controls | 6s |
| Blind Zone | ▓ | Creates invisible zone on screen | 5s |

### 💾 Database Integration (MySQL)
- **Persistent scores**: Your best scores are saved to MySQL database
- **Leaderboard**: Compete with other players
- **Player profiles**: Track progress across sessions
- **Session history**: Game statistics are recorded

**MySQL Configuration** (in `GameConfig.java`):
```java
private final String dbHost = "localhost";
private final int dbPort = 3306;
private final String dbName = "breakout_game";
private final String dbUser = "root";
private final String dbPassword = "";  // Set your password
```

### 🎵 Audio System
- **Background Music**: Different tracks per level
- **Sound Effects**: 
  - Ball hits (paddle, brick, wall)
  - Brick destruction
  - Power-up/penalty collection
  - Level complete/Game over
- **Toggle controls**: F1 (Music) / F2 (SFX)

### 🖼️ Background Images
- Custom backgrounds per level
- Animated star field fallback
- Level-themed color gradients

## 📁 Project Structure (MVC Architecture)

```
BreakoutGame/
├── pom.xml
├── README.md
├── database_setup.sql          # MySQL setup script
└── src/main/
    ├── java/com/breakout/
    │   ├── Main.java                 # Entry point
    │   ├── config/
    │   │   └── GameConfig.java       # Singleton configuration
    │   ├── model/
    │   │   ├── GameModel.java        # Game logic (Model)
    │   │   ├── GameState.java        # State enum
    │   │   ├── PlayerProfile.java    # Player data
    │   │   ├── Ball.java
    │   │   ├── Paddle.java
    │   │   ├── Brick.java
    │   │   ├── PowerUp.java          # NEW: Power-up entity
    │   │   ├── Penalty.java          # NEW: Penalty entity
    │   │   └── FallingBrick.java     # NEW: Falling brick hazard
    │   ├── view/
    │   │   └── GameView.java         # Rendering (View)
    │   ├── controller/
    │   │   └── GameController.java   # Input handling (Controller)
    │   ├── database/
    │   │   └── DatabaseManager.java  # SQLite DAO
    │   └── audio/
    │       └── AudioManager.java     # Sound system
    └── resources/
        ├── images/                   # Background images
        │   ├── menu_bg.png
        │   ├── level1_bg.png
        │   ├── level2_bg.png
        │   └── ...
        └── sounds/                   # Audio files
            ├── menu_music.mp3
            ├── level1_music.mp3
            ├── ball_paddle.wav
            ├── brick_destroy.wav
            └── ...
```

## 🎮 Controls

| Screen | Key | Action |
|--------|-----|--------|
| **Global** | F1 | Toggle Music |
| | F2 | Toggle Sound Effects |
| **Name Input** | Type | Enter your name |
| | ENTER | Confirm name |
| | BACKSPACE | Delete character |
| **Menu** | ↑/W | Select previous level |
| | ↓/S | Select next level |
| | ENTER/SPACE | Start selected level |
| | 1-5 | Quick select level |
| | L | View Leaderboard |
| **Playing** | ←/A | Move paddle left |
| | →/D | Move paddle right |
| | SPACE | Launch ball / Release sticky ball |
| | P/ESC | Pause game |
| | M | Return to menu |
| **Paused** | SPACE/P/ESC | Resume |
| | R | Restart level |
| | M | Return to menu |

## 🎯 Design Patterns Used

### 1. MVC (Model-View-Controller)
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Model     │◄────│ Controller  │◄────│    View     │
│ (GameModel) │     │(GameCtrl)   │     │ (GameView)  │
│             │────►│             │────►│             │
└─────────────┘     └─────────────┘     └─────────────┘
```

### 2. Singleton Pattern
- `GameConfig`: Global configuration access
- `DatabaseManager`: Single database connection
- `AudioManager`: Centralized audio control

### 3. State Pattern
```java
enum GameState {
    NAME_INPUT, MENU, LEADERBOARD,
    PLAYING, PAUSED, GAME_OVER,
    LEVEL_COMPLETE, VICTORY
}
```

### 4. Strategy Pattern
- `PowerUp.PowerUpType`: Different power-up behaviors
- `Penalty.PenaltyType`: Different penalty effects
- `Brick.BrickType`: Different brick properties

### 5. DAO Pattern (Data Access Object)
- `DatabaseManager`: Abstracts SQLite operations

## 🗄️ Database Schema (MySQL)

```sql
-- Players table
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Scores table
CREATE TABLE scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    level INT NOT NULL,
    score INT NOT NULL,
    stars INT DEFAULT 0,
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY (player_id, level)
) ENGINE=InnoDB;

-- Game sessions
CREATE TABLE game_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    total_score INT DEFAULT 0,
    levels_completed INT DEFAULT 0,
    total_time_seconds INT DEFAULT 0,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### MySQL Setup
1. Install MySQL Server (XAMPP, WAMP, or standalone)
2. Start MySQL service
3. Run the setup script:
```bash
mysql -u root -p < database_setup.sql
```
Or let the application create the database automatically on first run.

## 🎨 Brick Types

| Type | Hit Points | Base Points | Color | Special |
|------|------------|-------------|-------|---------|
| Normal | 1 | 10 | Green | - |
| Hard | 2 | 25 | Orange | - |
| Tough | 3 | 50 | Red | - |
| Gold | 2 | 100 | Gold | Bonus points |
| Power | 1 | 15 | Cyan | Always drops power-up |
| Penalty | 1 | 5 | Gray | Always drops penalty |
| Unbreakable | ∞ | 0 | Slate | Cannot be destroyed |

## ⭐ Star Rating System

| Stars | Score Requirement |
|-------|-------------------|
| ☆☆☆☆☆ | No score yet |
| ★☆☆☆☆ | Score ≥ Level × 100 |
| ★★☆☆☆ | Score ≥ Level × 200 |
| ★★★☆☆ | Score ≥ Level × 300 |
| ★★★★☆ | Score ≥ Level × 400 |
| ★★★★★ | Score ≥ Level × 500 |

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Maven
```bash
cd BreakoutGame
mvn clean javafx:run
```

### IntelliJ IDEA
1. Open project folder
2. Wait for Maven sync
3. Run `Main.java`

## 📦 Dependencies

```xml
<dependencies>
    <!-- JavaFX 21 (Controls, Graphics, Base, Media) -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-media</artifactId>
        <version>21</version>
    </dependency>
    
    <!-- MySQL JDBC Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.2.0</version>
    </dependency>
</dependencies>
```

## 🔧 MySQL Configuration

Edit `src/main/java/com/breakout/config/GameConfig.java`:

```java
// Database settings (MySQL)
private final String dbHost = "localhost";
private final int dbPort = 3306;
private final String dbName = "breakout_game";
private final String dbUser = "root";
private final String dbPassword = "your_password";  // Change this!
```

## 🎵 Adding Custom Audio

Place audio files in `src/main/resources/sounds/`:

| Filename | Type | Usage |
|----------|------|-------|
| `menu_music.mp3` | Music | Menu screen |
| `level1_music.mp3` | Music | Level 1 |
| `level2_music.mp3` | Music | Level 2 |
| `ball_paddle.wav` | SFX | Ball hits paddle |
| `brick_destroy.wav` | SFX | Brick destroyed |
| `powerup.wav` | SFX | Power-up collected |
| `penalty.wav` | SFX | Penalty collected |

## 🖼️ Adding Custom Backgrounds

Place images in `src/main/resources/images/`:

| Filename | Usage |
|----------|-------|
| `menu_bg.png` | Menu background |
| `level1_bg.png` | Level 1 background |
| `level2_bg.png` | Level 2 background |
| ... | ... |

Recommended size: 800×600 pixels

## 🎮 Game Flow

```
┌──────────────┐
│  NAME INPUT  │  Enter your name
└──────┬───────┘
       ↓
┌──────────────┐     ┌─────────────┐
│    MENU      │◄───►│ LEADERBOARD │
│  (Levels)    │     │   (Top 10)  │
└──────┬───────┘     └─────────────┘
       ↓
┌──────────────┐
│   PLAYING    │  Break all bricks!
│ +Power-ups   │  Collect power-ups
│ +Penalties   │  Avoid penalties
└──────┬───────┘
       ↓
   ┌───┴───┐
   ↓       ↓
┌──────┐ ┌──────────────┐
│GAME  │ │LEVEL COMPLETE│
│OVER  │ │   → Next     │
└──────┘ └──────┬───────┘
               ↓
         ┌──────────┐
         │ VICTORY! │
         └──────────┘
```

## 👨‍💻 Author

Created for Computer Engineering students at EMSI.
Course: Java Programming / Software Architecture

---

**Enjoy the game! 🎮**

*Version 3.0 - With Power-ups, Penalties, Database, and Audio*
