# 🎮 Breakout Enhanced v2.0 - JavaFX Game

A classic Breakout game with **player profiles**, **level selection**, and **score tracking per level**.

## ✨ New Features in v2.0

### 🎯 Player Name Input
- Enter your name on the first screen
- Name displayed throughout the game

### 📊 Level Selection Menu
- **5 Levels** to choose from
- Levels unlock progressively
- See your **best score** for each level
- **Star ratings** (☆☆☆ to ★★★) based on performance

### 🏆 Score System
- Each level has its **own score**
- Best scores are saved per level
- Total score displayed in menu
- Higher levels = more points multiplier

### ❤️ Fixed 3 Hearts
- Always exactly 3 lives
- No more negative lives bug

## 🎯 Controls

| Screen | Key | Action |
|--------|-----|--------|
| **Name Input** | Type | Enter your name |
| | ENTER | Confirm name |
| | BACKSPACE | Delete character |
| **Menu** | ↑/W | Select previous level |
| | ↓/S | Select next level |
| | ENTER/SPACE | Start selected level |
| | 1-5 | Quick select level |
| **Playing** | ←/A | Move paddle left |
| | →/D | Move paddle right |
| | SPACE | Launch ball |
| | P/ESC | Pause game |
| | M | Return to menu |
| **Paused** | SPACE/P/ESC | Resume |
| | R | Restart level |
| | M | Return to menu |

## 📁 Project Structure

```
BreakoutGame/
├── pom.xml
├── README.md
└── src/main/java/com/breakout/
    ├── Main.java                 # Entry point
    ├── config/
    │   └── GameConfig.java       # Game settings (Singleton)
    ├── model/
    │   ├── GameModel.java        # Game logic
    │   ├── GameState.java        # State enum
    │   ├── PlayerProfile.java    # Player data & scores
    │   ├── Ball.java
    │   ├── Paddle.java
    │   └── Brick.java
    ├── view/
    │   └── GameView.java         # All rendering
    └── controller/
        └── GameController.java   # Input handling
```

## 🎮 Game Flow

```
┌──────────────┐
│  NAME INPUT  │  Enter your name
└──────┬───────┘
       ↓
┌──────────────┐
│    MENU      │  Select level (1-5)
│  (Levels)    │  See scores & stars
└──────┬───────┘
       ↓
┌──────────────┐
│   PLAYING    │  Break all bricks!
└──────┬───────┘
       ↓
   ┌───┴───┐
   ↓       ↓
┌──────┐ ┌──────────────┐
│GAME  │ │LEVEL COMPLETE│
│OVER  │ │   → Next     │
└──────┘ └──────────────┘
```

## ⭐ Star Rating System

| Stars | Requirement |
|-------|-------------|
| ☆☆☆ | No score yet |
| ★☆☆ | Score ≥ Level × 100 |
| ★★☆ | Score ≥ Level × 250 |
| ★★★ | Score ≥ Level × 500 |

## 🚀 How to Run

### Maven
```bash
cd BreakoutGame
mvn clean javafx:run
```

### IntelliJ IDEA
1. Open project folder
2. Wait for Maven sync
3. Run `Main.java`

## 🎨 Level Difficulty

| Level | Brick Rows | Tough Bricks | Point Multiplier |
|-------|------------|--------------|------------------|
| 1 | 5 rows | None | ×1 |
| 2 | 6 rows | Top 2 rows HARD | ×2 |
| 3 | 7 rows | Top row TOUGH | ×3 |
| 4 | 8 rows | More TOUGH | ×4 |
| 5 | 8 rows (max) | Most TOUGH | ×5 |

## 🛠️ Technical Details

- **Framework**: JavaFX 21
- **Java Version**: 17+
- **Pattern**: MVC Architecture
- **Design Patterns**: Singleton, State

## 👨‍💻 Author

Created for Computer Engineering students at EMSI.

---

**Enjoy the game! 🎮**
