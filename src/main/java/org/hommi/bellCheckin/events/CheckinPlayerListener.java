package org.hommi.bellCheckin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.hommi.bellCheckin.model.CheckinInfo;

import java.util.UUID;

public class CheckinPlayerListener implements Listener {

    private final BellCheckin plugin;

    public CheckinPlayerListener() {
        plugin = BellCheckin.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin
                .getCheckinManager()
                .addOrUpdate(new CheckinInfo(uuid, plugin.getCheckinManager().getCheckinFromDb(uuid).getLastCheckin()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // Lưu dữ liệu điểm danh khi người chơi thoát
        CheckinManager checkinManager = BellCheckin.getInstance().getCheckinManager();
        if (checkinManager.contains(playerUuid)) {
            CheckinInfo info = checkinManager.get(playerUuid);
            checkinManager.saveToDb(info);
        }
    }
}