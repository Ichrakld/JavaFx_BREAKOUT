package com.breakout.audio;

import com.breakout.config.GameConfig;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Audio Manager for Breakout Game
 *
 * Design Pattern: Singleton + Facade
 * - Manages background music and sound effects
 * - Provides simple interface for audio playback
 * - Handles audio resource loading and caching
 *
 * Sound Categories:
 * - Background Music: Loops during gameplay
 * - Sound Effects: Short clips for game events
 */
public class AudioManager {
    private static AudioManager instance;
    private GameConfig config;

    // Media players
    private MediaPlayer musicPlayer;
    private Map<SoundEffect, MediaPlayer> sfxPlayers;

    // Current music track
    private MusicTrack currentTrack;

    /**
     * Available sound effects
     */
    public enum SoundEffect {
        BALL_HIT_PADDLE("ball_paddle.wav"),
        BALL_HIT_BRICK("ball_brick.wav"),
        BALL_HIT_WALL("ball_wall.wav"),
        BRICK_DESTROY("brick_destroy.wav"),
        POWER_UP_COLLECT("powerup.wav"),
        PENALTY_COLLECT("penalty.wav"),
        LEVEL_COMPLETE("level_complete.wav"),
        GAME_OVER("game_over.wav"),
        VICTORY("victory.wav"),
        MENU_SELECT("menu_select.wav"),
        MENU_CONFIRM("menu_confirm.wav"),
        LIFE_LOST("life_lost.wav"),
        BALL_LAUNCH("ball_launch.wav");

        private final String filename;

        SoundEffect(String filename) {
            this.filename = filename;
        }

        public String getFilename() { return filename; }
    }

    /**
     * Available music tracks
     */
    public enum MusicTrack {
        MENU("menu_music.mp3"),
        LEVEL_1("level1_music.mp3"),
        LEVEL_2("level2_music.mp3"),
        LEVEL_3("level3_music.mp3"),
        LEVEL_4("level4_music.mp3"),
        LEVEL_5("level5_music.mp3"),
        VICTORY("victory_music.mp3"),
        GAME_OVER("gameover_music.mp3");

        private final String filename;

        MusicTrack(String filename) {
            this.filename = filename;
        }

        public String getFilename() { return filename; }

        public static MusicTrack forLevel(int level) {
            return switch (level) {
                case 1 -> LEVEL_1;
                case 2 -> LEVEL_2;
                case 3 -> LEVEL_3;
                case 4 -> LEVEL_4;
                case 5 -> LEVEL_5;
                default -> LEVEL_1;
            };
        }
    }

    private AudioManager() {
        this.config = GameConfig.getInstance();
        this.sfxPlayers = new HashMap<>();
        preloadSoundEffects();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Preload sound effects for quick playback
     */
    private void preloadSoundEffects() {
        for (SoundEffect sfx : SoundEffect.values()) {
            try {
                URL resource = getClass().getResource("/sounds/" + sfx.getFilename());
                if (resource != null) {
                    Media media = new Media(resource.toExternalForm());
                    MediaPlayer player = new MediaPlayer(media);
                    player.setVolume(config.getSfxVolume());
                    sfxPlayers.put(sfx, player);
                }
            } catch (Exception e) {
                // Sound file not found - will use fallback
                System.out.println("Sound not found: " + sfx.getFilename() + " (using fallback)");
            }
        }
    }

    // ==================== MUSIC CONTROLS ====================

    /**
     * Play background music
     */
    public void playMusic(MusicTrack track) {
        if (!config.isMusicEnabled()) return;
        if (currentTrack == track && musicPlayer != null &&
                musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return; // Already playing this track
        }

        stopMusic();

        try {
            URL resource = getClass().getResource("/sounds/" + track.getFilename());
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setVolume(config.getMusicVolume());
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop
                musicPlayer.play();
                currentTrack = track;
            } else {
                System.out.println("Music not found: " + track.getFilename());
            }
        } catch (Exception e) {
            System.out.println("Cannot play music: " + e.getMessage());
        }
    }

    /**
     * Play music for specific level
     */
    public void playLevelMusic(int level) {
        playMusic(MusicTrack.forLevel(level));
    }

    /**
     * Stop current music
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
            currentTrack = null;
        }
    }

    /**
     * Pause music
     */
    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    /**
     * Resume music
     */
    public void resumeMusic() {
        if (musicPlayer != null && config.isMusicEnabled()) {
            musicPlayer.play();
        }
    }

    /**
     * Set music volume
     */
    public void setMusicVolume(double volume) {
        config.setMusicVolume(volume);
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
    }

    /**
     * Toggle music on/off
     */
    public void toggleMusic() {
        config.setMusicEnabled(!config.isMusicEnabled());
        if (config.isMusicEnabled()) {
            resumeMusic();
        } else {
            pauseMusic();
        }
    }

    // ==================== SOUND EFFECTS ====================

    /**
     * Play a sound effect
     */
    public void playSfx(SoundEffect sfx) {
        if (!config.isSoundEffectsEnabled()) return;

        MediaPlayer player = sfxPlayers.get(sfx);
        if (player != null) {
            player.stop();
            player.seek(Duration.ZERO);
            player.setVolume(config.getSfxVolume());
            player.play();
        } else {
            // Generate simple beep as fallback
            playFallbackSound(sfx);
        }
    }

    /**
     * Play fallback sound when audio file not available
     */
    private void playFallbackSound(SoundEffect sfx) {
        // In a real implementation, you could use Java's built-in beep
        // or generate a simple tone using AudioSystem
        System.out.println("♪ " + sfx.name());
    }

    /**
     * Set sound effects volume
     */
    public void setSfxVolume(double volume) {
        config.setSfxVolume(volume);
        for (MediaPlayer player : sfxPlayers.values()) {
            player.setVolume(volume);
        }
    }

    /**
     * Toggle sound effects on/off
     */
    public void toggleSfx() {
        config.setSoundEffectsEnabled(!config.isSoundEffectsEnabled());
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Play brick hit sound
     */
    public void playBrickHit() {
        playSfx(SoundEffect.BALL_HIT_BRICK);
    }

    /**
     * Play brick destroy sound
     */
    public void playBrickDestroy() {
        playSfx(SoundEffect.BRICK_DESTROY);
    }

    /**
     * Play paddle hit sound
     */
    public void playPaddleHit() {
        playSfx(SoundEffect.BALL_HIT_PADDLE);
    }

    /**
     * Play power-up collect sound
     */
    public void playPowerUp() {
        playSfx(SoundEffect.POWER_UP_COLLECT);
    }

    /**
     * Play penalty sound
     */
    public void playPenalty() {
        playSfx(SoundEffect.PENALTY_COLLECT);
    }

    /**
     * Play level complete sound
     */
    public void playLevelComplete() {
        stopMusic();
        playSfx(SoundEffect.LEVEL_COMPLETE);
    }

    /**
     * Play game over sound
     */
    public void playGameOver() {
        stopMusic();
        playSfx(SoundEffect.GAME_OVER);
    }

    /**
     * Play victory sound
     */
    public void playVictory() {
        stopMusic();
        playSfx(SoundEffect.VICTORY);
        playMusic(MusicTrack.VICTORY);
    }

    /**
     * Play life lost sound
     */
    public void playLifeLost() {
        playSfx(SoundEffect.LIFE_LOST);
    }

    /**
     * Cleanup resources
     */
    public void dispose() {
        stopMusic();
        for (MediaPlayer player : sfxPlayers.values()) {
            player.dispose();
        }
        sfxPlayers.clear();
    }

    // ==================== STATUS METHODS ====================

    public boolean isMusicPlaying() {
        return musicPlayer != null && musicPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public boolean isMusicEnabled() {
        return config.isMusicEnabled();
    }

    public boolean isSfxEnabled() {
        return config.isSoundEffectsEnabled();
    }
}