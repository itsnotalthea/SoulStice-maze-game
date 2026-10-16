package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    private final GameSettings settings;

    private Music menuMusic;
    private Music gameplayMusic;
    private Sound winSound;
    private Sound loseSound;

    public AudioManager(GameSettings settings) {
        this.settings = settings;

        // Load the music and sound effects from the assets folder.
        menuMusic = Gdx.audio.newMusic(
            Gdx.files.internal("backgroundMusic.mp3")
        );

        gameplayMusic = Gdx.audio.newMusic(
            Gdx.files.internal("ingameMusic.mp3")
        );

        winSound = Gdx.audio.newSound(
            Gdx.files.internal("win.wav")
        );

        loseSound = Gdx.audio.newSound(
            Gdx.files.internal("lose.wav")
        );

        // Keep background music playing continuously.
        menuMusic.setLooping(true);
        gameplayMusic.setLooping(true);

        updateMusicVolume();
    }

    public void playMenuMusic() {
        if (!menuMusic.isPlaying()) {
            playMusic(menuMusic);
        }
    }

    public void playGameplayMusic() {
        if (!gameplayMusic.isPlaying()) {
            playMusic(gameplayMusic);
        }
    }

    // Stop the previous track before playing the selected one.
    private void playMusic(Music selectedMusic) {
        if (menuMusic.isPlaying()) {
            menuMusic.stop();
        }

        if (gameplayMusic.isPlaying()) {
            gameplayMusic.stop();
        }

        selectedMusic.setVolume(settings.musicVolume);
        selectedMusic.play();
    }

    // Apply the selected volume to both background tracks.
    public void updateMusicVolume() {
        menuMusic.setVolume(settings.musicVolume);
        gameplayMusic.setVolume(settings.musicVolume);
    }

    public void playWinSound() {
        if (settings.soundEffects) {
            winSound.play();
        }
    }

    public void playLoseSound() {
        if (settings.soundEffects) {
            loseSound.play();
        }
    }

    public void pauseGameplayMusic() {
        if (gameplayMusic.isPlaying()) {
            gameplayMusic.pause();
        }
    }

    public void resumeGameplayMusic() {
        if (!gameplayMusic.isPlaying()) {
            gameplayMusic.play();
        }
    }

    // Release audio resources when the game closes.
    public void dispose() {
        menuMusic.dispose();
        gameplayMusic.dispose();
        winSound.dispose();
        loseSound.dispose();
    }
}
