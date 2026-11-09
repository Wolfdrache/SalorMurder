package com.wolfdrache.salormurder.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.wolfdrache.helper.Helper;
import com.wolfdrache.leaderboard.LeaderBoardManager;
import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.PlayerStats;

import net.kyori.adventure.text.Component;

public class StatsManager {
    private final FileManager fileManager;
    private final LeaderBoardManager leaderBoardManager;

    private final Map<Player, PlayerStats> playerStatsMap = new HashMap<>();

    public StatsManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.leaderBoardManager = new LeaderBoardManager(SalorMurder.getInstance(), this::getPlayerStats);
    }

    public void loadPlayerStats(Player player) {
        PlayerStats stats = fileManager.loadPlayerStats(player);
        playerStatsMap.put(player, stats);
        leaderBoardManager.onPlayerLoaded(player);
    }

    public void updateLeaderboard() {
        leaderBoardManager.updateLeaderboard();
        saveAllPlayerStats(false);
    }

    public void savePlayerStats(Player player, boolean removeAfterSave) {
        if (playerStatsMap.containsKey(player)) {
            PlayerStats stats = playerStatsMap.get(player);
            fileManager.savePlayerStats(player, stats);
            if (removeAfterSave) {
                playerStatsMap.remove(player);
            }
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

    private List<String>getStatsOutput(OfflinePlayer player) {
        PlayerStats stats = getPlayerStats(player);
        List<String> output = new ArrayList<>();
        output.add("§7Kills als Murder: §a" + (stats.killedDetectives + stats.killedInnocents));
        output.add("§7Murderer getötet: §a" + stats.murderersKilled);
        output.add("§7Random Kills: §4" + stats.randomKills);
        output.add("§7Siege als Murder: §a" + stats.roundsWonMurderer);
        output.add("§7Siege als Innocent: §a" + stats.roundsWonInnocent);
        output.add("§7Niederlagen als Murder: §c" + stats.roundsLostMurderer);
        output.add("§7Niederlagen als Innocent: §c" + stats.roundsLostInnocent);
        output.add("§7Runden gespielt: §7" + stats.getRoundsPlayed());
        output.add("§7Siegerquote: §7" + stats.getWinRate());
        output.add("§7Gemachte Coins: §6" + stats.coins);
        output.add("§7Punkte: §6" + stats.getPoints());
        output.add("§7Leaderboard Position: §6" + leaderBoardManager.getRank(player));
        return output;
    }

    public void showPlayerStats(Player sender, OfflinePlayer targetPlayer) {
        sender.sendMessage("§8§m------------------------------");
        sender.sendMessage("§3§lSailors§c§lMurder §7| §fStats von §e" + Helper.getPlayerRankName(targetPlayer));
        sender.sendMessage("§8§m------------------------------");
        for (String line : getStatsOutput(targetPlayer)) {
            sender.sendMessage(line);
        }
        sender.sendMessage("§8§m------------------------------");
    }

    public ItemStack getPlayerStatsItem(OfflinePlayer player) {
        int rank = leaderBoardManager.getRank(player);
        ItemStack item = new ItemStack(Material.TRIDENT, rank);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Helper.createComponent("§3§lSailors§c§lMurder"));
            List<Component> lore = new ArrayList<>();
            for (String line : getStatsOutput(player)) {
                lore.add(Helper.createComponent(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void saveAllPlayerStats(boolean removeAfterSave) {
        for (Player player : new HashSet<>(playerStatsMap.keySet())) {
            savePlayerStats(player, removeAfterSave);
        }
    }

    public void despawnArmorstands() {
        leaderBoardManager.removeOldArmorstands();
    }
}
