package com.wolfdrache.salormurder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class ForceJoinCommand implements TabExecutor {
    private final RoundManager roundManager;

    public ForceJoinCommand(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    @Override 
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }

        if (args.length < 2) {
            // MessageHelper.sendMessage(player, "§cNutzung: /forcejoinbw <@r | [Runde]> <@a | [Spieler1] [Spieler2] ...>");
            return false;
        }

        String roundName = args[0];
        RoundSM round = roundManager.getRoundByName(roundName);
        if (round == null) {
            MessageHelper.sendMessage(player, "§cRunde '" + roundName + "' nicht gefunden!");
            return false;
        }

        if (round.mode != RoundMode.WAITING) {
            MessageHelper.sendMessage(player, "§cRunde '" + roundName + "' ist nicht im Wartezustand!");
            return false;
        }

        List<Player> targetPlayers = new ArrayList<>();

        if (Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("@a")) && args.length > 2) {
            MessageHelper.sendMessage(player, "§cDu kannst nicht @a mit individuelen Spielern kombinieren.");
            return false;
        }
        if (args[1].equalsIgnoreCase("@a")) {
            targetPlayers.addAll(player.getServer().getOnlinePlayers());
        } else {
            for (int i = 1; i < args.length; i++) {
                Player target = player.getServer().getPlayer(args[i]);
                if (target != null) {
                    targetPlayers.add(target);
                } else {
                    MessageHelper.sendMessage(player, "§cSpieler nicht gefunden: " + args[i]);
                }
            }
        }

        for (Player targetPlayer : targetPlayers) {
            roundManager.joinPlayer(targetPlayer, round);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialRoundName = args[0].toLowerCase();
            List<String> roundNames = new ArrayList<>();
            roundNames.add("@r");
            roundNames.addAll(roundManager.getRounds().stream()
                .map(round -> round.map.name)
                .toList());

            return roundNames.stream().filter(round -> round.toLowerCase().startsWith(partialRoundName)).toList();
        }

        if (args.length >= 2) {
            Set<String> selectedPlayers = Arrays.stream(args, 1, args.length - 1)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            String currentPartial = args[args.length - 1].toLowerCase();

            if (selectedPlayers.contains("@a") || currentPartial.equals("@a")){
                return List.of();
            }

            List<String> suggestions = new ArrayList<>();

            if ("@a".startsWith(currentPartial) && !selectedPlayers.contains("@a")) {
                suggestions.add("@a");
            }
            for (Player player : sender.getServer().getOnlinePlayers()) {
                String name = player.getName();
                if (selectedPlayers.contains(name.toLowerCase())) continue;
                if (!name.toLowerCase().startsWith(currentPartial)) continue;
                suggestions.add(name);
            }
            return suggestions;
        }
        return List.of();
    }
}
