package com.wolfdrache.salormurder.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

import com.wolfdrache.salormurder.helper.LootChestHelper;

public class LootChest {
    private static class LootTable {
        public final Material material;
        public final int max;
        public final int min;
        public final int chance;

        public LootTable(Material material, int max, int min, int chance) {
            this.material = material;
            this.max = max;
            this.min = min;
            this.chance = chance;
        }
    }
    private final static List<LootTable> lootTables = List.of(
        new LootTable(Material.EMERALD, 3, 1, 100)
    );

    public static List<Material> getLootTableMaterials() {
        return lootTables.stream().map(lootTable -> lootTable.material).toList();
    }

    public final Map<Material, Integer> items = new HashMap<>();
    public ArmorStand armorstand;

    public LootChest(Location armorstandLocation) {
        this.armorstand = LootChestHelper.createArmorStand(armorstandLocation);
        for (LootTable lootTable : lootTables) {
            int randomChance = ThreadLocalRandom.current().nextInt(100);
            if (randomChance < lootTable.chance) {
                int amount = ThreadLocalRandom.current().nextInt(lootTable.min, lootTable.max + 1);
                items.put(lootTable.material, amount);
            }
        }
    }

    public void refill() {
        for (LootTable lootTable : lootTables) {
            int randomChance = ThreadLocalRandom.current().nextInt(100);
            if (randomChance < lootTable.chance) {
                int amount = ThreadLocalRandom.current().nextInt(lootTable.min, lootTable.max + 1);
                amount = amount + items.getOrDefault(lootTable.material, 0);
                items.put(lootTable.material, amount);
            }
        }
    }
}
