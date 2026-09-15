package com.wolfdrache.salormurder.listener;

import org.bukkit.Location;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.murderknifes.api.MurderKnfesAPI;
import com.wolfdrache.salormurder.helper.LootChestHelper;
import com.wolfdrache.salormurder.items.NavItems;
import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;
import com.wolfdrache.salormurder.ui.TeleporterGui;

public class InteractionListener implements Listener {
    private final MurderKnfesAPI murderKnfes;
    private final RoundManager roundManager;
    private final TeleporterGui teleporterGui;

    public InteractionListener(MurderKnfesAPI murderKnfes, RoundManager roundManager, TeleporterGui teleporterGui) {
        this.murderKnfes = murderKnfes;
        this.roundManager = roundManager;
        this.teleporterGui = teleporterGui;
    }

    @EventHandler 
    public void onItemInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        if (item.equals(NavItems.leaveItem)) {
            event.setCancelled(true);
            roundManager.leavePlayer(player);
            return;
        } else if (item.equals(NavItems.knifeSelectorItem)) {
            event.setCancelled(true);
            murderKnfes.openKnifeSelector(player);
            return;
        } else if (item.equals(NavItems.spectatorTpItem)) {
            event.setCancelled(true);
            teleporterGui.open(player);
            return;
        }
    }

    @EventHandler 
    public void onNonAliveInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        PlayerSM playerSM = round.players.get(player);
        if (playerSM.mode != PlayerMode.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler 
    public void onLootChestInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorstand)) return;
        if (!LootChestHelper.isLootChest(armorstand)) return;
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        PlayerSM playerSM = round.players.get(player);
        if (playerSM.mode != PlayerMode.PLAYING) {
            event.setCancelled(true);
            return;
        }
        Location location = armorstand.getLocation();
        roundManager.playerLootChest(round, player, location);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        PlayerSM playerSM = round.players.get(player);
        if (playerSM.mode != PlayerMode.PLAYING) {
            event.setCancelled(true);
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getState() instanceof Container || block.getBlockData() instanceof Bed) {
            event.setCancelled(true);
        }
    }

    @EventHandler 
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        event.setCancelled(true);
    }
}
