package com.wolfdrache.salormurder.manager;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.models.PlayerStats;

import net.milkbowl.vault.economy.Economy;

public class CoinManager {
    private final Economy economy;
    private final FileManager fileManager;
    private final StatsManager statsManager;

    public CoinManager(Economy economy, FileManager fileManager, StatsManager statsManager) {
        this.economy = economy;
        this.fileManager = fileManager;
        this.statsManager = statsManager;
    }

    public void giveCoins(Player player, Coins coins) {
        int amount = fileManager.getCoins(coins);
        economy.depositPlayer(player, amount);
        MessageHelper.sendMessage(player, "§aDu hast §6" + amount + " Coins §aerhalten.");
        PlayerStats stats = statsManager.getPlayerStats(player);
        stats.coins += amount;
    }
}
