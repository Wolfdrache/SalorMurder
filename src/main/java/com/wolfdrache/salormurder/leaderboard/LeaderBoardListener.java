package com.wolfdrache.salormurder.leaderboard;

import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class LeaderBoardListener implements Listener {
    @EventHandler
    public void onLeaderboardArmorstandInteract(PlayerArmorStandManipulateEvent event) {
        ArmorStand armorStand = event.getRightClicked();
        if (!ArmorstandLB.isLeaderboardArmorstand(armorStand)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeaderboardArmorstandDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;
        if (!ArmorstandLB.isLeaderboardArmorstand(armorStand)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeaderboardArmorstandTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;
        if (!ArmorstandLB.isLeaderboardArmorstand(armorStand)) return;
        event.setCancelled(true);
    }
}
