package com.wolfdrache.salormurder.leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
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
        EntityEquipment equipment = parseEquipment(equipmentSection);

        if (headPose == null || bodyPose == null || leftArmPose == null || rightArmPose == null || leftLegPose == null || rightLegPose == null || equipment == null) {
            plugin.getLogger().warning("Invalid armorstand config for leaderboard rank " + rank);
            return null;
        }

        return new ArmorstandLB(headPose, bodyPose, leftArmPose, rightArmPose, leftLegPose, rightLegPose, location, equipment);
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

    private EntityEquipment parseEquipment(ConfigurationSection section) {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().warning("Could not load world 'world' for leaderboard armorstand equipment");
            return null;
        }

        ItemStack helmet = readItemStack(section, "helmet");
        ItemStack chestplate = readItemStack(section, "chestplate");
        ItemStack leggings = readItemStack(section, "leggings");
        ItemStack boots = readItemStack(section, "boots");
        ItemStack itemInMainHand = readItemStack(section, "itemInMainHand");
        ItemStack itemInOffHand = readItemStack(section, "itemInOffHand");

        ArmorStand armorStand = (ArmorStand) world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ARMOR_STAND);
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(helmet);
            equipment.setChestplate(chestplate);
            equipment.setLeggings(leggings);
            equipment.setBoots(boots);
            equipment.setItemInMainHand(itemInMainHand);
            equipment.setItemInOffHand(itemInOffHand);
        }
        armorStand.remove();
        return equipment;
    }

    private ItemStack readItemStack(ConfigurationSection section, String path) {
        Object raw = section.get(path);
        if (raw instanceof ItemStack itemStack) {
            return itemStack;
        }

        ConfigurationSection sub = section.getConfigurationSection(path);
        if (sub != null) {
            try {
                return ItemStack.deserialize(toPlainMap(sub));
            } catch (IllegalArgumentException ignored) {
                // Fallback below keeps compatibility with already-deserialized values.
            }
        }

        return section.getItemStack(path);
    }

    private Map<String, Object> toPlainMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, toPlainMap((ConfigurationSection) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }
}