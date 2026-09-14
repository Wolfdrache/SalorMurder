package com.wolfdrache.salormurder.manager;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.ConfigModes.Coins;

import net.milkbowl.vault.economy.Economy;

public class CoinManager {
    private final Economy economy;
    private final FileManager fileManager;

    public CoinManager(Economy economy, FileManager fileManager) {
        this.economy = economy;
        this.fileManager = fileManager;
    }

    public void giveCoins(Player player, Coins coins) {
        int amount = fileManager.getCoins(coins);
        economy.depositPlayer(player, amount);
    }
}
