package com.wolfdrache.salormurder.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.wolfdrache.salormurder.models.PlayerSM.PlayerMode;

public class RoundSM {
    public static enum RoundMode {
        WAITING,
        STARTING,
        RUNNING,
        ENDING,
        EDIT
    }

    public final Map<Player, PlayerSM> players = new HashMap<>();
    public final MapSM map;
    public RoundMode mode = RoundMode.WAITING;
    public int time;
    private final Map<Location, LootChest> lootChests = new HashMap<>();

    public RoundSM(MapSM map) {
        this.map = map;
    }

    public boolean addPlayer(Player player) {
        if (players.containsKey(player)) return false;
        if (mode == RoundMode.WAITING) {
            players.put(player, new PlayerSM(PlayerMode.WAITING));
        } else if (mode == RoundMode.RUNNING) {
            players.put(player, new PlayerSM(PlayerMode.SPECTATING));
        } else if (mode == RoundMode.EDIT) {
            players.put(player, new PlayerSM(PlayerMode.EDIT));
        } else if (mode == RoundMode.ENDING) {
            return false;
        }
        return true;
    }

    public boolean hasFreeSlot() {
        return players.size() < map.spawnpoints.size();
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    public List<Player> getPlayersByMode(PlayerMode mode) {
        return players.entrySet().stream()
            .filter(entry -> entry.getValue().mode == mode)
            .map(Map.Entry::getKey)
            .toList();
    }

    public void initializeLootChests() {
        lootChests.clear();
        for (Location location : map.lootchests) {
            lootChests.put(location, new LootChest(location));
        }
    }

    public void refillLootChests() {
        for (Location location : map.lootchests) {
            LootChest lootChest = lootChests.get(location);
            if (lootChest == null) {
                lootChests.put(location, new LootChest(location));
            } else if (lootChest.armorstand == null || lootChest.armorstand.isDead()) {
                lootChests.put(location, new LootChest(location));
            } else {
                lootChest.refill();
            }
        }
    }

    public void playerLootChest(Player player, Location location) {
        LootChest lootChest = lootChests.get(location);
        if (lootChest == null) return;
        PlayerInventory inventory = player.getInventory();
        if (inventory.getItem(0) == null) inventory.setItem(0, new ItemStack(Material.BARRIER)); 
        if (inventory.getItem(8) == null) inventory.setItem(8, new ItemStack(Material.BARRIER));
        for (Material material : lootChest.items.keySet()) {
            int amount = lootChest.items.get(material);
            ItemStack itemStack = new ItemStack(material, amount);
            inventory.addItem(itemStack);
        }
        inventory.remove(Material.BARRIER);
        lootChests.remove(location);
    }

    public void addLootChest(Location location, Player player) {
        LootChest lootChest = new LootChest(location);
        lootChest.items.clear();
        PlayerInventory inventory = player.getInventory();
        for (Material material : LootChest.getLootTableMaterials()) {
            if (inventory.contains(material)) {
                int amount = inventory.all(material).values().stream().mapToInt(ItemStack::getAmount).sum();
                lootChest.items.put(material, amount);
            }
        }
        lootChests.put(location, lootChest);
    }

    public void removeAllLootChests() {
        for (LootChest lootChest : lootChests.values()) {
            if (lootChest.armorstand != null && !lootChest.armorstand.isDead()) {
                lootChest.armorstand.remove();
            }
        }
        lootChests.clear();
    }
}
