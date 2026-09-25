package com.wolfdrache.salormurder.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.LootChest;
import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.MapSM;
import com.wolfdrache.salormurder.models.PlayerStats;

public class FileManager {
    private final SalorMurder plugin;

    private final File mapsFolder;
    private final File statsFile;
    
    public FileManager(SalorMurder plugin) {
        this.plugin = plugin;

        this.mapsFolder = new File(plugin.getDataFolder(), "maps");
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
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
        String locationString = plugin.getConfig().getString("lobbyLocation.position");
        Location location = readLocation(locationString);
        World world = plugin.getServer().createWorld(new WorldCreator(plugin.getConfig().getString("lobbyLocation.world")));
        location.setWorld(world);
        return location;
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

    public ItemStack getTridentCost() {
        String tridentMaterialString = plugin.getConfig().getString("trident.material", "EMERALD");
        int tridentAmount = plugin.getConfig().getInt("trident.amount", 1);
        Material tridentMaterial = Material.getMaterial(tridentMaterialString);
        List<Material> lootableMaterials = LootChest.getLootTableMaterials();
        if (tridentMaterial == null || !lootableMaterials.contains(tridentMaterial)) {
            if (lootableMaterials.contains(Material.EMERALD)) {
                tridentMaterial = Material.EMERALD;
            } else {
                tridentMaterial = lootableMaterials.get(0);
            }
        }
        return new ItemStack(tridentMaterial, tridentAmount);
    }
}
