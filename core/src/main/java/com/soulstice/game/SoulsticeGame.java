package com.soulstice.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class SoulsticeGame extends Game {

    public String playerName = "";
    public int unlockedLevel = 1;

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
        swap(new SettingsScreen(this, fromPause), !fromPause);
    }

    public void startLevel(int level) {
        currentLevel = level;
        disposeActiveMaze();
        activeMaze = new MazeScreen(level, this::onLevelWon, this::showPause);
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

    private void onLevelWon() {
        if (currentLevel >= unlockedLevel && unlockedLevel < 21) {
            unlockedLevel = currentLevel + 1;
        }
        showLevelMap();
    }

    private void disposeActiveMaze() {
        if (activeMaze != null && getScreen() != activeMaze) {
            activeMaze.dispose();
        }
        activeMaze = null;
    }
}
