package com.wolfdrache.salormurder.ui;

import java.util.HashSet;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.RoundSM;

public class XpBarTimer {
    public static void updateXpBar(RoundSM round, int totalTime) {
        for (Player player : new HashSet<>(round.players.keySet())) {
            updateXpBar(player, round.time, totalTime);
        }
    }

    public static void updateXpBar(Player player, int timeLeft, int totalTime) {
        player.setLevel(timeLeft);
        player.setExp((float) timeLeft / totalTime);
    }
}
