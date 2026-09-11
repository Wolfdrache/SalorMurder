package com.wolfdrache.salormurder.timer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.manager.FileManager;
import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class RoundTimer {
    private final SalorMurder plugin;
    private final RoundManager roundManager;
    private final FileManager fileManager;

    private final Map<RoundSM, BukkitTask> roundTasks = new HashMap<>(); 

    public RoundTimer(SalorMurder plugin, RoundManager roundManager, FileManager fileManager) {
        this.plugin = plugin;
        this.roundManager = roundManager;
        this.fileManager = fileManager;
    }

    public void startTimer(RoundSM round) {
        BukkitTask task = roundTasks.get(round);
        if (task != null && !task.isCancelled()) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> update(round), 0L, 20L);
        roundTasks.put(round, task);
    }

    public void stopTimer(RoundSM round) {
        BukkitTask task = roundTasks.get(round);
        if (task != null) {
            task.cancel();
            roundTasks.remove(round);
        }
    }
    private void update(RoundSM round) {
        if (round.mode == RoundMode.EDIT) {
            stopTimer(round);
            return;
        }

        for (Player player : round.players.keySet()) {
            player.setSaturation(20);
            player.setFoodLevel(20);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }

        if (round.mode == RoundMode.WAITING) {
            int lobbyTime = fileManager.getTime(Time.LOBBY);
            if (!roundManager.enoughPlayersToStart(round)) {
                round.time = lobbyTime;
                return;
            } else if (round.time == 5) {
                MessageHelper.startingMessage(round);
            } else if (round.time == 0) {
                roundManager.startRound(round);
                return;
            }
            round.time--;
        }
    }
}
