package com.wolfdrache.salormurder.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;

public class AdminCommand implements TabExecutor {
    private final RoundManager roundManager;
    private final List<String> subCommands = List.of("edit", "stop", "kill", "save");

    public AdminCommand(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        String roundName = args[0];
        RoundSM round = roundManager.getRoundByName(roundName);
        if (round == null) {
            player.sendMessage("Round not found.");
            return true;
        }
        String subCommand = args[1];
        if (!subCommands.contains(subCommand)) {
            player.sendMessage("Invalid sub-command.");
            return true;
        }
        if (subCommand.equalsIgnoreCase("edit")) {
            roundManager.editRound(player, round);
        } else if (subCommand.equalsIgnoreCase("stop")) {
            roundManager.stopRound(round);
        } else if (subCommand.equalsIgnoreCase("kill")) {
            String targetName = args[2];
            Player target = player.getServer().getPlayer(targetName);
            if (target == null) {
                player.sendMessage("Player not found.");
                return true;
            }
            roundManager.killPlayer(target, round);
        } else if (subCommand.equalsIgnoreCase("save")) {
            roundManager.saveRound(round);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return roundManager.getRounds().stream()
                .map(round -> round.map.name)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        if (args.length == 2) {
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[1].toLowerCase()))
                .toList();
        }
        if (args.length == 3) {
            String roundName = args[0];
            RoundSM round = roundManager.getRoundByName(roundName);
            if (round == null) {
                return List.of();
            }
            if (args[1].equalsIgnoreCase("kill")) {
                return round.getPlayersByMode(PlayerMode.PLAYING).stream()
                    .map(player -> player.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
            }
        }
        return List.of();
    }
}
