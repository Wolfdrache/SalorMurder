package com.wolfdrache.salormurder.manager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import com.wolfdrache.salormurder.models.MapSM;

public class MapManager {
    private final FileManager fileManager;
    private final List<MapSM> maps;
    private final Server server;

    private final Map<MapSM, Object> worldLoadingTasks = new HashMap<>();

    public MapManager(FileManager fileManager){
        this.fileManager = fileManager;
        maps = this.fileManager.loadMaps();
        server = Bukkit.getServer();
    }

    public void addWorldToMap(MapSM map, World world){
        map.world = world;

        for (Location spawnpoint : map.spawnpoints) {
            spawnpoint.setWorld(world);
        }
        for (Location lootchest : map.lootchests) {
            lootchest.setWorld(world);
        }
    }

    public List<MapSM> getMaps(){
        return maps;
    }

    public World getOrLoadWorld(MapSM map) {
        if (map == null || map.worldName == null) {
            return null;
        }

        synchronized (worldLoadingTasks.computeIfAbsent(map, k -> new Object())) {
            World world = server.getWorld(map.worldName);
            if (world != null) {
                world.setAutoSave(false);
                return world;
            }

            File worldFolder = new File(server.getWorldContainer(), map.worldName);
            if (!worldFolder.exists()) {
                return null;
            }

            world = server.createWorld(new WorldCreator(map.worldName));
            if (world != null) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                world.setGameRule(GameRule.NATURAL_REGENERATION, true);
                world.setAutoSave(false);
            }
            return world;
        }
    }

    public void unloadWorld(MapSM map) {
        if (map == null || map.world == null) {
            return;
        }

        if (map.world != null) {
            server.unloadWorld(map.world, false);
            map.world = null;
        }
    }

    public void saveWorld(MapSM map) {
        if (map == null || map.world == null) {
            return;
        }

        map.world.save();
        server.unloadWorld(map.world, true);
    }
}
