package com.wolfdrache.salormurder.helper;

import java.time.Duration;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.models.RoundSM;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

public class MessageHelper {
    private static final String prefix = "§3§lSalor§c§lMurder §7 >> §e";
    
    public static void sendMessage(Player player, String message) {
        player.sendMessage(prefix + message);
    }
    
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = createComponent(title);
        Component subtitleComponent = createComponent(subtitle);
        Title titleObj = Title.title(titleComponent, subtitleComponent, Title.Times.times(Duration.ofMillis(fadeIn * 50), Duration.ofMillis(stay * 50), Duration.ofMillis(fadeOut * 50)));
        player.showTitle(titleObj);
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    public static Component createComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    public static void playerJoinRound(Player player, RoundSM round) {
        String message = "§e" + player.getName() + " §ahat die Runde betreten. (" + round.players.size() + "/" + round.map.spawnpoints.size() + ")";
        for (Player p : round.players.keySet()) {
            sendMessage(p, message);
        }
    }

    public static void playerLeaveRound(Player player, RoundSM round) {
        String message = "§7" + player.getName() + " §chat die Runde verlassen!";
        for (Player p : round.players.keySet()) {
            sendMessage(p, message);
        }
    }

    public static void playerSpectateRound(Player player, RoundSM round) {
        String message = "§e" + player.getName() + " §aschaut nun zu.";
        for (Player p : round.players.keySet()) {
            sendMessage(p, message);
        }
    }

    public static void startingMessage(RoundSM round) {
        String mainTitle = "§a" + round.map.name;
        String subTitle = "§7Gebaut von: " + round.map.builder;
        for (Player p : round.players.keySet()) {
            sendTitle(p, mainTitle, subTitle);
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        }
    }
}
