package com.wolfdrache.salormurder.listener;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.wolfdrache.salormurder.manager.JoinSignManager;

public class JoinSignListener implements Listener {
    private final JoinSignManager joinSignManager;

    public JoinSignListener(JoinSignManager joinSignManager) {
        this.joinSignManager = joinSignManager;
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if  (!(event.getClickedBlock().getState() instanceof Sign sign)) return;
        joinSignManager.interactWithSign(sign.getLocation(), event.getPlayer());
    }
}
