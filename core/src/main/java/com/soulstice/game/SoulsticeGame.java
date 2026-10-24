package com.soulstice.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SoulsticeGame extends Game {

    private static final int TOTAL_LEVELS = 21;
    private static final int STREAK_LENGTH = 5;

    public String playerName = "";
    public int unlockedLevel = 1;
    public final GameSettings settings = new GameSettings();
    public AudioManager audio;

    private int currentLevel = 1;
    private int consecutiveWins;
    private final EnumSet<Achievement> unlockedAchievements = EnumSet.noneOf(Achievement.class);
    private MazeScreen activeMaze;

    @Override
    public void create() {
        SaveSystem.load(this);
        audio = new AudioManager(settings);

        showMainMenu();
    }

    private void swap(Screen next, boolean disposeOld) {
        Screen old = getScreen();
        setScreen(next);
        if (disposeOld && old != null) old.dispose();
    }

    public void showMainMenu() {
        if (activeMaze != null) recordFailedAttempt();
        disposeActiveMaze();
        swap(new MainMenuScreen(this), true);
        audio.playMenuMusic();

    }

    public void showLevelMap() {
        disposeActiveMaze();
        swap(new LevelMapScreen(this), true);
        audio.playMenuMusic();

    }

    public void showSettings(boolean fromPause) {
        swap(new SettingsScreen(this, fromPause), true);
    }

    public void showAchievements() {
        swap(new AchievementsScreen(this), true);
        audio.playMenuMusic();
    }

    public void showTutorial() {
        swap(new TutorialScreen(this), true);
        audio.playMenuMusic();
    }

    public void startLevel(int level) {
        currentLevel = level;
        disposeActiveMaze();
        activeMaze = new MazeScreen(level, this::onLevelWon, this::showPause, this::onTimeUp);
        swap(activeMaze, true);
        audio.playGameplayMusic();
    }

    public void showPause() {
        setScreen(new PauseMenuScreen(this));
        audio.pauseGameplayMusic();

    }

    public void resumeLevel() {
        Screen pauseScreen = getScreen();
        setScreen(activeMaze);
        audio.resumeGameplayMusic();

        if (pauseScreen != null && pauseScreen != activeMaze) pauseScreen.dispose();
    }

    public void quitCurrentLevel() {
        int level = currentLevel;
        recordFailedAttempt();
        audio.pauseGameplayMusic();
        disposeActiveMaze();
        swap(new GameOverScreen(this, level), true);
    }

    private void onTimeUp() {
        int level = currentLevel;
        recordFailedAttempt();
        audio.pauseGameplayMusic();
        disposeActiveMaze();
        swap(new GameOverScreen(this, level, true), true);
    }

    private void onLevelWon() {
        float elapsedSeconds = activeMaze == null ? Float.MAX_VALUE : activeMaze.getElapsedSeconds();
        consecutiveWins++;

        if (currentLevel >= unlockedLevel && unlockedLevel < TOTAL_LEVELS) {
            unlockedLevel = currentLevel + 1;
        }

        List<Achievement> notifications = new ArrayList<Achievement>();
        unlockAchievement(Achievement.FIRST_WIN, notifications);
        if (consecutiveWins >= STREAK_LENGTH) {
            unlockAchievement(Achievement.STREAK, notifications);
        }
        if (elapsedSeconds <= 15f) {
            unlockAchievement(Achievement.TIME_BEATER, notifications);
        }
        if (unlockedLevel >= TOTAL_LEVELS) {
            unlockAchievement(Achievement.SOULSTICE_LEGEND, notifications);
        }
        if (hasAllCoreAchievements()) {
            unlockAchievement(Achievement.ALL_ACHIEVEMENTS, notifications);
        }
        SaveSystem.save(this);

        int level = currentLevel;
        disposeActiveMaze();
        if (notifications.isEmpty()) {
            showLevelComplete(level);
        } else {
            swap(new AchievementNotificationScreen(this, notifications, new Runnable() {
                @Override
                public void run() {
                    showLevelComplete(level);
                }
            }), true);
        }
    }

    private void unlockAchievement(Achievement achievement, List<Achievement> notifications) {
        if (unlockedAchievements.add(achievement)) notifications.add(achievement);
    }

    private void recordFailedAttempt() {
        if (consecutiveWins != 0) {
            consecutiveWins = 0;
            SaveSystem.save(this);
        }
    }

    private void showLevelComplete(int level) {
        swap(new LevelCompleteScreen(this, level), true);
        audio.playMenuMusic();
    }

    public boolean isAchievementUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement);
    }

    void setAchievementUnlocked(Achievement achievement, boolean unlocked) {
        if (unlocked) unlockedAchievements.add(achievement);
        else unlockedAchievements.remove(achievement);
    }

    public boolean hasAllCoreAchievements() {
        return unlockedAchievements.contains(Achievement.FIRST_WIN)
            && unlockedAchievements.contains(Achievement.STREAK)
            && unlockedAchievements.contains(Achievement.TIME_BEATER)
            && unlockedAchievements.contains(Achievement.SOULSTICE_LEGEND);
    }

    public int getConsecutiveWins() {
        return consecutiveWins;
    }

    void setConsecutiveWins(int consecutiveWins) {
        this.consecutiveWins = Math.max(0, consecutiveWins);
    }

    private void disposeActiveMaze() {
        if (activeMaze != null && getScreen() != activeMaze) {
            activeMaze.dispose();
        }
        activeMaze = null;
    }

    @Override
    public void dispose() {
        if (audio != null) {
            audio.dispose();
        }

        if (getScreen() != null) {
            getScreen().dispose();
        }
    }    
}
