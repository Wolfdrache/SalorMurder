package com.wolfdrache.salormurder.helper;

import org.bukkit.entity.Player;

import com.wolfdrache.helper.Helper;
import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;

public class TabHelper {

    public static void updateTabList(RoundSM round) {
        String header = "§3§lSailors§c§lMurder\n";
        String footer = "\n§7Map: §e" + round.map.name +
                "\n§7Zeit: §e" + formatTime(round.time) +
                "\n§7Gebaut von: " + round.map.builder;
        for (Player player : round.players.keySet()) {
            var playerSM = round.players.get(player);
            player.sendPlayerListHeader(Helper.createComponent(header));
            player.sendPlayerListFooter(Helper.createComponent(footer));
            for (Player otherPlayer : round.players.keySet()) {
                if (otherPlayer.equals(player)) {
                    continue;
                }
                if (playerSM == null || playerSM.mode == PlayerMode.SPECTATING) {
                    otherPlayer.hidePlayer(SalorMurder.getInstance(), player);
                } else if (playerSM.mode == PlayerMode.PLAYING || playerSM.mode == PlayerMode.ENDING) {
                    otherPlayer.showPlayer(SalorMurder.getInstance(), player);
                }
            }
        }
    }

    public static String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void showPlayerToAll(RoundSM round, Player player) {
        for (Player otherPlayer : round.players.keySet()) {
            if (!otherPlayer.equals(player)) {
                otherPlayer.showPlayer(SalorMurder.getInstance(), player);
            }
        }
    }

    public static void showAllPlayersRound(RoundSM round) {
        for (Player player : round.players.keySet()) {
            showPlayerToAll(round, player);
        }
    }
}
