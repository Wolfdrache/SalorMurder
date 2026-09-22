package com.wolfdrache.salormurder.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.wolfdrache.murderknifes.api.MurderKnifesAPI;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.helper.TabHelper;
import com.wolfdrache.salormurder.items.NavItems;
import com.wolfdrache.salormurder.models.MapSM;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.PlayerSM.Role;
import com.wolfdrache.salormurder.models.PlayerStats;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;
import com.wolfdrache.salormurder.timer.BowTimer;
import com.wolfdrache.salormurder.timer.RoundTimer;

public class RoundManager {
    private final MurderKnifesAPI murderKnifes;
    
    private final MapManager mapManager;
    private final StatsManager statsManager;
    private final CoinManager coinManager;
    private final FileManager fileManager;
    
    private JoinSignManager joinSignManager;
    private RoundTimer roundTimer;
    private BowTimer bowTimer;

    private final Location waitingLobby;

    private final List<RoundSM> rounds = new ArrayList<>();
    private final Map<Player, RoundSM> activePlayers = new HashMap<>();

    public final List<Player> joiningPlayers = new ArrayList<>();

    public RoundManager(MurderKnifesAPI murderKnifes, MapManager mapManager, StatsManager statsManager, CoinManager coinManager, FileManager fileManager) {
        this.murderKnifes = murderKnifes;
        this.mapManager = mapManager;
        this.statsManager = statsManager;
        this.coinManager = coinManager;
        this.fileManager = fileManager;

        this.waitingLobby = fileManager.getLobbyLocation();

        createAllRounds();
    }

    public void setExtras(JoinSignManager joinSignManager, RoundTimer roundTimer, BowTimer bowTimer) {
        this.joinSignManager = joinSignManager;
        this.roundTimer = roundTimer;
        this.bowTimer = bowTimer;
    }

    private void createAllRounds() {
        List<MapSM> maps = mapManager.getMaps();
        for (MapSM map : maps) {
            rounds.add(new RoundSM(map));
        }
    }

    public void joinPlayer(Player player, RoundSM round) {
        if (activePlayers.containsKey(player)) {
            leavePlayer(player);
        }
        joiningPlayers.add(player);
        if (round.mode == RoundMode.WAITING && round.hasFreeSlot()) {
            if (!round.hasPlayers()) {
                round.time = fileManager.getTime(Time.LOBBY);
                roundTimer.startTimer(round);
            }
            player.teleport(waitingLobby);
            round.addPlayer(player);
            activePlayers.put(player, round);
            giveItems(player);
            joinSignManager.updateSign(round);
            MessageHelper.playerJoinRound(player, round);
            givePlayerWaitingAttributes(player);
            player.setGameMode(GameMode.ADVENTURE);
        } else if (round.mode == RoundMode.STARTING || round.mode == RoundMode.RUNNING) {
            player.teleport(round.map.world.getSpawnLocation());
            round.addPlayer(player);
            activePlayers.put(player, round);
            giveItems(player);
            givePlayerSpectatorAttributes(player);
            MessageHelper.playerSpectateRound(player, round);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void leavePlayer(Player player) {
        if (!activePlayers.containsKey(player)) return;
        RoundSM round = activePlayers.get(player);
        PlayerSM playerSM = round.players.get(player);
        round.players.remove(player);
        activePlayers.remove(player);
        MessageHelper.playerLeaveRound(player, round);
        TabHelper.showPlayerToAll(round, player);
        player.teleport(fileManager.getLeaveLocation());
        givePlayerNormalAttributes(player);
        if (round.mode == RoundMode.WAITING) {
            joinSignManager.updateSign(round);
            if (round.players.isEmpty()) {
                roundTimer.stopTimer(round);
                mapManager.unloadWorld(round.map);
                return;
            }
        } else if (round.mode == RoundMode.STARTING || round.mode == RoundMode.RUNNING) {
            if (playerSM.mode == PlayerMode.PLAYING) {
                PlayerStats playerStats = statsManager.getPlayerStats(player);
                playerStats.addLoss(playerSM.role);
                bowTimer.stopBowTimer(player);
                checkEndRound(round);
            }
        }
    }

    public boolean enoughPlayersToStart(RoundSM round) {
        return round.players.size() >= fileManager.getMinPlayers();
    }

    public void startCommand(RoundSM round) {
        if (enoughPlayersToStart(round) && round.time > 6) {
            round.time = 6; 
        }
    }

    public void startRound(RoundSM round) {
        World world = mapManager.getOrLoadWorld(round.map);
        mapManager.addWorldToMap(round.map, world);
        round.resetLootChests();
        round.time = fileManager.getTime(Time.BEFORE_START);
        round.mode = RoundMode.STARTING;
        joinSignManager.replaceRoundSign(round);
        spawnPlayer(round);
    }

    private void endRound(RoundSM round) {
        round.mode = RoundMode.ENDING;
        round.time = fileManager.getTime(Time.END);
        bowTimer.stopTimerRound(round);
        announceWinner(round);
        TabHelper.showAllPlayersRound(round);
        for (Player player : round.players.keySet()) {
            PlayerSM playerSM = round.players.get(player);
            playerSM.mode = PlayerMode.ENDING;
            givePlayerWaitingAttributes(player);
            giveItems(player);
        }
    }

    public void resetRound(RoundSM round) {
        for (Player player : new ArrayList<>(round.players.keySet())) {
            leavePlayer(player);
        }
        bowTimer.stopTimerRound(round);
        round.mode = RoundMode.WAITING;
        round.time = fileManager.getTime(Time.LOBBY);
        mapManager.unloadWorld(round.map);
        roundTimer.stopTimer(round);
        joinSignManager.giveRoundToSign(round);
        statsManager.updateLeaderboard();
    }

    private void announceWinner(RoundSM round) {
        Role winningRole = null;
        for (Player player : round.players.keySet()) {
            PlayerSM playerSM = round.players.get(player);
            if (playerSM.mode != PlayerMode.PLAYING) continue;
            if (playerSM.role == Role.MURDERER) {
                winningRole = Role.MURDERER;
                break;
            } else {
                winningRole = Role.INNOCENT;
                break;
            }
        }

        if (winningRole != null) {
            String message;
            if (winningRole == Role.MURDERER) {
                message = "§cDer Mörder hat gewonnen!";
            } else {
                message = "§aDie Unschuldigen haben gewonnen!";
            }
            for (Player player : round.players.keySet()) {
                MessageHelper.sendTitle(player, message, "");
                PlayerSM playerSM = round.players.get(player);
                if (playerSM.mode == PlayerMode.PLAYING && playerSM.role == winningRole) {
                    PlayerStats playerStats = statsManager.getPlayerStats(player);
                    playerStats.addWin(winningRole);
                    coinManager.giveCoins(player, winningRole == Role.MURDERER ? Coins.MURDER_WIN : Coins.INNO_WIN);
                }
            }
        }
    }

    private void checkEndRound(RoundSM round) {
        boolean hasMurderer = false;
        boolean hasInnocent = false;
        for (PlayerSM playerSM : round.players.values()) {
            if (playerSM.mode != PlayerMode.PLAYING) continue;
            if (playerSM.role == Role.MURDERER) hasMurderer = true;
            if (playerSM.role == Role.INNOCENT || playerSM.role == Role.DETECTIVE) hasInnocent = true;
        }
        if (!hasMurderer || !hasInnocent) {
            endRound(round);
        }
    }

    private void spawnPlayer(RoundSM round) {
        List<Location> spawnPoints = new ArrayList<>(round.map.spawnpoints);
        List<Player> players = new ArrayList<>(round.players.keySet());
        int murderIndex = ThreadLocalRandom.current().nextInt(players.size());
        Player murderer = players.get(murderIndex);
        players.remove(murderer);
        PlayerSM murdererSM = round.players.get(murderer);
        murdererSM.role = Role.MURDERER;

        int detectiveIndex = ThreadLocalRandom.current().nextInt(players.size());
        Player detective = players.get(detectiveIndex);
        PlayerSM detectiveSM = round.players.get(detective);
        detectiveSM.role = Role.DETECTIVE;
        players.add(murderer);

        for (Player player : players) {
            Location spawnLocation = spawnPoints.get(ThreadLocalRandom.current().nextInt(spawnPoints.size()));
            player.teleport(spawnLocation);
            PlayerSM playerSM = round.players.get(player);
            if (playerSM.role == null) playerSM.role = Role.INNOCENT;
            playerSM.mode = PlayerMode.PLAYING;
            spawnPoints.remove(spawnLocation);
            givePlayerNormalAttributes(player);
            giveItems(player);
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void giveItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        RoundSM round = activePlayers.get(player);
        PlayerSM playerSM = round.players.get(player);
        switch (playerSM.mode) {
            case WAITING:
                inventory.setItem(4, NavItems.knifeSelectorItem);
                inventory.setItem(8, NavItems.leaveItem);
                break;
            case PLAYING:
                switch (playerSM.role) {
                    case MURDERER:
                        ItemStack knife = murderKnifes.getKnife(player);
                        inventory.setItem(4, knife);
                        break;
                    case DETECTIVE:
                        inventory.setItem(0, NavItems.bowItem);
                        inventory.setItem(8, NavItems.arrowItem);
                        break;
                    case INNOCENT:
                        break;
                }
                break;
            case SPECTATING:
                inventory.setItem(4, NavItems.spectatorTpItem);
                inventory.setItem(8, NavItems.leaveItem);
                break;
            case ENDING:
                inventory.setItem(8, NavItems.leaveItem);
                break;
            case EDIT:
                break;
        }
    }

    public List<RoundSM> getRoundsByMode(RoundMode mode) {
        List<RoundSM> roundsByMode = new ArrayList<>();
        for (RoundSM round : rounds) {
            if (round.mode == mode) {
                roundsByMode.add(round);
            }
        }
        return roundsByMode;
    }

    public RoundSM getRoundByPlayer(Player player) {
        return activePlayers.get(player);
    }

    public RoundSM getRoundByName(String name) {
        if (name.equals("@r")) {
            return getRandomRoundByMode(RoundMode.WAITING);
        }
        for (RoundSM round : rounds) {
            if (round.map.name.equalsIgnoreCase(name)) {
                return round;
            }
        }
        return null;
    }

    public RoundSM getRoundByLocation(Location location) {
        for (RoundSM round : rounds) {
            if (round.map != null && round.map.world != null &&round.map.world.equals(location.getWorld())) {
                return round;
            }
        }
        return null;
    }

    private RoundSM getRandomRoundByMode(RoundMode waiting) {
        List<RoundSM> roundsByMode = getRoundsByMode(waiting);
        if (roundsByMode.isEmpty()) {
            return null;
        }
        return roundsByMode.get(ThreadLocalRandom.current().nextInt(roundsByMode.size()));
    }

    public void killPlayer(Player player, RoundSM round) {
        if (round == null || !round.players.containsKey(player)) return;
        if (round.mode != RoundMode.RUNNING) return;
        PlayerSM playerSM = round.players.get(player);
        if (playerSM.mode != PlayerMode.PLAYING) return;
        PlayerStats stats = statsManager.getPlayerStats(player);
        if (playerSM.role == Role.MURDERER) {
            stats.roundsLostMurderer++;
        } else {
            stats.roundsLostInnocent++;
        }
        playerSM.mode = PlayerMode.SPECTATING;
        givePlayerSpectatorAttributes(player);
        round.addLootChest(player.getLocation(), player);
        giveItems(player);
        checkEndRound(round);
    }

    public List<RoundSM> getRounds() {
        return new ArrayList<>(rounds);
    }

    public void playerLootChest(RoundSM round, Player player, Location location) {
        round.playerLootChest(player, location);
        checkPlayerGetTrident(player);
    }

    public void checkPlayerGetTrident(Player player) {
        if (player.getInventory().contains(Material.BOW)) return;
        ItemStack tridentCost = fileManager.getTridentCost();
        if (tridentCost != null && player.getInventory().containsAtLeast(tridentCost, tridentCost.getAmount())) {
            player.getInventory().removeItem(tridentCost);
            player.getInventory().setItem(0, NavItems.bowItem);
            player.getInventory().setItem(8, NavItems.arrowItem);
        }
    }

    public void givePlayerWaitingAttributes(Player player) {
        player.setInvisible(false);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(false);
        player.setFlying(false);
    }
    public void givePlayerSpectatorAttributes(Player player) {
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    public void givePlayerNormalAttributes(Player player) {
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setFlying(false);
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(false);
        }
    }

    public void stopRound(RoundSM round) {
        round.mode = RoundMode.ENDING;
        for (Player player : new ArrayList<>(round.players.keySet())) {
            leavePlayer(player);
            MessageHelper.sendMessage(player, "§cDie Runde wurde gestoppt.");
        }
        resetRound(round);
    }

    public void editRound(Player player, RoundSM round) {
        if (getRoundByPlayer(player) != null) {
            leavePlayer(player);
        }
        if (round.mode != RoundMode.EDIT) {
            stopRound(round);
            round.mode = RoundMode.EDIT;
            World world = mapManager.getOrLoadWorld(round.map);
            mapManager.addWorldToMap(round.map, world);
            joinSignManager.replaceRoundSign(round);
        }
        player.teleport(round.map.world.getSpawnLocation());
        activePlayers.put(player, round);
        round.addPlayer(player);
        player.setGameMode(GameMode.CREATIVE);
        player.getInventory().clear();
    }

    public void saveRound(RoundSM round) {
        if (round.mode == RoundMode.EDIT) {
            for (Player player : new ArrayList<>(round.players.keySet())) {
                leavePlayer(player);
                MessageHelper.sendMessage(player, "§aDie Map wurde gespeichert.");
            }
            mapManager.saveWorld(round.map);
            resetRound(round);
        }
    }

    public void unloadAllRounds() {
        for (RoundSM round : rounds) {
            if (round.mode == RoundMode.EDIT) {
                saveRound(round);
            } else {
                stopRound(round);
            }
        }
    }
}
