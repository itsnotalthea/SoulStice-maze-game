package com.soulstice.game;

import java.util.prefs.Preferences;

// Saves player progress and settings using Java Preferences API

public final class SaveSystem {

    private static final String NODE = "com/soulstice/save";

    private static final String KEY_NAME = "playerName";
    private static final String KEY_UNLOCKED = "unlockedLevel";
    private static final String KEY_MUSIC = "musicVolume";
    private static final String KEY_SFX = "soundEffects";

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
        } catch (Exception e) {
            // If No save available, then keep the defaults.
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
            p.flush();
        } catch (Exception e) {
        }
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
