package com.wolfdrache.salormurder.leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.wolfdrache.salormurder.SalorMurder;

public class LeaderBoardFileManager {
    private final SalorMurder plugin;
    private final File statsFile;
    private final File armorstandsFile;

    public LeaderBoardFileManager(SalorMurder plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.armorstandsFile = new File(plugin.getDataFolder(), "armorstands.yml");

        if (!armorstandsFile.exists()) {
            plugin.saveResource("armorstands.yml", false);
        }
    }

    public List<UUID> getLeaderboard() {
        YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        List<UUID> leaderboard = new ArrayList<>();
        for (String uuid : statsConfig.getKeys(false)) {
            leaderboard.add(UUID.fromString(uuid));
        }
        return leaderboard;
    }

    public ArmorstandLB loadArmorstandLB(int rank) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(armorstandsFile);
        String path = "rank." + rank;

        ConfigurationSection locationSection = config.getConfigurationSection(path + ".location");
        ConfigurationSection equipmentSection = config.getConfigurationSection(path + ".equipment");
        if (locationSection == null || equipmentSection == null) {
            plugin.getLogger().warning("Missing armorstand config for leaderboard rank " + rank);
            return null;
        }

        Location location = getWorldLocation(locationSection.getValues(false));
        EulerAngle headPose = parseEulerAngle(config.getString(path + ".headPose"));
        EulerAngle bodyPose = parseEulerAngle(config.getString(path + ".bodyPose"));
        EulerAngle leftArmPose = parseEulerAngle(config.getString(path + ".leftArmPose"));
        EulerAngle rightArmPose = parseEulerAngle(config.getString(path + ".rightArmPose"));
        EulerAngle leftLegPose = parseEulerAngle(config.getString(path + ".leftLegPose"));
        EulerAngle rightLegPose = parseEulerAngle(config.getString(path + ".rightLegPose"));
        boolean small = config.getBoolean(path + ".small");
        ItemStack chestplate = readItemStack(equipmentSection, "chestplate");
        ItemStack leggings = readItemStack(equipmentSection, "leggings");
        ItemStack boots = readItemStack(equipmentSection, "boots");
        ItemStack itemInMainHand = readItemStack(equipmentSection, "itemInMainHand");
        ItemStack itemInOffHand = readItemStack(equipmentSection, "itemInOffHand");

        if (headPose == null || bodyPose == null || leftArmPose == null || rightArmPose == null || leftLegPose == null || rightLegPose == null) {
            plugin.getLogger().warning("Invalid armorstand config for leaderboard rank " + rank);
            return null;
        }

        return new ArmorstandLB(headPose, bodyPose, leftArmPose, rightArmPose, leftLegPose, rightLegPose, small, location, chestplate, leggings, boots, itemInMainHand, itemInOffHand);
    }

    private ItemStack readItemStack(ConfigurationSection section, String key) {
        if (section == null || !section.contains(key)) {
            return null;
        }
        String itemString = section.getString(key + ".type");
        boolean enchanted = section.getBoolean(key + ".enchanted", false);
        Material material = Material.getMaterial(itemString);
        if (material == null) {
            plugin.getLogger().warning("Invalid material for " + key + ": " + itemString);
            return null;
        }
        ItemStack itemStack = new ItemStack(material);
        if (enchanted) {
            itemStack.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1); // Example enchantment, adjust as needed
        }
        return itemStack;
    }

    private Location getWorldLocation(Map<?, ?> locationMap) {
        String locationString = (String) locationMap.get("position");
        Location location = readLocation(locationString);
        location.setWorld(plugin.getServer().getWorld((String) locationMap.get("world")));
        return location;
    }

    private Location readLocation(String locationString) {
        String[] parts = locationString.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        float yaw = parts.length > 3 ? Float.parseFloat(parts[3]) : 0;
        float pitch = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        return new Location(null, x, y, z, yaw, pitch);
    }

    private EulerAngle parseEulerAngle(String str) {
        if (str == null) {
            return null;
        }
        String[] parts = str.split(" ");
        if (parts.length < 3) {
            return null;
        }
        return new EulerAngle(
            Math.toRadians(Double.parseDouble(parts[0])),
            Math.toRadians(Double.parseDouble(parts[1])),
            Math.toRadians(Double.parseDouble(parts[2]))
        );
    }
}