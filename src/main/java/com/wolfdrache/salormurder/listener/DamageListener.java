package com.wolfdrache.salormurder.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.murderknifes.api.MurderKnifesAPI;
import com.wolfdrache.salormurder.helper.MessageHelper;
import com.wolfdrache.salormurder.manager.CoinManager;
import com.wolfdrache.salormurder.manager.RoundManager;
import com.wolfdrache.salormurder.manager.StatsManager;
import com.wolfdrache.salormurder.models.PlayerSM;
import com.wolfdrache.salormurder.models.RoundSM;
import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;
import com.wolfdrache.salormurder.models.PlayerSM.Role;
import com.wolfdrache.salormurder.models.PlayerStats;
import com.wolfdrache.salormurder.models.RoundSM.RoundMode;

public class DamageListener implements Listener {
    private final MurderKnifesAPI murderKnifes;
    private final RoundManager roundManager;
    private final StatsManager statsManager;
    private final CoinManager coinManager;

    public DamageListener(MurderKnifesAPI murderKnifes, RoundManager roundManager, StatsManager statsManager, CoinManager coinManager) {
        this.murderKnifes = murderKnifes;
        this.roundManager = roundManager;
        this.statsManager = statsManager;
        this.coinManager = coinManager;
    }

    @EventHandler 
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        player.setFallDistance(0);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        if (round.mode == RoundMode.RUNNING) {
            event.setDamage(0);
            return;
        }
        event.setCancelled(true);
        player.setFallDistance(0);
    }

    @EventHandler 
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode != RoundMode.RUNNING) return;
        if (!round.players.containsKey(damager)) return;
        PlayerSM playerSM = round.players.get(player);
        PlayerSM damagerSM = round.players.get(damager);
        if (damagerSM.role == Role.MURDERER) {
            ItemStack weapon = damager.getInventory().getItemInMainHand();
            if (murderKnifes.isKnife(weapon)) {
                PlayerStats killerStats = statsManager.getPlayerStats(damager);
                if (playerSM.role == Role.INNOCENT) {
                    killerStats.killedInnocents++;
                    coinManager.giveCoins(player, Coins.MURDER_KILL_INNO);
                } else {
                    killerStats.killedDetectives++;
                    coinManager.giveCoins(player, Coins.MURDER_KILL_DETECTIVE);
                    MessageHelper.detectiveKilled(round);
                }
                roundManager.killPlayer(player, round);
            }
        }
    }

    @EventHandler 
    public void onTridentDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player damager)) return;
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode != RoundMode.RUNNING) return;
        if (!round.players.containsKey(damager)) return;
        PlayerSM playerSM = round.players.get(player);
        PlayerSM damagerSM = round.players.get(damager);
        if (damagerSM.role == Role.MURDERER) {
            PlayerStats killerStats = statsManager.getPlayerStats(damager);
            if (playerSM.role == Role.INNOCENT) {
                killerStats.killedInnocents++;
                coinManager.giveCoins(player, Coins.MURDER_KILL_INNO);
            } else {
                killerStats.killedDetectives++;
                coinManager.giveCoins(player, Coins.MURDER_KILL_DETECTIVE);
                MessageHelper.detectiveKilled(round);
            }
        } else {
            PlayerStats killerStats = statsManager.getPlayerStats(damager);
            if (playerSM.role == Role.MURDERER) {
                killerStats.murderersKilled++;
                coinManager.giveCoins(player, Coins.KILL_MURDER);
            } else {
                killerStats.randomKills++;
                coinManager.giveCoins(player, Coins.RANDOM_KILL);
                if (playerSM.role == Role.DETECTIVE) {
                    MessageHelper.detectiveKilled(round);
                }
            }
        }
        roundManager.killPlayer(player, round);
    }

    @EventHandler 
    public void onProjectileLand(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        RoundSM round = roundManager.getRoundByLocation(entity.getLocation());
        if (round == null) return;
        entity.remove();
    }

    @EventHandler 
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        RoundSM round = roundManager.getRoundByPlayer(player);
        if (round == null) return;
        if (round.mode != RoundMode.RUNNING) {
            player.teleport(round.map.world.getSpawnLocation());
            player.setFallDistance(0);
            return;
        }
        PlayerSM playerSM = round.players.get(player);
        if (playerSM.mode == PlayerMode.PLAYING) {
            roundManager.killPlayer(player, round);
        }
        player.teleport(round.map.world.getSpawnLocation());
        player.setFallDistance(0);
    }

    @EventHandler 
    public void onHangingDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Hanging hanging)) return;
        RoundSM round = roundManager.getRoundByLocation(hanging.getLocation());
        if (round == null) return;
        if (round.mode == RoundMode.EDIT) return;
        event.setCancelled(true);
    }
}
