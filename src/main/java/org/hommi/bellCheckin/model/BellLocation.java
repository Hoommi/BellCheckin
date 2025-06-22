package org.hommi.bellCheckin.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record BellLocation(String worldName, int x, int y, int z) {

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;
        return new Location(world, x, y, z);
    }

    public static BellLocation fromLocation(Location loc) {
        return new BellLocation(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }
}
