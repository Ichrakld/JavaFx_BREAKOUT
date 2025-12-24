package com.breakout.audio;

import com.breakout.config.GameConfig;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private GameConfig config;

    // CRITICAL: Maintain a strong reference to the media player
    // to prevent the Garbage Collector from stopping the music.
    private MediaPlayer musicPlayer;

    // Store sound effects in memory
    private Map<SoundEffect, AudioClip> sfxCache;
    private MusicTrack currentTrack;

    // ==================== ENUMS ====================
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
        SoundEffect(String filename) { this.filename = filename; }
        public String getFilename() { return filename; }
    }

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
        MusicTrack(String filename) { this.filename = filename; }
        public String getFilename() { return filename; }

        public static MusicTrack forLevel(int level) {
            if (level < 1) return LEVEL_1;
            if (level > 5) return LEVEL_5;
            return values()[level]; // Mapping index to enum
        }
    }

    private AudioManager() {
        this.config = GameConfig.getInstance();
        this.sfxCache = new HashMap<>();
        System.out.println("AUDIO: Initializing Manager...");
        preloadSoundEffects();
    }

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ==================== SOUND LOADING ====================

    private void preloadSoundEffects() {
        for (SoundEffect sfx : SoundEffect.values()) {
            try {
                URL resource = getClass().getResource("/sounds/" + sfx.getFilename());
                if (resource != null) {
                    AudioClip clip = new AudioClip(resource.toExternalForm());
                    clip.setVolume(config.getSfxVolume());
                    sfxCache.put(sfx, clip);
                } else {
                    System.err.println("AUDIO ERROR: SFX File missing: /sounds/" + sfx.getFilename());
                }
            } catch (Exception e) {
                System.err.println("AUDIO ERROR: Could not load " + sfx.getFilename());
            }
        }
    }

    // ==================== MUSIC CONTROLS ====================

    public void playMusic(MusicTrack track) {
        if (!config.isMusicEnabled()) {
            System.out.println("AUDIO: Music disabled in config.");
            return;
        }

        // Don't restart if it's already playing
        if (currentTrack == track && musicPlayer != null && musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }

        stopMusic();

        try {
            // 1. debug path
            String path = "/sounds/" + track.getFilename();
            URL resource = getClass().getResource(path);

            if (resource == null) {
                System.err.println("AUDIO CRITICAL: Music file NOT FOUND at: " + path);
                System.err.println("Check: src/main/resources/sounds/" + track.getFilename());
                return;
            }

            System.out.println("AUDIO: Loading music from: " + resource.toExternalForm());

            // 2. Create Media
            Media media = new Media(resource.toExternalForm());

            // 3. Handle Media Errors (Corrupt MP3s)
            media.setOnError(() -> System.err.println("AUDIO MEDIA ERROR: " + media.getError()));

            // 4. Create Player
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setOnError(() -> System.err.println("AUDIO PLAYER ERROR: " + musicPlayer.getError()));

            musicPlayer.setVolume(config.getMusicVolume());
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever

            // 5. Play
            musicPlayer.play();
            currentTrack = track;
            System.out.println("AUDIO: Playing " + track.name());

        } catch (Exception e) {
            System.err.println("AUDIO EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playLevelMusic(int level) {
        System.out.println("AUDIO: Requesting music for Level " + level);
        playMusic(MusicTrack.forLevel(level));
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
            currentTrack = null;
        }
    }

    // ==================== CONTROLS ====================

    public void pauseMusic() { if (musicPlayer != null) musicPlayer.pause(); }
    public void resumeMusic() { if (musicPlayer != null && config.isMusicEnabled()) musicPlayer.play(); }

    public void setMusicVolume(double volume) {
        config.setMusicVolume(volume);
        if (musicPlayer != null) musicPlayer.setVolume(volume);
    }

    public void toggleMusic() {
        boolean newState = !config.isMusicEnabled();
        config.setMusicEnabled(newState);
        System.out.println("AUDIO: Toggled Music to " + newState);
        if (newState) {
            if (currentTrack != null) playMusic(currentTrack); // Restart track
            else resumeMusic();
        } else {
            pauseMusic();
        }
    }

    // ==================== SFX CONTROLS ====================

    public void playSfx(SoundEffect sfx) {
        if (!config.isSoundEffectsEnabled()) return;
        AudioClip clip = sfxCache.get(sfx);
        if (clip != null) clip.play(config.getSfxVolume());
    }

    public void setSfxVolume(double volume) { config.setSfxVolume(volume); }
    public void toggleSfx() { config.setSoundEffectsEnabled(!config.isSoundEffectsEnabled()); }

    // ==================== HELPERS ====================

    public void playBrickHit() { playSfx(SoundEffect.BALL_HIT_BRICK); }
    public void playBrickDestroy() { playSfx(SoundEffect.BRICK_DESTROY); }
    public void playPaddleHit() { playSfx(SoundEffect.BALL_HIT_PADDLE); }
    public void playPowerUp() { playSfx(SoundEffect.POWER_UP_COLLECT); }
    public void playPenalty() { playSfx(SoundEffect.PENALTY_COLLECT); }
    public void playLifeLost() { playSfx(SoundEffect.LIFE_LOST); }

    public void playLevelComplete() { stopMusic(); playSfx(SoundEffect.LEVEL_COMPLETE); }
    public void playGameOver() { stopMusic(); playSfx(SoundEffect.GAME_OVER); }
    public void playVictory() { stopMusic(); playSfx(SoundEffect.VICTORY); playMusic(MusicTrack.VICTORY); }

    public void dispose() { stopMusic(); sfxCache.clear(); }

    // ==================== GETTERS (REQUIRED FOR VIEW) ====================
    public boolean isMusicEnabled() { return config.isMusicEnabled(); }
    public boolean isSfxEnabled() { return config.isSoundEffectsEnabled(); }
}