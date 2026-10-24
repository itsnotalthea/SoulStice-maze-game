package com.soulstice.game;

public enum Achievement {
    FIRST_WIN(
        "First Win",
        "Win your first maze.",
        "FIRST WIN",
        "Every journey begins with a single victory!",
        "firstWinCard.png",
        "nextButton (for 1-4 achievements).png"
    ),
    STREAK(
        "Streak",
        "Complete five mazes consecutively without losing.",
        "STREAK",
        "Five mazes, zero defeats. Your focus is unmatched!",
        "streakwithnext.png",
        null
    ),
    TIME_BEATER(
        "Time Beater",
        "Complete a maze in 15 seconds or less.",
        "TIME BEATER",
        "Time waits for no one, but you were faster!",
        "timeBeaterCard.png",
        "nextButton (for 1-4 achievements).png"
    ),
    SOULSTICE_LEGEND(
        "Soulstice Legend",
        "Unlock all 21 levels of the game.",
        "SOULSTICE LEGEND",
        "Every path explored, every level unlocked. You have mastered Soulstice!",
        "soulsticeLegendCard.png",
        "nextButton (for trophy).png"
    ),
    ALL_ACHIEVEMENTS(
        "All Achievements",
        "Collect every other achievement.",
        "ALL ACHIEVEMENTS",
        "No challenge left behind. You have earned every achievement in Soulstice!",
        "allAchievementsCard.png",
        "nextButton (for trophy).png"
    );

    public final String displayName;
    public final String description;
    public final String popupTitle;
    public final String popupDescription;
    public final String popupAsset;
    public final String nextButtonAsset;

    Achievement(
        String displayName,
        String description,
        String popupTitle,
        String popupDescription,
        String popupAsset,
        String nextButtonAsset
    ) {
        this.displayName = displayName;
        this.description = description;
        this.popupTitle = popupTitle;
        this.popupDescription = popupDescription;
        this.popupAsset = popupAsset;
        this.nextButtonAsset = nextButtonAsset;
    }
}
