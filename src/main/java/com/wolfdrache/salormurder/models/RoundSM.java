package com.wolfdrache.salormurder.models;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;

public class RoundSM {
    public static enum RoundMode {
        WAITING,
        STARTING,
        RUNNING,
        ENDING,
        EDIT
    }

    public final Map<Player, PlayerSM> players = new HashMap<>();
    public final MapSM map;
    public RoundMode mode = RoundMode.WAITING;
    public int time;

    public RoundSM(MapSM map) {
        this.map = map;
    }

    public boolean addPlayer(Player player) {
        if (players.containsKey(player)) return false;
        if (mode == RoundMode.WAITING) {
            players.put(player, new PlayerSM(PlayerMode.WAITING));
        } else if (mode == RoundMode.RUNNING) {
            players.put(player, new PlayerSM(PlayerMode.SPECTATING));
        } else if (mode == RoundMode.EDIT) {
            players.put(player, new PlayerSM(PlayerMode.EDIT));
        } else if (mode == RoundMode.ENDING) {
            return false;
        }
        return true;
    }

    public boolean hasFreeSlot() {
        return players.size() < map.spawnpoints.size();
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }
}
