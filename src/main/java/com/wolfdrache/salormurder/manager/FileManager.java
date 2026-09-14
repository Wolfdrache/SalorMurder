package com.wolfdrache.salormurder.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.ArmorstandLB;
import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.MapSM;
import com.wolfdrache.salormurder.models.PlayerStats;

public class FileManager {
    private final SalorMurder plugin;

    private final File mapsFolder;
    private final File statsFile;
    private final File armorstandsFile;
    
    public FileManager(SalorMurder plugin) {
        this.plugin = plugin;

        this.mapsFolder = new File(plugin.getDataFolder(), "maps");
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.armorstandsFile = new File(plugin.getDataFolder(), "armorstands.yml");
    }

    public List<MapSM> loadMaps() {
        List<MapSM> maps = new ArrayList<>();

        if (!mapsFolder.exists() || !mapsFolder.isDirectory()) {
            plugin.getLogger().warning("Maps folder not found SalorMurder!");
            return maps;
        }

        File[] mapFiles = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (mapFiles == null || mapFiles.length == 0) {
            plugin.getLogger().warning("No map files found SalorMurder!");
            return maps;
        }
        
        for (File mapFile : mapFiles) {
            if (!mapFile.exists()) {
                continue;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
            String name = config.getString("name");
            String worldName = config.getString("worldName");
            String builder = config.getString("builder");

            List<Location> spawnpoints = new ArrayList<>();
            List<String> spawnpointsStrings = config.getStringList("spawnpoints");
            for (String spawnpointString : spawnpointsStrings) {
                spawnpoints.add(readLocation(spawnpointString));
            }

            List<Location> lootchests = new ArrayList<>();
            List<String> lootchestsStrings = config.getStringList("lootchests");
            for (String lootchestString : lootchestsStrings) {
                lootchests.add(readLocation(lootchestString));
            }

            maps.add(new MapSM(name, worldName, builder, spawnpoints, lootchests));
        }
        return maps;
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
    
    private Location getWorldLocation(String customLocation) {
        String locationString = plugin.getConfig().getString(customLocation + ".position");
        Location location = readLocation(locationString);
        location.setWorld(plugin.getServer().getWorld(plugin.getConfig().getString(customLocation + ".world")));
        return location;
    }
    private Location getWorldLocation(Map<?, ?> locationMap) {
        String locationString = (String) locationMap.get("position");
        Location location = readLocation(locationString);
        location.setWorld(plugin.getServer().getWorld((String) locationMap.get("world")));
        return location;
    }

    public Location getLeaveLocation() {
        return getWorldLocation("leaveLocation");
    }
    public Location getLobbyLocation() {
        return getWorldLocation("lobbyLocation");
    }

    public List<Location> getJoinSigns() {
        List<Location> joinSignLocations = new ArrayList<>();
        List<Map<?, ?>> joinSignMaps = plugin.getConfig().getMapList("joinSigns");
        if (joinSignMaps != null) {
            for (Map<?, ?> joinSignMap : joinSignMaps) {
                joinSignLocations.add(getWorldLocation(joinSignMap));
            }
        }
        return joinSignLocations;
    }

    public int getTime(Time time) {
        return plugin.getConfig().getInt("time." + time.name().toLowerCase());
    }

    public int getCoins(Coins coins) {
        return plugin.getConfig().getInt("coins." + coins.name().toLowerCase());
    }

    public int getMinPlayers() {
        return plugin.getConfig().getInt("minPlayers");
    }

    public List<UUID> getLeaderboard() {
        YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        List<UUID> leaderboard = new ArrayList<>();
        for (String uuid : statsConfig.getKeys(false)) {
            leaderboard.add(UUID.fromString(uuid));
        }
        leaderboard.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = loadPlayerStats(Bukkit.getOfflinePlayer(uuid1));
            PlayerStats stats2 = loadPlayerStats(Bukkit.getOfflinePlayer(uuid2));
            return Integer.compare(stats2.getPoints(), stats1.getPoints());
        });
        return leaderboard;
    }

    public ArmorstandLB loadArmorstandLB(int rank) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(armorstandsFile);
        String path = "rank." + rank;
        Map<?, ?> locationMap = config.getConfigurationSection(path + ".location").getValues(false);
        Location location = getWorldLocation(locationMap);
        EulerAngle headPose = parseEulerAngle(config.getString(path + ".headPose"));
        EulerAngle bodyPose = parseEulerAngle(config.getString(path + ".bodyPose"));
        EulerAngle leftArmPose = parseEulerAngle(config.getString(path + ".leftArmPose"));
        EulerAngle rightArmPose = parseEulerAngle(config.getString(path + ".rightArmPose"));
        EulerAngle leftLegPose = parseEulerAngle(config.getString(path + ".leftLegPose"));
        EulerAngle rightLegPose = parseEulerAngle(config.getString(path + ".rightLegPose"));
        EntityEquipment equipment = parseEquipment(config.getConfigurationSection(path + ".equipment"));
        return new ArmorstandLB(headPose, bodyPose, leftArmPose, rightArmPose, leftLegPose, rightLegPose, location, equipment);
    }
    private EulerAngle parseEulerAngle(String str) {
        String[] parts = str.split(" ");
        return new EulerAngle(
            Math.toRadians(Double.parseDouble(parts[0])),
            Math.toRadians(Double.parseDouble(parts[1])),
            Math.toRadians(Double.parseDouble(parts[2]))
        );
    }

    private EntityEquipment parseEquipment(ConfigurationSection section) {
        ItemStack helmet = readItemStack(section, "helmet");
        ItemStack chestplate = readItemStack(section, "chestplate");
        ItemStack leggings = readItemStack(section, "leggings");
        ItemStack boots = readItemStack(section, "boots");
        ItemStack itemInMainHand = readItemStack(section, "itemInMainHand");
        ItemStack itemInOffHand = readItemStack(section, "itemInOffHand");

        ArmorStand armorStand = (ArmorStand) Bukkit.getWorld("world").spawnEntity(new Location(Bukkit.getWorld("world"), 0, 64, 0), EntityType.ARMOR_STAND);
        EntityEquipment equipment = armorStand.getEquipment();
        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);
        equipment.setItemInMainHand(itemInMainHand);
        equipment.setItemInOffHand(itemInOffHand);
        armorStand.remove();
        return equipment;
    }
    private ItemStack readItemStack(ConfigurationSection parentSection, String path) {
        ConfigurationSection section = parentSection.getConfigurationSection(path);
        if (section != null) {
            try {
                return ItemStack.deserialize(toPlainMap(section));
            } catch (IllegalArgumentException ignored) {
                // Fallback keeps compatibility with already-deserialized values.
            }
        }

        return parentSection.getItemStack(path);
    }
    private Map<String, Object> toPlainMap(ConfigurationSection section) {
        Map<String, Object> plainMap = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                plainMap.put(key, toPlainMap((ConfigurationSection) value));
            } else {
                plainMap.put(key, value);
            }
        }
        return plainMap;
    }

    private Map<String, Object> toPlainMap(Map<?, ?> map) {
        Map<String, Object> plainMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                plainMap.put(key, toPlainMap((Map<?, ?>) value));
            } else if (value instanceof List<?>) {
                plainMap.put(key, toPlainList((List<?>) value));
            } else {
                plainMap.put(key, value);
            }
        }
        return plainMap;
    }

    private List<Object> toPlainList(List<?> list) {
        List<Object> plainList = new ArrayList<>();
        for (Object value : list) {
            if (value instanceof Map<?, ?>) {
                plainList.add(toPlainMap((Map<?, ?>) value));
            } else if (value instanceof List<?>) {
                plainList.add(toPlainList((List<?>) value));
            } else {
                plainList.add(value);
            }
        }
        return plainList;
    }

    public void savePlayerStats(Player player, PlayerStats stats) {
        YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        String playerUUID = player.getUniqueId().toString();
        statsConfig.set(playerUUID + ".name", player.getName());
        statsConfig.set(playerUUID + ".killedDetectives", stats.killedDetectives);
        statsConfig.set(playerUUID + ".killedInnocents", stats.killedInnocents);
        statsConfig.set(playerUUID + ".murderersKilled", stats.murderersKilled);
        statsConfig.set(playerUUID + ".roundsWonMurderer", stats.roundsWonMurderer);
        statsConfig.set(playerUUID + ".roundsWonInnocent", stats.roundsWonInnocent);
        statsConfig.set(playerUUID + ".roundsLostMurderer", stats.roundsLostMurderer);
        statsConfig.set(playerUUID + ".roundsLostInnocent", stats.roundsLostInnocent);
        statsConfig.set(playerUUID + ".randomKills", stats.randomKills);
        statsConfig.set(playerUUID + ".coins", stats.coins);
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerStats loadPlayerStats(OfflinePlayer player) {
        YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        String playerUUID = player.getUniqueId().toString();
        int killedDetectives = statsConfig.getInt(playerUUID + ".killedDetectives", 0);
        int killedInnocents = statsConfig.getInt(playerUUID + ".killedInnocents", 0);
        int murderersKilled = statsConfig.getInt(playerUUID + ".murderersKilled", 0);
        int roundsWonMurderer = statsConfig.getInt(playerUUID + ".roundsWonMurderer", 0);
        int roundsWonInnocent = statsConfig.getInt(playerUUID + ".roundsWonInnocent", 0);
        int roundsLostMurderer = statsConfig.getInt(playerUUID + ".roundsLostMurderer", 0);
        int roundsLostInnocent = statsConfig.getInt(playerUUID + ".roundsLostInnocent", 0);
        int randomKills = statsConfig.getInt(playerUUID + ".randomKills", 0);
        int coins = statsConfig.getInt(playerUUID + ".coins", 0);

        return new PlayerStats(killedDetectives, killedInnocents, murderersKilled, roundsWonMurderer, roundsWonInnocent, roundsLostMurderer, roundsLostInnocent, randomKills, coins);
    }
}
