package org.hommi.bellCheckin.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.manager.BellLocationManager;

public class BellBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Chỉ xử lý khi phá chuông
        if (block.getType() != Material.BELL) {
            return;
        }

        Player player = event.getPlayer();
        BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();

        // Kiểm tra xem chuông có phải là chuông điểm danh không
        boolean isCheckinBell = bellLocationManager.isCheckinBell(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ());

        if (!isCheckinBell) {
            return;
        }

        // Kiểm tra quyền
        if (!player.hasPermission("bellcheckin.remove") && !player.isOp()) {
            player.sendMessage(Component.text("Bạn không có quyền phá chuông điểm danh!")
                    .color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Xóa chuông khỏi danh sách
        bellLocationManager.removeBellLocation(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ());

        player.sendMessage(Component.text("Đã xóa chuông điểm danh!")
                .color(NamedTextColor.GREEN));
    }
}
