package com.breakout;

import com.breakout.config.GameConfig;
import com.breakout.controller.GameController;
import com.breakout.model.GameModel;
import com.breakout.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Entry Point
 * 
 * BREAKOUT GAME - ENHANCED EDITION v2.0
 * 
 * Features:
 * - Player name input on first screen
 * - Level selection menu with 5 levels
 * - Separate score tracking per level
 * - Star ratings based on score
 * - Exactly 3 hearts for lives
 * - Pause/Resume (P or ESC)
 * - Return to menu (M key)
 * 
 * @author Breakout Team
 * @version 2.0
 */
public class Main extends Application {
    private GameModel model;
    private GameView view;
    private GameController controller;
    private AnimationTimer gameLoop;
    private long lastUpdate;
    
    @Override
    public void start(Stage primaryStage) {
        GameConfig config = GameConfig.getInstance();
        
        // Initialize MVC components
        model = new GameModel();
        view = new GameView(model, config.getWindowWidth(), config.getWindowHeight());
        controller = new GameController(model);
        
        // Setup scene with keyboard input
        Scene scene = new Scene(view, config.getWindowWidth(), config.getWindowHeight());
        setupInputHandlers(scene);
        
        // Configure stage
        primaryStage.setTitle("Breakout Enhanced v2.0");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // Request focus for keyboard input
        view.getCanvas().requestFocus();
        
        // Start game loop
        startGameLoop();
    }
    
    /**
     * Setup keyboard input handlers
     */
    private void setupInputHandlers(Scene scene) {
        // Key pressed (for controls)
        scene.setOnKeyPressed(event -> {
            controller.handleKeyPressed(event.getCode());
            event.consume();
        });
        
        // Key released
        scene.setOnKeyReleased(event -> {
            controller.handleKeyReleased(event.getCode());
            event.consume();
        });
        
        // Key typed (for name input - captures actual characters)
        scene.setOnKeyTyped(event -> {
            controller.handleKeyTyped(event);
            event.consume();
        });
        
        // Ensure canvas can receive focus
        view.getCanvas().setFocusTraversable(true);
    }
    
    /**
     * Main game loop using AnimationTimer
     */
    private void startGameLoop() {
        lastUpdate = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                deltaTime = Math.min(deltaTime, 0.1);
                
                // Update phase
                controller.update();
                model.update();
                
                // Render phase
                view.render();
            }
        };
        
        gameLoop.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
