package org.hommi.bellCheckin.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.commands.BciCommand;
import org.hommi.bellCheckin.model.BellLocation;

public class BellSetLocationListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!BciCommand.addModePlayers.contains(player.getUniqueId())) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BELL) return;

        BellLocation bellLocation = BellLocation.fromLocation(block.getLocation());
        BellCheckin.getInstance().getBellLocationManager().saveBellLocation(bellLocation);
        player.sendMessage("§aBell check-in point set and saved to database!");
        BciCommand.addModePlayers.remove(player.getUniqueId());
        event.setCancelled(true);
    }
}

