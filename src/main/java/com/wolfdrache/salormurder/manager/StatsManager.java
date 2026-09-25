package com.wolfdrache.salormurder.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.leaderboard.LeaderBoardFileManager;
import com.wolfdrache.salormurder.leaderboard.LeaderBoardManager;
import com.wolfdrache.salormurder.models.PlayerStats;

public class StatsManager {
    private final FileManager fileManager;
    private final LeaderBoardManager leaderBoardManager;

    private final Map<Player, PlayerStats> playerStatsMap = new HashMap<>();

    public StatsManager(FileManager fileManager, LeaderBoardFileManager leaderBoardFileManager) {
        this.fileManager = fileManager;
        this.leaderBoardManager = new LeaderBoardManager(leaderBoardFileManager, this::getPlayerStats);
    }

    public void loadPlayerStats(Player player) {
        PlayerStats stats = fileManager.loadPlayerStats(player);
        playerStatsMap.put(player, stats);
        leaderBoardManager.onPlayerLoaded(player);
    }

    public void updateLeaderboard() {
        leaderBoardManager.updateLeaderboard();
    }

    public void savePlayerStats(Player player) {
        if (playerStatsMap.containsKey(player)) {
            PlayerStats stats = playerStatsMap.get(player);
            fileManager.savePlayerStats(player, stats);
            playerStatsMap.remove(player);
        }
    }

    public PlayerStats getPlayerStats(OfflinePlayer player) {
        if (playerStatsMap.containsKey(player)) {
            return playerStatsMap.get(player);
        } else {
            PlayerStats stats = fileManager.loadPlayerStats(player);
            return stats;
        }
    }

    public void showPlayerStats(Player sender, OfflinePlayer targetPlayer) {
        PlayerStats stats = getPlayerStats(targetPlayer);
        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : "Unbekannt";

        sender.sendMessage("§8§m------------------------------");
        sender.sendMessage("§3§lSalor§c§lMurder §7| §fStats von §e" + targetName);
        sender.sendMessage("§8§m------------------------------");
        sender.sendMessage("§7Kills als Murder: §f" + (stats.killedDetectives + stats.killedInnocents));
        sender.sendMessage("§7Murderer getötet: §f" + stats.murderersKilled);
        sender.sendMessage("§7Random Kills: §f" + stats.randomKills);
        sender.sendMessage("§7Siege als Murder: §f" + stats.roundsWonMurderer);
        sender.sendMessage("§7Siege als Innocent: §f" + stats.roundsWonInnocent);
        sender.sendMessage("§7Niederlagen als Murder: §f" + stats.roundsLostMurderer);
        sender.sendMessage("§7Niederlagen als Innocent: §f" + stats.roundsLostInnocent);
        sender.sendMessage("§7Runden gespielt: §f" + stats.getRoundsPlayed());
        sender.sendMessage("§7Siegerquote: §f" + stats.getWinRate());
        sender.sendMessage("§7Gemachte Coins: §f" + stats.coins);
        sender.sendMessage("§7Punkte: §f" + stats.getPoints());
        sender.sendMessage("§7Leaderboard Position: §f" + leaderBoardManager.getRank(targetPlayer));
        sender.sendMessage("§8§m------------------------------");
    }

    public void saveAllPlayerStats() {
        for (Player player : new HashSet<>(playerStatsMap.keySet())) {
            savePlayerStats(player);
        }
    }

    public void despawnArmorstands() {
        leaderBoardManager.despawnArmorstands();
    }
}
