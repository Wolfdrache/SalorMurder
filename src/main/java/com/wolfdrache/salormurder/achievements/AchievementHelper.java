package com.wolfdrache.salormurder.achievements;

import java.util.HashMap;
import java.util.List;
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

    public enum AchievementType implements IAchievementType {
        BOW_AFTER_RANDOMKILL(null),
        RANDOMKILLED_AS_DETECTIVE(null),
        WRONG_MURDER(null),
        POINTS (p -> statsManager.getPlayerStats(p).getPoints()),
        ROUNDS_WON (p -> statsManager.getPlayerStats(p).getRoundsWon()),
        MURDERER_WINS(p -> statsManager.getPlayerStats(p).roundsWonMurderer),
        INNOCENT_WINS(p -> statsManager.getPlayerStats(p).roundsWonInnocent),
        KIILS_AS_MURDER(p -> statsManager.getPlayerStats(p).getKillsAsMurderer()),
        MURDERERS_KILLED(p -> statsManager.getPlayerStats(p).murderersKilled),
        ROUNDS_PLAYED(p -> statsManager.getPlayerStats(p).getRoundsPlayed()),
        CLUES (null);

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
    private static final Achievement BOW_AFTER_RANDOMKILL = new Achievement(1, "§4Dein Tot kann ich mir leisten", List.of("Randomkille einen Spieler mit genug Emeralds im Inventar", "um direkt einen neuen Bogen zu erhalten"), Material.EMERALD, Map.of(AchievementType.BOW_AFTER_RANDOMKILL, () -> 1), 100, true);
    private static final Achievement iWasDetective = new Achievement(2, "§3Ich war Detective", List.of("Werde als Detective random gekillt"), Material.BOW, Map.of(AchievementType.RANDOMKILLED_AS_DETECTIVE, () -> 1), 100, true);
    private static final Achievement iWasntMurder = new Achievement(3, "§cIch war gar nicht Mörder??", List.of("Randomkille in einer Runde alle", "Spieler und sei der einzige Überlebende", "(mindestens 5 Spieler in der Runde)"), Material.TRIDENT, Map.of(AchievementType.WRONG_MURDER, () -> 1), 1000, true);
    private static final Achievement reachLeaderBoard = new Achievement(4, "§6Leaderboard erreicht", "Erreiche das Leaderboard", Material.GOLD_BLOCK, Map.of(AchievementType.POINTS, () -> statsManager.getLowestPointsForLeaderboard()), 1000);
    private static final Achievement winn1 = new Achievement(5, "§aErster Sieg", "Gewinne deine erste Runde", Material.DIAMOND, Map.of(AchievementType.ROUNDS_WON, () -> 1), 100);
    private static final Achievement winn20 = new Achievement(6, "§a20 Siege", "Gewinne 20 Runden", Material.EMERALD, Map.of(AchievementType.ROUNDS_WON, () -> 20), 100);
    private static final Achievement winn50 = new Achievement(7, "§a50 Siege", "Gewinne 50 Runden", Material.NETHERITE_INGOT, Map.of(AchievementType.ROUNDS_WON, () -> 50), 100);

    private static final Achievement innoWinn10 = new Achievement(8, "§a10 Siege als Unschuldiger", "Gewinne 10 Runden als Unschuldiger", Material.EMERALD, Map.of(AchievementType.INNOCENT_WINS, () -> 10), 100);
    private static final Achievement innoWinn50 = new Achievement(9, "§a50 Siege als Unschuldiger", "Gewinne 50 Runden als Unschuldiger", Material.BOW, Map.of(AchievementType.INNOCENT_WINS, () -> 50), 100);
    private static final Achievement innoWinn100 = new Achievement(10, "§a100 Siege als Unschuldiger", "Gewinne 100 Runden als Unschuldiger", Material.TRIDENT, Map.of(AchievementType.INNOCENT_WINS, () -> 100), 100);

    private static final Achievement murdererWinn10 = new Achievement(11, "§c10 Siege als Mörder", "Gewinne 10 Runden als Mörder", Material.IRON_SWORD, Map.of(AchievementType.MURDERER_WINS, () -> 10), 100);
    private static final Achievement murdererWinn50 = new Achievement(12, "§c50 Siege als Mörder", "Gewinne 50 Runden als Mörder", Material.DIAMOND_SWORD, Map.of(AchievementType.MURDERER_WINS, () -> 50), 100);
    private static final Achievement murdererWinn100 = new Achievement(13, "§c100 Siege als Mörder", "Gewinne 100 Runden als Mörder", Material.NETHERITE_SWORD, Map.of(AchievementType.MURDERER_WINS, () -> 100), 100);

    private static final Achievement killsMurderer20 = new Achievement(14, "§420 MörderKills", "Erziele 20 Kills als Mörder", Material.IRON_SWORD, Map.of(AchievementType.KIILS_AS_MURDER, () -> 20), 100);
    private static final Achievement killsMurderer50 = new Achievement(15, "§450 MörderKills", "Erziele 50 Kills als Mörder", Material.DIAMOND_SWORD, Map.of(AchievementType.KIILS_AS_MURDER, () -> 50), 100);
    private static final Achievement killsMurderer100 = new Achievement(16, "§4100 MörderKills", "Erziele 100 Kills als Mörder", Material.NETHERITE_SWORD, Map.of(AchievementType.KIILS_AS_MURDER, () -> 100), 100);

    private static final Achievement killsInnocent20 = new Achievement(17, "§420 Mörder getötet", "Töte 20x den Mörder", Material.IRON_SWORD, Map.of(AchievementType.MURDERERS_KILLED, () -> 20), 100);
    private static final Achievement killsInnocent50 = new Achievement(18, "§450 Mörder getötet", "Töte 50x den Mörder", Material.DIAMOND_SWORD, Map.of(AchievementType.MURDERERS_KILLED, () -> 50), 100);
    private static final Achievement killsInnocent100 = new Achievement(19, "§4100 Mörder getötet", "Töte 100x den Mörder", Material.NETHERITE_SWORD, Map.of(AchievementType.MURDERERS_KILLED, () -> 100), 100);

    private static final Achievement playedRounds100 = new Achievement(20, "§4100 Gespielte Runden", "Spiele 100 Runden", Material.EMERALD, Map.of(AchievementType.ROUNDS_PLAYED, () -> 100), 100);
    private static final Achievement playedRounds200 = new Achievement(21, "§4200 Gespielte Runden", "Spiele 200 Runden", Material.IRON_SWORD, Map.of(AchievementType.ROUNDS_PLAYED, () -> 200), 100);
    private static final Achievement playedRounds500 = new Achievement(22, "§4500 Gespielte Runden", "Spiele 500 Runden", Material.BOW, Map.of(AchievementType.ROUNDS_PLAYED, () -> 500), 100);
    private static final Achievement playedRounds1000 = new Achievement(23, "§41000 Gespielte Runden", "Spiele 1000 Runden", Material.TRIDENT, Map.of(AchievementType.ROUNDS_PLAYED, () -> 1000), 100);

    private static final Achievement foundClues500 = new Achievement(24, "§a500 Emeralds gefunden", "Sammle 500 Emeralds", Material.EMERALD_ORE, Map.of(AchievementType.CLUES, () -> 500), 100);
    private static final Achievement foundClues1000 = new Achievement(25, "§a1000 Emeralds gefunden", "Sammle 1000 Emeralds", Material.DEEPSLATE_EMERALD_ORE, Map.of(AchievementType.CLUES, () -> 1000), 100);
    private static final Achievement foundClues2000 = new Achievement(26, "§a2000 Emeralds gefunden", "Sammle 2000 Emeralds", Material.EMERALD, Map.of(AchievementType.CLUES, () -> 2000), 100);
    private static final Achievement foundClues5000 = new Achievement(27, "§a5000 Emeralds gefunden", "Sammle 5000 Emeralds", Material.EMERALD_BLOCK, Map.of(AchievementType.CLUES, () -> 5000), 100);

    public static void setStatics(AchievementService achievementService, StatsManager statsManager) {
        AchievementHelper.achievementService = achievementService;
        AchievementHelper.statsManager = statsManager;
        achievementCategory = achievementService.registerAchievementCategory("SailorsMurder", Material.TRIDENT, "§3§lSailors§c§lMurder", Set.of(AchievementType.values()));

        achievementService.registerAchievement(BOW_AFTER_RANDOMKILL, achievementCategory);
        achievementService.registerAchievement(iWasDetective, achievementCategory);
        achievementService.registerAchievement(iWasntMurder, achievementCategory);
        achievementService.registerAchievement(reachLeaderBoard, achievementCategory);
        achievementService.registerAchievement(winn1, achievementCategory);
        achievementService.registerAchievement(winn20, achievementCategory);
        achievementService.registerAchievement(winn50, achievementCategory);
        achievementService.registerAchievement(innoWinn10, achievementCategory);
        achievementService.registerAchievement(innoWinn50, achievementCategory);
        achievementService.registerAchievement(innoWinn100, achievementCategory);
        achievementService.registerAchievement(murdererWinn10, achievementCategory);
        achievementService.registerAchievement(murdererWinn50, achievementCategory);
        achievementService.registerAchievement(murdererWinn100, achievementCategory);
        achievementService.registerAchievement(killsMurderer20, achievementCategory);
        achievementService.registerAchievement(killsMurderer50, achievementCategory);
        achievementService.registerAchievement(killsMurderer100, achievementCategory);
        achievementService.registerAchievement(killsInnocent20, achievementCategory);
        achievementService.registerAchievement(killsInnocent50, achievementCategory);
        achievementService.registerAchievement(killsInnocent100, achievementCategory);
        achievementService.registerAchievement(playedRounds100, achievementCategory);
        achievementService.registerAchievement(playedRounds200, achievementCategory);
        achievementService.registerAchievement(playedRounds500, achievementCategory);
        achievementService.registerAchievement(playedRounds1000, achievementCategory);
        
        achievementService.registerAchievement(foundClues500, achievementCategory);
        achievementService.registerAchievement(foundClues1000, achievementCategory);
        achievementService.registerAchievement(foundClues2000, achievementCategory);
        achievementService.registerAchievement(foundClues5000, achievementCategory);
    }

    public static void updateAchievementProgress(Player player, int progress, AchievementType type) {
        achievementService.updateAchievementTypeProgress(player, achievementCategory, type, progress);
    }

    private static final Map<Player, AchievementTracker> playerAchievementTrackers = new HashMap<>();
    public static AchievementTracker getPlayerAchievementTracker(Player player) {
        return playerAchievementTrackers.get(player);
    }
    public static void initializePlayerAchievementTracker(Player player, int neededRandomKillsInRound) {
        if (!playerAchievementTrackers.containsKey(player)) {
            playerAchievementTrackers.put(player, new AchievementTracker(neededRandomKillsInRound));
        }
    }
    public static void removePlayerAchievementTracker(Player player) {
        playerAchievementTrackers.remove(player);
    }
}
