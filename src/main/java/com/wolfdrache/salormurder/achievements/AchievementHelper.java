package com.wolfdrache.salormurder.achievements;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.manager.StatsManager;
import com.wolfdrache.serverapi.achievements.Achievement;
import com.wolfdrache.serverapi.achievements.Achievement.IAchievementType;
import com.wolfdrache.serverapi.achievements.AchievementCategory;
import com.wolfdrache.serverapi.achievements.AchievementService;

public class AchievementHelper {
    private static StatsManager statsManager;
    public static void setStatsManager(StatsManager statsManager) {
        AchievementHelper.statsManager = statsManager;
    }
    public enum AchievementType implements IAchievementType {
        BOW_AFTER_RANDOMKILL(null);
        private final @Nullable Function<OfflinePlayer, Integer> syncFunction;

        AchievementType(@Nullable Function<OfflinePlayer, Integer> syncFunction) {
            this.syncFunction = syncFunction;
        }
        @Override
        public String getName() {
            return name();
        }
        @Override 
        public @Nullable Function<OfflinePlayer, Integer> getSyncFunction() {
            return syncFunction;
        }
    }

    private static AchievementService achievementService;

    public static AchievementCategory achievementCategory;
    private static final Achievement BOW_AFTER_RANDOMKILL = new Achievement(1, "§4Dein Tot kann ich mir leisten", "Randomkille einen Spieler mit genug Emeralds im Inventar \num direkt einen neuen Bogen zu erhalten", Material.EMERALD, Map.of(AchievementType.BOW_AFTER_RANDOMKILL, 1), 100);

    public static void setService(AchievementService achievementService) {
        AchievementHelper.achievementService = achievementService;
        achievementCategory = achievementService.registerAchievementCategory("SailorsMurder", Material.TRIDENT, "§3§lSailors§c§lMurder", Set.of(AchievementType.values()));

        achievementService.registerAchievement(BOW_AFTER_RANDOMKILL, achievementCategory);
    }

    public static void updateAchievementProgress(Player player, int progress, AchievementType type) {
        achievementService.updateAchievementTypeProgress(player, achievementCategory, type, progress);
    }
}
