package org.hommi.bellCheckin.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.model.BellLocation;

public class BellBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;
        BellLocation bellLocation = BellLocation.fromLocation(block.getLocation());
        boolean removed = BellCheckin.getInstance().getBellLocationManager().removeBellLocation(bellLocation);
        if (removed) {
            event.getPlayer().sendMessage("§aĐã xóa điểm checkin chuông vì chuông đã bị phá!");
        }
    }
}

