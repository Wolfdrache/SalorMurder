package com.wolfdrache.salormurder;

import com.wolfdrache.salormurder.models.ConfigModes;
import com.wolfdrache.serverapi.coins.CoinService;
import com.wolfdrache.serverapi.gamemodes.roundManagement.IRound;
import com.wolfdrache.serverapi.gamemodes.roundManagement.IRoundMode;
import com.wolfdrache.serverapi.gamemodes.roundManagement.RoundService;
import com.wolfdrache.serverapi.gamemodes.roundManagement.RoundStatusUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.wolfdrache.murderknifes.api.MurderKnifesAPI;
import com.wolfdrache.party.api.PartyService;
import com.wolfdrache.salormurder.achievements.AchievementHelper;
import com.wolfdrache.salormurder.achievements.AchievementTracker;
import com.wolfdrache.salormurder.commands.*;
import com.wolfdrache.salormurder.listener.*;
import com.wolfdrache.salormurder.manager.*;
import com.wolfdrache.salormurder.timer.*;
import com.wolfdrache.salormurder.ui.*;
import com.wolfdrache.serverapi.achievements.AchievementService;
import com.wolfdrache.serverapi.gamemodes.GameService;
import com.wolfdrache.serverapi.reports.ReportService;

public class SalorMurder extends JavaPlugin {
    private static SalorMurder instance;
    private MurderKnifesAPI murderKnifes;
    private PartyService partyService;
    private ReportService reportService;
    private GameService gameService;
    private RoundService roundService;
    private AchievementService achievementService;
    private CoinService coinService;

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
        instance = this;
        partyService = setupService(PartyService.class);
        if (partyService == null) {
            getLogger().severe("Party API service not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        reportService = setupService(ReportService.class);
        if (reportService == null) {
            getLogger().severe("ReportService not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        gameService = setupService(GameService.class);
        if (gameService == null) {
            getLogger().severe("GameService not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        roundService = setupService(RoundService.class);
        if (roundService == null) {
            getLogger().severe("RoundService not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        achievementService = setupService(AchievementService.class);
        if (achievementService == null) {
            getLogger().severe("AchievementService not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        murderKnifes = setupService(MurderKnifesAPI.class);
        if (murderKnifes == null) {
            getLogger().severe("MurderKnifes API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        coinService = setupService(CoinService.class);
        if (coinService == null) {
            getLogger().severe("CoinService not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        fileManager = new FileManager(this);
        chatManager = new ChatManager();
        statsManager = new StatsManager(fileManager);
        coinManager = new CoinManager(coinService, fileManager, statsManager);
        mapManager = new MapManager(fileManager);
        roundManager = new RoundManager(partyService, gameService, roundService, murderKnifes, mapManager, statsManager, coinManager, fileManager);

        joinSignManager = new JoinSignManager(roundManager, fileManager);
        roundTimer = new RoundTimer(roundManager, fileManager);
        bowTimer = new BowTimer(fileManager);
        roundManager.setExtras(joinSignManager, roundTimer, bowTimer);

        teleporterGui = new TeleporterGui(roundManager);

        AchievementHelper.setStatics(achievementService, statsManager);
        AchievementTracker.setRequiredRandomKills(3);

        getServer().getPluginManager().registerEvents(teleporterGui, this);
        getServer().getPluginManager().registerEvents(new InteractionListener(murderKnifes, roundManager, teleporterGui), this);
        getServer().getPluginManager().registerEvents(new DamageListener(murderKnifes, roundManager, statsManager, coinManager, bowTimer), this);
        getServer().getPluginManager().registerEvents(new JoinSignListener(joinSignManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(roundManager, statsManager, chatManager, bowTimer), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(roundManager, fileManager), this);

        String reportCategory = "SailorsMurder";
        gameService.registerCommand(this, GameService.CommandType.JOIN, new JoinCommand(roundManager));
        gameService.registerCommand(this, GameService.CommandType.LEAVE, new LeaveCommand(roundManager));
        gameService.registerCommand(this, GameService.CommandType.START, new StartCommand(roundManager));
        gameService.registerCommand(this, GameService.CommandType.FORCEJOIN, new ForceJoinCommand(roundManager));
        gameService.registerCommand(this, GameService.CommandType.ADMIN, new AdminCommand(roundManager));
        gameService.registerCommand(this, GameService.CommandType.STATS, new StatsCommand(statsManager));
        gameService.registerPluginAlias(this, reportCategory);

        gameService.registerStatsDisplayItem(player -> statsManager.getPlayerStatsItem(player));

        reportService.createCategory(reportCategory);
        reportService.addReason(reportCategory, "teaming");
        reportService.addReason(reportCategory, "hacking");
        reportService.addReason(reportCategory, "stats_boosting");
        reportService.addReason(reportCategory, "outofmap");
        reportService.addReason(reportCategory, "random_killing");
        reportService.addReason(reportCategory, "trolling");

        getLogger().info("SalorMurder has been enabled!");
    }
    
    @Override
    public void onDisable(){
        roundManager.unloadAllRounds();

        statsManager.despawnArmorstands();
        statsManager.saveAllPlayerStats(true);
        getLogger().info("SalorMurder has been disabled!");
    }

    private <T> T setupService(Class<T> serviceClass) {
        RegisteredServiceProvider<T> rsp =
                getServer().getServicesManager().getRegistration(serviceClass);

        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }

    public static SalorMurder getInstance() {
        return instance;
    }

    public static void callRoundUpdateEvent(IRound round, IRoundMode roundMode) {
        RoundStatusUpdateEvent event = new RoundStatusUpdateEvent(instance, round, roundMode);
        Bukkit.getPluginManager().callEvent(event);
    }
}
