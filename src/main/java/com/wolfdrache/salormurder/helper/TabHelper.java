package com.wolfdrache.salormurder.helper;

import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;

public class TabHelper {
    private static SalorMurder plugin;
    public static void setPlugin(SalorMurder pluginInstance) {
        plugin = pluginInstance;
    }

    public static void updateTabList(RoundSM round) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin instance is not set. Call setPlugin() before using this method.");
        }

        if (round.map.world == null) {
            return;
        }
        
        String header = "§3§lSalor§c§lMurder\n";
        String footer = "§7Map: §e" + round.map.name +
                "\n§7Zeit: §e" + formatTime(round.time) +
                "\n§7Gebaut von: " + round.map.builder;

        round.map.world.sendPlayerListHeader(MessageHelper.createComponent(header));
        round.map.world.sendPlayerListFooter(MessageHelper.createComponent(footer));
        for (Player player : round.players.keySet()) {
            var playerSM = round.players.get(player);
            for (Player otherPlayer : round.players.keySet()) {
                if (otherPlayer.equals(player)) {
                    continue;
                }
                if (playerSM == null || playerSM.mode == PlayerMode.SPECTATING) {
                    otherPlayer.hidePlayer(plugin, player);
                } else if (playerSM.mode == PlayerMode.PLAYING || playerSM.mode == PlayerMode.ENDING) {
                    otherPlayer.showPlayer(plugin, player);
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
                otherPlayer.showPlayer(plugin, player);
            }
        }
    }

    public static void showAllPlayersRound(RoundSM round) {
        for (Player player : round.players.keySet()) {
            showPlayerToAll(round, player);
        }
    }
}
