package com.breakout.controller;

import com.breakout.model.GameModel;
import com.breakout.model.GameState;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Game Controller - MVC Pattern
 * Handles all user input and delegates to model
 * 
 * CONTROLS:
 * - Name Input: Type name, ENTER to confirm
 * - Menu: UP/DOWN to select level, ENTER to start
 * - Playing: LEFT/RIGHT to move, SPACE to launch
 * - P/ESCAPE: Pause/Resume
 * - M: Return to menu
 */
public class GameController {
    private GameModel model;
    private Set<KeyCode> pressedKeys;
    
    public GameController(GameModel model) {
        this.model = model;
        this.pressedKeys = new HashSet<>();
    }
    
    /**
     * Handle key press events
     */
    public void handleKeyPressed(KeyCode code) {
        pressedKeys.add(code);
        
        GameState currentState = model.getState();
        
        switch (currentState) {
            case NAME_INPUT:
                handleNameInputKey(code);
                break;
            case MENU:
                handleMenuKey(code);
                break;
            case PLAYING:
                handlePlayingKey(code);
                break;
            case PAUSED:
                handlePausedKey(code);
                break;
            case GAME_OVER:
                handleGameOverKey(code);
                break;
            case LEVEL_COMPLETE:
                handleLevelCompleteKey(code);
                break;
            case VICTORY:
                handleVictoryKey(code);
                break;
        }
    }
    
    /**
     * Handle key typed events (for name input)
     */
    public void handleKeyTyped(KeyEvent event) {
        if (model.getState() == GameState.NAME_INPUT) {
            String character = event.getCharacter();
            if (character.length() == 1) {
                char c = character.charAt(0);
                if (Character.isLetterOrDigit(c) || c == ' ' || c == '_') {
                    model.addCharToName(c);
                }
            }
        }
    }
    
    /**
     * Handle key release events
     */
    public void handleKeyReleased(KeyCode code) {
        pressedKeys.remove(code);
    }
    
    /**
     * Update paddle movement based on pressed keys
     */
    public void update() {
        if (model.getState() != GameState.PLAYING) {
            model.getPaddle().stop();
            return;
        }
        
        boolean leftPressed = pressedKeys.contains(KeyCode.LEFT) || 
                              pressedKeys.contains(KeyCode.A);
        boolean rightPressed = pressedKeys.contains(KeyCode.RIGHT) || 
                               pressedKeys.contains(KeyCode.D);
        
        if (leftPressed && !rightPressed) {
            model.getPaddle().moveLeft();
        } else if (rightPressed && !leftPressed) {
            model.getPaddle().moveRight();
        } else {
            model.getPaddle().stop();
        }
    }
    
    // ==================== STATE-SPECIFIC HANDLERS ====================
    
    /**
     * Handle keys during name input
     */
    private void handleNameInputKey(KeyCode code) {
        switch (code) {
            case ENTER:
                model.confirmName();
                break;
            case BACK_SPACE:
                model.removeCharFromName();
                break;
            default:
                // Letter/number input handled by handleKeyTyped
                break;
        }
    }
    
    /**
     * Handle keys in menu (level selection)
     */
    private void handleMenuKey(KeyCode code) {
        switch (code) {
            case UP:
            case W:
                model.selectPreviousLevel();
                break;
            case DOWN:
            case S:
                model.selectNextLevel();
                break;
            case ENTER:
            case SPACE:
                if (model.canPlaySelectedLevel()) {
                    model.startSelectedLevel();
                }
                break;
            case DIGIT1:
                selectLevelIfUnlocked(1);
                break;
            case DIGIT2:
                selectLevelIfUnlocked(2);
                break;
            case DIGIT3:
                selectLevelIfUnlocked(3);
                break;
            case DIGIT4:
                selectLevelIfUnlocked(4);
                break;
            case DIGIT5:
                selectLevelIfUnlocked(5);
                break;
            default:
                break;
        }
    }
    
    /**
     * Select level by number if unlocked
     */
    private void selectLevelIfUnlocked(int level) {
        if (model.getPlayerProfile().isLevelUnlocked(level)) {
            model.startLevel(level);
        }
    }
    
    /**
     * Handle keys during gameplay
     */
    private void handlePlayingKey(KeyCode code) {
        switch (code) {
            case SPACE:
                model.launchBall();
                break;
            case P:
            case ESCAPE:
                model.pauseGame();
                break;
            case M:
                model.returnToMenu();
                break;
            default:
                break;
        }
    }
    
    /**
     * Handle keys when paused
     */
    private void handlePausedKey(KeyCode code) {
        switch (code) {
            case SPACE:
            case P:
            case ESCAPE:
                model.resumeGame();
                break;
            case M:
                model.returnToMenu();
                break;
            case R:
                model.restartLevel();
                break;
            default:
                break;
        }
    }
    
    /**
     * Handle keys on game over screen
     */
    private void handleGameOverKey(KeyCode code) {
        switch (code) {
            case ENTER:
            case R:
                model.restartLevel();
                break;
            case M:
                model.returnToMenu();
                break;
            default:
                break;
        }
    }
    
    /**
     * Handle keys on level complete screen
     */
    private void handleLevelCompleteKey(KeyCode code) {
        switch (code) {
            case ENTER:
            case SPACE:
                model.continueToNextLevel();
                break;
            case M:
                model.returnToMenu();
                break;
            default:
                break;
        }
    }
    
    /**
     * Handle keys on victory screen
     */
    private void handleVictoryKey(KeyCode code) {
        switch (code) {
            case ENTER:
            case M:
                model.returnToMenu();
                break;
            default:
                break;
        }
    }
    
    /**
     * Check if a specific key is currently pressed
     */
    public boolean isKeyPressed(KeyCode code) {
        return pressedKeys.contains(code);
    }
}
