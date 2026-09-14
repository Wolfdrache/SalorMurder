package com.wolfdrache.salormurder.models;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

public class MapSM {
    public final String name;
    public final String worldName;
    public final String builder;

    public final List<Location> spawnpoints;
    public final List<Location> lootchests;

    public World world;

    public MapSM(String name, String worldName, String builder, List<Location> spawnpoints, List<Location> lootchests) {
        this.name = name;
        this.worldName = worldName;
        this.builder = builder;
        this.spawnpoints = spawnpoints;
        this.lootchests = lootchests;
    }

    public int getMaxPlayers() {
        return spawnpoints.size();
    }
}
