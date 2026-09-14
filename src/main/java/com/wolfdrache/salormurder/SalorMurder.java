package com.wolfdrache.salormurder;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import com.wolfdrache.murderknifes.api.MurderKnfesAPI;
import com.wolfdrache.salormurder.commands.*;
import com.wolfdrache.salormurder.listener.*;
import com.wolfdrache.salormurder.manager.*;
import com.wolfdrache.salormurder.timer.RoundTimer;

public class SalorMurder extends JavaPlugin {

    private Economy economy;
    private MurderKnfesAPI murderKnfes;

    private FileManager fileManager;
    private MapManager mapManager;
    private RoundManager roundManager;
    private StatsManager statsManager;
    private JoinSignManager joinSignManager;
    private CoinManager coinManager;

    private RoundTimer roundTimer;

    @Override
    public void onEnable(){
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupMurderKnfes()) {
            getLogger().severe("MurderKnifes API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        fileManager = new FileManager(this);
        coinManager = new CoinManager(economy, fileManager);
        mapManager = new MapManager(fileManager);
        statsManager = new StatsManager(fileManager);
        roundManager = new RoundManager(murderKnfes, mapManager, statsManager, coinManager, fileManager);

        joinSignManager = new JoinSignManager(roundManager, fileManager);
        roundTimer = new RoundTimer(this, roundManager, fileManager);
        roundManager.setExtras(joinSignManager, roundTimer);

        getServer().getPluginManager().registerEvents(new InteractionListener(murderKnfes, roundManager), this);
        getServer().getPluginManager().registerEvents(new DamageListener(murderKnfes, roundManager, statsManager, coinManager), this);
        getServer().getPluginManager().registerEvents(new JoinSignListener(joinSignManager), this);

        getCommand("startsm").setExecutor(new StartCommand(roundManager));
        getCommand("statssm").setExecutor(new StatsCommand(statsManager));

        getLogger().info("SalorMurder has been enabled!");
    }
    
    @Override
    public void onDisable(){
        getLogger().info("SalorMurder has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }

    private boolean setupMurderKnfes() {
        RegisteredServiceProvider<MurderKnfesAPI> provider = getServer().getServicesManager().getRegistration(MurderKnfesAPI.class);

        if (provider == null) {
            getLogger().severe("MurderKnifes API not found!");
            return false;
        }

        murderKnfes = provider.getProvider();
        return true;
    }
}
