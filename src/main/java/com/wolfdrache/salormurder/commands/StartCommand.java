package com.wolfdrache.salormurder.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;

public class StartCommand implements TabExecutor {
    private final RoundManager roundManager;

    public StartCommand(RoundManager roundManager) {
        this.roundManager = roundManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) {
            return false;
        }
        roundManager.startCommand(round);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
