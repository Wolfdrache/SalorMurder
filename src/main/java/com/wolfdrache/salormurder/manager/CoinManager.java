package com.wolfdrache.salormurder.manager;

import com.wolfdrache.serverapi.coins.CoinService;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.models.PlayerStats;

public class CoinManager {
    private final CoinService coinService;
    private final FileManager fileManager;
    private final StatsManager statsManager;

    public CoinManager(CoinService coinService, FileManager fileManager, StatsManager statsManager) {
        this.coinService = coinService;
        this.fileManager = fileManager;
        this.statsManager = statsManager;
    }

    public void giveCoins(Player player, Coins coins) {
        int amount = fileManager.getCoins(coins);
        if (coinService.giveCoins(player, amount)) {
            MessageHelper.sendMessage(player, "§aDu hast §6" + amount + " Coins §aerhalten.");
            PlayerStats stats = statsManager.getPlayerStats(player);
            stats.coins += amount;
        }
    }

    public void loseCoins(Player player, Coins coins) {
        int amount = fileManager.getCoins(coins) * -1;
        if (coinService.loseCoins(player, amount)) {
            MessageHelper.sendMessage(player, "§cDu hast §6" + amount + " Coins §cverloren.");
            PlayerStats stats = statsManager.getPlayerStats(player);
            stats.coins -= amount;
        }
    }
}
