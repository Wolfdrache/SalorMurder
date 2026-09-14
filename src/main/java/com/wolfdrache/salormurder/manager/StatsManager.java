package com.wolfdrache.salormurder.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.salormurder.helper.ItemHelper;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.models.ArmorstandLB;
import com.wolfdrache.salormurder.models.PlayerStats;

public class StatsManager {
    private final FileManager fileManager;

    private final Map<Player, PlayerStats> playerStatsMap = new HashMap<>();

    private final List<UUID> leaderboard = new ArrayList<>();

    private final ArmorstandLB armorstandRank1;
    private final ArmorstandLB armorstandRank2;
    private final ArmorstandLB armorstandRank3;

    private final ArmorStand armorstandEntityRank1;
    private final ArmorStand armorstandEntityRank2;
    private final ArmorStand armorstandEntityRank3;

    public StatsManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.leaderboard.addAll(fileManager.getLeaderboard());
        this.armorstandRank1 = fileManager.loadArmorstandLB(1);
        this.armorstandRank2 = fileManager.loadArmorstandLB(2);
        this.armorstandRank3 = fileManager.loadArmorstandLB(3);

        this.armorstandEntityRank1 = this.armorstandRank1.summon();
        this.armorstandEntityRank2 = this.armorstandRank2.summon();
        this.armorstandEntityRank3 = this.armorstandRank3.summon();

        updateArmorstands();
    }

    private void updateArmorstands() {
        updateArmorstand(armorstandEntityRank1, 1);
        updateArmorstand(armorstandEntityRank2, 2);
        updateArmorstand(armorstandEntityRank3, 3);
    }

    private void updateArmorstand(ArmorStand armorstand, int i) {
        if (leaderboard.size() < i) return;
        if (armorstand == null || armorstand.isDead()) {
            ArmorstandLB armorstandLB = switch (i) {
                case 1 -> this.armorstandRank1;
                case 2 -> this.armorstandRank2;
                case 3 -> this.armorstandRank3;
                default -> null;
            };
            if (armorstandLB != null) {
                armorstand = armorstandLB.summon();
            }
        };
        OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(leaderboard.get(i - 1));
        ItemStack playerHead = ItemHelper.createPlayerHead(topPlayer);
        int points = getPlayerStats(topPlayer).getPoints();
        armorstand.customName(MessageHelper.createComponent("§6" + i + ". " + topPlayer.getName() + " §7- §e" + points));
        EntityEquipment equipment = armorstand.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(playerHead);
        }
    }

    public void loadPlayerStats(Player player) {
        PlayerStats stats = fileManager.loadPlayerStats(player);
        playerStatsMap.put(player, stats);
        if (!leaderboard.contains(player.getUniqueId())) {
            leaderboard.add(player.getUniqueId());
            updateLeaderboard();
        }
    }

    public void updateLeaderboard() {
        leaderboard.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = getPlayerStats(Bukkit.getOfflinePlayer(uuid1));
            PlayerStats stats2 = getPlayerStats(Bukkit.getOfflinePlayer(uuid2));
            return Integer.compare(stats2.getPoints(), stats1.getPoints());
        });

        updateArmorstands();
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
        sender.sendMessage("§7Leaderboard Position: §f" + (leaderboard.indexOf(targetPlayer.getUniqueId()) + 1));
        sender.sendMessage("§8§m------------------------------");
    }

    public void saveAllPlayerStats() {
        for (Player player : new HashSet<>(playerStatsMap.keySet())) {
            savePlayerStats(player);
        }
    }

    public void despawnArmorstands() {
        Chunk armorstandChunk1 = armorstandEntityRank1.getLocation().getChunk();
        Chunk armorstandChunk2 = armorstandEntityRank2.getLocation().getChunk();
        Chunk armorstandChunk3 = armorstandEntityRank3.getLocation().getChunk();

        if (!armorstandChunk1.isLoaded()) armorstandChunk1.load();
        if (!armorstandChunk2.isLoaded()) armorstandChunk2.load();
        if (!armorstandChunk3.isLoaded()) armorstandChunk3.load();

        armorstandEntityRank1.remove();
        armorstandEntityRank2.remove();
        armorstandEntityRank3.remove();
    }
}
