package com.wolfdrache.salormurder;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import com.wolfdrache.murderknifes.api.MurderKnifesAPI;
import com.wolfdrache.party.api.PartyService;
import com.wolfdrache.salormurder.commands.*;
import com.wolfdrache.salormurder.helper.TabHelper;
import com.wolfdrache.salormurder.leaderboard.*;
import com.wolfdrache.salormurder.listener.*;
import com.wolfdrache.salormurder.manager.*;
import com.wolfdrache.salormurder.timer.*;
import com.wolfdrache.salormurder.ui.*;
import com.wolfdrache.serverapi.gamemodes.GameCommandTypes;
import com.wolfdrache.serverapi.gamemodes.GameService;
import com.wolfdrache.serverapi.reports.ReportService;

public class SalorMurder extends JavaPlugin {
    private static SalorMurder instance;
    private Economy economy;
    private MurderKnifesAPI murderKnifes;
    private PartyService partyService;
    private ReportService reportService;
    private GameService gameService;

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

    private LeaderBoardFileManager leaderBoardFileManager;

    @Override
    public void onEnable(){
        instance = this;
        economy = setupService(Economy.class);
        if (economy == null) {
            getLogger().severe("Vault economy service not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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

        murderKnifes = setupService(MurderKnifesAPI.class);
        if (murderKnifes == null) {
            getLogger().severe("MurderKnifes API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        fileManager = new FileManager(this);
        leaderBoardFileManager = new LeaderBoardFileManager(this);
        chatManager = new ChatManager();
        statsManager = new StatsManager(fileManager, leaderBoardFileManager);
        coinManager = new CoinManager(economy, fileManager, statsManager);
        mapManager = new MapManager(fileManager);
        roundManager = new RoundManager(partyService, gameService, murderKnifes, mapManager, statsManager, coinManager, fileManager);

        joinSignManager = new JoinSignManager(roundManager, fileManager);
        roundTimer = new RoundTimer(roundManager, fileManager);
        bowTimer = new BowTimer(fileManager);
        roundManager.setExtras(joinSignManager, roundTimer, bowTimer);

        teleporterGui = new TeleporterGui(roundManager);

        TabHelper.setPlugin(this);

        getServer().getPluginManager().registerEvents(teleporterGui, this);
        getServer().getPluginManager().registerEvents(new InteractionListener(murderKnifes, roundManager, teleporterGui), this);
        getServer().getPluginManager().registerEvents(new DamageListener(murderKnifes, roundManager, statsManager, coinManager, bowTimer), this);
        getServer().getPluginManager().registerEvents(new JoinSignListener(joinSignManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(roundManager, statsManager, chatManager, bowTimer), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(roundManager, fileManager), this);
        getServer().getPluginManager().registerEvents(new LeaderBoardListener(), this);

        String reportCategory = "SailorsMurder";
        gameService.registerCommand(this, GameCommandTypes.JOIN, new JoinCommand(roundManager));
        gameService.registerCommand(this, GameCommandTypes.LEAVE, new LeaveCommand(roundManager));
        gameService.registerCommand(this, GameCommandTypes.START, new StartCommand(roundManager));
        gameService.registerCommand(this, GameCommandTypes.FORCEJOIN, new ForceJoinCommand(roundManager));
        gameService.registerCommand(this, GameCommandTypes.ADMIN, new AdminCommand(roundManager));
        gameService.registerCommand(this, GameCommandTypes.STATS, new StatsCommand(statsManager));
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
}
