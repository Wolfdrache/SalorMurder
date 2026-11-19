package com.wolfdrache.salormurder.achievements;

public class AchievementTracker {
    private static int minRequiredRandomKills;
    public int randomKills = 0;
    public int neededRandomKillsInRound;

    public AchievementTracker(int neededRandomKillsInRound) {
        this.neededRandomKillsInRound = neededRandomKillsInRound;
    }

    public boolean hasReachedNeededRandomKills() {
        return randomKills >= neededRandomKillsInRound && neededRandomKillsInRound >= minRequiredRandomKills;
    }

    public static void setRequiredRandomKills (int requiredKillCount) {
        minRequiredRandomKills = requiredKillCount;
    }
}
