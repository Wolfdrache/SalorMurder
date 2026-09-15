package com.wolfdrache.salormurder.manager;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;

public class ChatManager {
    private final String spectatorPrefix = "§7[§c☠§7] §r";

    public void sendSpectatorMessage(Player player, RoundSM round, String message) {
        String messagePrefix = spectatorPrefix + player.getName() + ": §r";
        String fullMessage = messagePrefix + message;
        for (Player p : round.players.keySet()) {
            var playerSM = round.players.get(p);
            if (playerSM == null || playerSM.mode == PlayerMode.SPECTATING) {
                p.sendMessage(fullMessage);
            }
        }
    }

    public void sendGlobalMessage(Player player, @Nullable PlayerSM playerSM, RoundSM round, String message) {
        String playerPrefix = "";
        if (playerSM == null || playerSM.mode == PlayerMode.SPECTATING) {
            playerPrefix = spectatorPrefix;
        } 
        String messagePrefix = playerPrefix + player.getName() + ": §r";
        String fullMessage = messagePrefix + message;
        for (Player p : round.players.keySet()) {
            p.sendMessage(fullMessage);
        }
    }
}
