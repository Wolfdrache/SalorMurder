package com.wolfdrache.salormurder.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.wolfdrache.salormurder.SalorMurder;
import com.wolfdrache.salormurder.models.ConfigModes.Coins;
import com.wolfdrache.salormurder.models.ConfigModes.Time;
import com.wolfdrache.salormurder.models.MapSM;

public class FileManager {
    private final SalorMurder plugin;

    private final File mapsFolder;
    
    public FileManager(SalorMurder plugin) {
        this.plugin = plugin;

        this.mapsFolder = new File(plugin.getDataFolder(), "maps");
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

    public List<Location> getJoinSignLocations() {
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
}
