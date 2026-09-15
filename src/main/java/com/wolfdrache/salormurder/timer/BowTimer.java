package com.wolfdrache.salormurder.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.items.NavItems;
import com.wolfdrache.salormurder.manager.FileManager;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.ui.XpBarTimer;

public class BowTimer {
    private static class Data {
        public int completeTime;
        public int time;
        public BukkitTask task;

        public Data(int time, BukkitTask task) {
            this.completeTime = time;
            this.time = time;
            this.task = task;
        }
    }

    private final SalorMurder plugin;
    private final FileManager fileManager;

    private final Map<Player, Data> bowTasks = new HashMap<>();

    public BowTimer(SalorMurder plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
    }

    public void startBowTimer(Player player) {
        Data data = bowTasks.get(player);
        if (data != null && !data.task.isCancelled()) {
            data.task.cancel();
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            update(player);
        }, 0, 20L);
        int bowCooldown = fileManager.getTime(Time.TRIDENT_RELOAD);
        bowTasks.put(player, new Data(bowCooldown, task));
    }

    public void stopBowTimer(Player player) {
        Data data = bowTasks.get(player);
        if (data != null && !data.task.isCancelled()) {
            data.task.cancel();
        }
        bowTasks.remove(player);
    }

    public void stopTimerRound(RoundSM round) {
        for (Player player : round.players.keySet()) {
            stopBowTimer(player);
        }
    }

    public void stopAllBowTimers() {
        for (Player player : new ArrayList<>(bowTasks.keySet())) {
            stopBowTimer(player);
        }
    }

    private void update(Player player) {
        Data data = bowTasks.get(player);
        if (data == null) {
            return;
        }
        data.time--;
        XpBarTimer.updateXpBar(player, data.time, data.completeTime);
        if (data.time <= 0) {
            stopBowTimer(player);
            player.getInventory().setItem(8, NavItems.arrowItem);
        }
    }
}
