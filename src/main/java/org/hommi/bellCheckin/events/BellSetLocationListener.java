package org.hommi.bellCheckin.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.commands.BciCommand;
import org.hommi.bellCheckin.model.BellLocation;

public class BellSetLocationListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Kiểm tra xem người chơi có đang trong chế độ thêm chuông không
        if (!BciCommand.addModePlayers.contains(player.getUniqueId())) {
            return;
        }

        // Chỉ xử lý khi nhấp chuột trái
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Kiểm tra xem block có phải là chuông không
        if (block.getType() != Material.BELL) {
            player.sendMessage(Component.text("Bạn phải nhấp vào chuông!")
                    .color(NamedTextColor.RED));
            return;
        }

        // Tạo vị trí chuông mới
        BellLocation bellLocation = BellLocation.fromLocation(block.getLocation());

        // Kiểm tra xem chuông đã được đăng ký chưa
        if (BellCheckin.getInstance().getBellLocationManager().isCheckinBell(
                bellLocation.worldName(),
                bellLocation.x(),
                bellLocation.y(),
                bellLocation.z())) {
            player.sendMessage(Component.text("Chuông này đã được đăng ký làm điểm điểm danh!")
                    .color(NamedTextColor.YELLOW));
            return;
        }

        // Thêm vị trí chuông mới
        BellCheckin.getInstance().getBellLocationManager().addBellLocation(bellLocation);

        // Thông báo thành công
        player.sendMessage(Component.text("Đã thiết lập chuông làm điểm điểm danh!")
                .color(NamedTextColor.GREEN));

        // Xóa người chơi khỏi chế độ thêm chuông
        BciCommand.addModePlayers.remove(player.getUniqueId());

        // Hủy sự kiện để không làm chuông kêu
        event.setCancelled(true);
    }
}
