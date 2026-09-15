package com.wolfdrache.salormurder.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.manager.StatsManager;
import com.wolfdrache.salormurder.models.LootChest;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class PlayerListener implements Listener {
    private final RoundManager roundManager;
    private final StatsManager statsManager;

    public PlayerListener(RoundManager roundManager, StatsManager statsManager) {
        this.roundManager = roundManager;
        this.statsManager = statsManager;
    }

    @EventHandler 
    public void onPlayerJoin(PlayerJoinEvent event) {
        statsManager.loadPlayerStats(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        statsManager.savePlayerStats(event.getPlayer());
    }

    @EventHandler 
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        if (round.mode != RoundMode.RUNNING) {
            event.setCancelled(true);
            return;
        }
        PlayerSM playerSM = round.players.get(player);
        if (playerSM == null) return;
        if (playerSM.mode != PlayerMode.PLAYING) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItemDrop().getItemStack();
        if (!LootChest.getLootTableMaterials().contains(item.getType())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler 
    public void onPlayerItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        if (round.mode != RoundMode.RUNNING) {
            event.setCancelled(true);
            return;
        }
        PlayerSM playerSM = round.players.get(player);
        if (playerSM == null) return;
        if (playerSM.mode != PlayerMode.PLAYING) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem().getItemStack();
        if (!LootChest.getLootTableMaterials().contains(item.getType())) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        roundManager.checkPlayerGetTrident(player);
    }
}
