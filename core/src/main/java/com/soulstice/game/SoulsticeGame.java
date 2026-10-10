package com.soulstice.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class SoulsticeGame extends Game {

    public String playerName = "";
    public int unlockedLevel = 1;
    public final GameSettings settings = new GameSettings();

    private int currentLevel = 1;
    private MazeScreen activeMaze;

    @Override
    public void create() {
        showMainMenu();
    }

    private void swap(Screen next, boolean disposeOld) {
        Screen old = getScreen();
        setScreen(next);
        if (disposeOld && old != null) old.dispose();
    }

    public void showMainMenu() {
        disposeActiveMaze();
        swap(new MainMenuScreen(this), true);
    }

    public void showLevelMap() {
        disposeActiveMaze();
        swap(new LevelMapScreen(this), true);
    }

    public void showSettings(boolean fromPause) {
        swap(new SettingsScreen(this, fromPause), true);
    }

    public void startLevel(int level) {
        currentLevel = level;
        disposeActiveMaze();
        activeMaze = new MazeScreen(level, this::onLevelWon, this::showPause, this::onTimeUp);
        swap(activeMaze, true);
    }

    public void showPause() {
        setScreen(new PauseMenuScreen(this));
    }

    public void resumeLevel() {
        Screen pauseScreen = getScreen();
        setScreen(activeMaze);
        if (pauseScreen != null && pauseScreen != activeMaze) pauseScreen.dispose();
    }

    public void quitCurrentLevel() {
        int level = currentLevel;
        disposeActiveMaze();
        swap(new GameOverScreen(this, level), true);
    }

    private void onTimeUp() {
        int level = currentLevel;
        disposeActiveMaze();
        swap(new GameOverScreen(this, level, true), true);
    }

    private void onLevelWon() {
        if (currentLevel >= unlockedLevel && unlockedLevel < 21) {
            unlockedLevel = currentLevel + 1;
        }
        int level = currentLevel;
        disposeActiveMaze();
        swap(new LevelCompleteScreen(this, level), true);
    }

    private void disposeActiveMaze() {
        if (activeMaze != null && getScreen() != activeMaze) {
            activeMaze.dispose();
        }
        activeMaze = null;
    }
}
