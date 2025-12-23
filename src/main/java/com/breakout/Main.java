package com.breakout;

import com.breakout.audio.AudioManager;
import com.breakout.config.GameConfig;
import com.breakout.controller.GameController;
import com.breakout.database.DatabaseManager;
import com.breakout.model.GameModel;
import com.breakout.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private GameModel model;
    private GameView view;
    private GameController controller;
    private AnimationTimer gameLoop;
    private long lastUpdate;

    @Override
    public void start(Stage primaryStage) {
        GameConfig config = GameConfig.getInstance();

        // Initialize database
        DatabaseManager.getInstance();

        // Initialize MVC components
        model = new GameModel();
        view = new GameView(model, config.getWindowWidth(), config.getWindowHeight());
        controller = new GameController(model);

        // Setup scene with keyboard input
        Scene scene = new Scene(view, config.getWindowWidth(), config.getWindowHeight());
        setupInputHandlers(scene);

        // Configure stage
        primaryStage.setTitle("Breakout Enhanced v3.0");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Request focus for keyboard input
        view.getCanvas().requestFocus();

        // Start game loop
        startGameLoop();

        // Handle window close
        primaryStage.setOnCloseRequest(event -> cleanup());
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            controller.handleKeyPressed(event.getCode());
            event.consume();
        });

        scene.setOnKeyReleased(event -> {
            controller.handleKeyReleased(event.getCode());
            event.consume();
        });

        scene.setOnKeyTyped(event -> {
            controller.handleKeyTyped(event);
            event.consume();
        });

        view.getCanvas().setFocusTraversable(true);
    }

    private void startGameLoop() {
        lastUpdate = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                deltaTime = Math.min(deltaTime, 0.1);

                controller.update();
                model.update();
                view.render();
            }
        };

        gameLoop.start();
    }

    private void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        AudioManager.getInstance().dispose();
        DatabaseManager.getInstance().close();
        System.out.println("Game closed. Thanks for playing!");
    }

    @Override
    public void stop() {
        cleanup();
    }

    public static void main(String[] args) {
        // ========== TEST DE CONNEXION MySQL ==========
        System.out.println("=================================");
        System.out.println("Test de connexion MySQL...");
        System.out.println("=================================");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/breakout_game?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    "root",
                    ""  // Mot de passe (vide pour XAMPP)
            );

            System.out.println("✅ Connexion MySQL réussie !");
            System.out.println("   Base : breakout_game");

            // Vérifier les tables
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            java.sql.ResultSet tables = metaData.getTables("breakout_game", null, "%", new String[]{"TABLE"});

            System.out.println("\n📋 Tables :");
            while (tables.next()) {
                System.out.println("   - " + tables.getString("TABLE_NAME"));
            }

            conn.close();
            System.out.println("=================================\n");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL non trouvé !");
            System.out.println("   Erreur : " + e.getMessage());
        } catch (java.sql.SQLException e) {
            System.out.println("❌ Erreur connexion : " + e.getMessage());
            System.out.println("\n🔧 Vérifie :");
            System.out.println("   1. MySQL/XAMPP démarré ?");
            System.out.println("   2. Base 'breakout_game' créée ?");
            System.out.println("   3. Mot de passe correct ?");
        }
        // ========== FIN TEST ==========

        launch(args);
    }
}