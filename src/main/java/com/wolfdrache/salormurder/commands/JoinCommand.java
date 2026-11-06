package com.wolfdrache.salormurder.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;

public class JoinCommand implements TabExecutor {
    private final RoundManager roundManager;

    public JoinCommand(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        String roundName = args.length > 0 ? args[0] : "";
        RoundSM round = roundManager.getRoundByName(roundName);
        if (round == null) {
            player.sendMessage("Round not found.");
            return true;
        }
        roundManager.joinPlayer(player, round);
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
        return List.of();
    }
}
