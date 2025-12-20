package com.breakout.model;

/**
 * Game State Enum - State Pattern
 * Represents all possible states of the game
 */
public enum GameState {
    NAME_INPUT,     // Player entering their name
    MENU,           // Main menu with level selection
    PLAYING,        // Active gameplay
    PAUSED,         // Game paused
    GAME_OVER,      // Player lost all lives
    LEVEL_COMPLETE, // Level completed
    VICTORY         // Game won (all levels beaten)
}
