package com.soulstice.game;

import com.badlogic.gdx.Gdx;

import java.util.prefs.Preferences;

// Saves player progress and settings using Java Preferences API

public final class SaveSystem {

    private static final String NODE = "com/soulstice/save";

    private static final String KEY_NAME = "playerName";
    private static final String KEY_UNLOCKED = "unlockedLevel";
    private static final String KEY_MUSIC = "musicVolume";
    private static final String KEY_SFX = "soundEffects";
    private static final String KEY_CONSECUTIVE_WINS = "consecutiveWins";

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 21;

    private SaveSystem() {}

    private static Preferences prefs() {
        return Preferences.userRoot().node(NODE);
    }

    // Reads saved values into the game (or leaves defaults if nothing is stored)
    public static void load(SoulsticeGame game) {
        try {
            Preferences p = prefs();
            game.playerName = p.get(KEY_NAME, "");
            game.unlockedLevel = clampLevel(p.getInt(KEY_UNLOCKED, MIN_LEVEL));
            game.settings.musicVolume = clampVolume(p.getFloat(KEY_MUSIC, 0.7f));
            game.settings.soundEffects = p.getBoolean(KEY_SFX, true);
            game.setConsecutiveWins(Math.max(0, p.getInt(KEY_CONSECUTIVE_WINS, 0)));
            for (Achievement achievement : Achievement.values()) {
                game.setAchievementUnlocked(achievement,
                    p.getBoolean(achievementKey(achievement), false));
            }

            if (game.unlockedLevel < MAX_LEVEL) {
                game.setAchievementUnlocked(Achievement.SOULSTICE_LEGEND, false);
            }
            if (!game.hasAllCoreAchievements()) {
                game.setAchievementUnlocked(Achievement.ALL_ACHIEVEMENTS, false);
            }
        } catch (Exception e) {
            logFailure("Could not load saved game progress", e);
        }
    }

    // Writes the current player name, progress and settings to disk.
    public static void save(SoulsticeGame game) {
        try {
            Preferences p = prefs();
            p.put(KEY_NAME, game.playerName == null ? "" : game.playerName);
            p.putInt(KEY_UNLOCKED, clampLevel(game.unlockedLevel));
            p.putFloat(KEY_MUSIC, clampVolume(game.settings.musicVolume));
            p.putBoolean(KEY_SFX, game.settings.soundEffects);
            p.putInt(KEY_CONSECUTIVE_WINS, Math.max(0, game.getConsecutiveWins()));
            for (Achievement achievement : Achievement.values()) {
                p.putBoolean(achievementKey(achievement), game.isAchievementUnlocked(achievement));
            }
            p.flush();
        } catch (Exception e) {
            logFailure("Could not save game progress", e);
        }
    }

    private static String achievementKey(Achievement achievement) {
        return "achievement." + achievement.name();
    }

    private static void logFailure(String message, Exception e) {
        if (Gdx.app != null) Gdx.app.error("Soulstice SaveSystem", message, e);
    }

    private static int clampLevel(int level) {
        if (level < MIN_LEVEL) return MIN_LEVEL;
        if (level > MAX_LEVEL) return MAX_LEVEL;
        return level;
    }

    private static float clampVolume(float volume) {
        if (volume < 0f) return 0f;
        if (volume > 1f) return 1f;
        return volume;
    }
}
