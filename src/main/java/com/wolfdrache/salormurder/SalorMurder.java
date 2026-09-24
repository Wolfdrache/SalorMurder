package com.wolfdrache.salormurder;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import com.wolfdrache.murderknifes.api.MurderKnifesAPI;
import com.wolfdrache.salormurder.commands.*;
import com.wolfdrache.salormurder.helper.TabHelper;
import com.wolfdrache.salormurder.listener.*;
import com.wolfdrache.salormurder.manager.*;
import com.wolfdrache.salormurder.timer.*;
import com.wolfdrache.salormurder.ui.TeleporterGui;

public class SalorMurder extends JavaPlugin {

    private Economy economy;
    private MurderKnifesAPI murderKnifes;

    private FileManager fileManager;
    private MapManager mapManager;
    private RoundManager roundManager;
    private StatsManager statsManager;
    private JoinSignManager joinSignManager;
    private CoinManager coinManager;
    private ChatManager chatManager;

    private RoundTimer roundTimer;
    private BowTimer bowTimer;

    private TeleporterGui teleporterGui;

    @Override
    public void onEnable(){
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupMurderKnifes()) {
            getLogger().severe("MurderKnifes API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        fileManager = new FileManager(this);
        chatManager = new ChatManager();
        coinManager = new CoinManager(economy, fileManager);
        mapManager = new MapManager(fileManager);
        statsManager = new StatsManager(fileManager);
        roundManager = new RoundManager(murderKnifes, mapManager, statsManager, coinManager, fileManager);

        joinSignManager = new JoinSignManager(roundManager, fileManager);
        roundTimer = new RoundTimer(this, roundManager, fileManager);
        bowTimer = new BowTimer(this, fileManager);
        roundManager.setExtras(joinSignManager, roundTimer, bowTimer);

        teleporterGui = new TeleporterGui(roundManager);

        TabHelper.setPlugin(this);

        getServer().getPluginManager().registerEvents(teleporterGui, this);
        getServer().getPluginManager().registerEvents(new InteractionListener(murderKnifes, roundManager, teleporterGui), this);
        getServer().getPluginManager().registerEvents(new DamageListener(murderKnifes, roundManager, statsManager, coinManager), this);
        getServer().getPluginManager().registerEvents(new JoinSignListener(joinSignManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(roundManager, statsManager, chatManager, bowTimer), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(roundManager), this);

        getCommand("joinsm").setExecutor(new JoinCommand(roundManager));
        getCommand("leavesm").setExecutor(new LeaveCommand(roundManager));
        getCommand("startsm").setExecutor(new StartCommand(roundManager));
        getCommand("statssm").setExecutor(new StatsCommand(statsManager));
        getCommand("forcejoinsm").setExecutor(new ForceJoinCommand(roundManager));
        getCommand("smadmin").setExecutor(new AdminCommand(roundManager));

        getLogger().info("SalorMurder has been enabled!");
    }
    
    @Override
    public void onDisable(){
        roundManager.unloadAllRounds();

        statsManager.despawnArmorstands();
        statsManager.saveAllPlayerStats();
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

    private boolean setupMurderKnifes() {
        RegisteredServiceProvider<MurderKnifesAPI> provider = getServer().getServicesManager().getRegistration(MurderKnifesAPI.class);

        if (provider == null) {
            getLogger().severe("MurderKnifes API not found!");
            return false;
        }

        murderKnifes = provider.getProvider();
        return true;
    }
}
