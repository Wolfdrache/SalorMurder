package com.wolfdrache.salormurder.listener;

import org.bukkit.Location;
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

public class InteractionListener implements Listener {
    private final MurderKnfesAPI murderKnfes;
    private final RoundManager roundManager;

    public InteractionListener(MurderKnfesAPI murderKnfes, RoundManager roundManager) {
        this.murderKnfes = murderKnfes;
        this.roundManager = roundManager;
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
}
