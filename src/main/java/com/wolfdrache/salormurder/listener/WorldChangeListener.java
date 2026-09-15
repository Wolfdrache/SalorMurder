package com.wolfdrache.salormurder.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class WorldChangeListener implements Listener {
    private final RoundManager roundManager;

    public WorldChangeListener(RoundManager roundManager) {
        this.roundManager = roundManager;
    }
    
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (!player.getWorld().equals(round.map.world)) {
            roundManager.leavePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerTpRound(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (roundManager.joiningPlayers.contains(player)) {
            roundManager.joiningPlayers.remove(player);
            return;
        }
        RoundSM round = roundManager.getRoundByLocation(player.getLocation());
        if (round == null) return;
        if (round.mode == RoundMode.RUNNING) {
            roundManager.joinPlayer(player, round);
            return;
        } else if (round.mode == RoundMode.EDIT) {
            roundManager.editRound(player, round);
            return;
        }
    }
}
