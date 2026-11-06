package com.wolfdrache.salormurder.helper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class LootChestHelper {
    private final static String LOOT_CHEST_KEY = "loot_chest";

    public static ArmorStand createArmorStand(Location armorstandLocation) {
        ArmorStand armorstand = armorstandLocation.getWorld().spawn(armorstandLocation, ArmorStand.class);
        armorstand.setVisible(false);
        armorstand.setInvulnerable(true);
        armorstand.setGravity(false);
        armorstand.addScoreboardTag(LOOT_CHEST_KEY);
        armorstand.setSmall(true);
        EntityEquipment equipment = armorstand.getEquipment(); 
        equipment.clear(); 
        equipment.setHelmet(new ItemStack(Material.CHEST)); 
        return armorstand;
    }
    
    public static boolean isLootChest(ArmorStand armorstand) {
        return armorstand.getScoreboardTags().contains(LOOT_CHEST_KEY.toString());
    }
}
