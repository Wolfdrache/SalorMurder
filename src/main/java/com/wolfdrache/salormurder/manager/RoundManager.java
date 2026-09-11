package com.wolfdrache.salormurder.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.wolfdrache.murderknifes.api.MurderKnfesAPI;

import com.wolfdrache.salormurder.models.MapSM;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.PlayerSM.Role;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class RoundManager {
    private final MurderKnfesAPI murderKnifes;

    private final MapManager mapManager;
    private final FileManager fileManager;

    private final List<RoundSM> rounds = new ArrayList<>();
    private final Map<Player, RoundSM> activePlayers = new HashMap<>();

    public final List<Player> joiningPlayers = new ArrayList<>();

    public RoundManager(MurderKnfesAPI murderKnifes, MapManager mapManager, FileManager fileManager) {
        this.murderKnifes = murderKnifes;
        this.mapManager = mapManager;
        this.fileManager = fileManager;

        createAllRounds();
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
            }
        }
    }

    public void leavePlayer(Player player) {
        if (activePlayers.containsKey(player)) {
            activePlayers.remove(player);
        }
    }

    public boolean enoughPlayersToStart(RoundSM round) {
        return round.players.size() >= fileManager.getMinPlayers();
    }

    public void startRound(RoundSM round) {
        World world = mapManager.getOrLoadWorld(round.map);
        mapManager.addWorldToMap(round.map, world);
        round.mode = RoundMode.STARTING;
        // joinSignManager.replaceRoundSign(round);
        spawnPlayer(round);
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

        for (Player p : players) {
            Location spawnLocation = spawnPoints.get(ThreadLocalRandom.current().nextInt(spawnPoints.size()));
            p.teleport(spawnLocation);
            PlayerSM playerSM = round.players.get(p);
            if (playerSM.role == null) playerSM.role = Role.INNOCENT;
            playerSM.mode = PlayerMode.PLAYING;
            spawnPoints.remove(spawnLocation);
            giveItems(playerSM.role, p);
        }
    }

    private void giveItems(Role role, Player p) {
        PlayerInventory inventory = p.getInventory();
        inventory.clear();
        switch (role) {
            case MURDERER: 
                ItemStack knife = murderKnifes.getKnife(p);
                inventory.setItem(4, knife);
                break;
            case DETECTIVE:
                // give detective an trident
                break;
            case INNOCENT:
                // give innocent no items
                break;
        }
    }
}
