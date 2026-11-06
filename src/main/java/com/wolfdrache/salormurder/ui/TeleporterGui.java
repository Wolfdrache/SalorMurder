package com.wolfdrache.salormurder.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.wolfdrache.salormurder.helper.ItemHelper;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;

public class TeleporterGui implements Listener {
    private final RoundManager roundManager;

    private final Map<Player, Inventory> activeInventories = new HashMap<>();

    public TeleporterGui(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    public void open(Player player) {
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) {
            MessageHelper.sendMessage(player, "§cDu bist in keiner Runde.");
            return;
        }
        PlayerSM playerSM = round.players.get(player);
        if (playerSM == null) {
            MessageHelper.sendMessage(player, "§cSpielerdaten nicht gefunden.");
            return;
        }
        if (playerSM.mode != PlayerMode.SPECTATING) {
            MessageHelper.sendMessage(player, "§cDu kannst dich nur als Zuschauer teleportieren.");
            
            return;
        }
        List<Player> alivePlayers = round.getPlayersByMode(PlayerMode.PLAYING);
        if (alivePlayers.isEmpty()) {
            MessageHelper.sendMessage(player, "§cEs gibt keine lebenden Spieler zum Teleportieren.");
            return;
        }
        int size = ((alivePlayers.size() - 1) / 9 + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, MessageHelper.createComponent("§6Teleporter"));
        for (Player target : alivePlayers) {
            PlayerSM targetSM = round.players.get(target);
            if (targetSM != null && targetSM.mode == PlayerMode.PLAYING) {
                ItemStack head = ItemHelper.createPlayerHead(target);
                gui.addItem(head);
                
            }
        }
        player.openInventory(gui);
        activeInventories.put(player, gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory != null && activeInventories.containsKey(player) && topInventory.equals(activeInventories.get(player))) {
            event.setCancelled(true);
            RoundSM round = roundManager.getRoundByPlayer(player);
            if (round == null) {
                MessageHelper.sendMessage(player, "§cDu bist in keiner Runde.");
                return;
            }
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
                if (meta != null && meta.hasOwner()) {
                    Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                    if (target != null) {
                        PlayerSM targetSM = round.players.get(target);
                        if (targetSM == null || targetSM.mode != PlayerMode.PLAYING) {
                            MessageHelper.sendMessage(player, "§cDieser Spieler ist nicht mehr am Leben.");
                            return;
                        }
                        player.teleport(target);
                        String message = "§aDu wurdest zu " + target.getName() + " §ateleportiert.";
                        MessageHelper.sendMessage(player, message);
                        player.closeInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!activeInventories.containsKey(player)) return;
        if (!event.getInventory().equals(activeInventories.get(player))) return;
        activeInventories.remove(player);
    }
}
