package org.hommi.bellCheckin.manager;

import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.model.CheckinInfo;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckinManager {
    BellCheckin plugin;
    public CheckinManager() {
        plugin = BellCheckin.getInstance();
    }

    private final Map<UUID, CheckinInfo> checkinMap = new ConcurrentHashMap<>();

    public void addOrUpdate(CheckinInfo info) {
        checkinMap.put(info.getUuid(), info);
    }

    public CheckinInfo get(UUID uuid) {
        return checkinMap.get(uuid);
    }

    public void remove(UUID uuid) {
        checkinMap.remove(uuid);
    }

    public boolean contains(UUID uuid) {
        return checkinMap.containsKey(uuid);
    }

    public CheckinInfo getCheckinFromDb(UUID uuid){
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "SELECT lastCheckin FROM checkin WHERE userUuid = ?";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (var rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        long lastCheckin = rs.getLong("lastCheckin");
                        return new CheckinInfo(uuid, lastCheckin);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error retrieving check-in info from database: " + e.getMessage());
        }
        return new CheckinInfo(uuid, 0);
    }

    public void saveToDb(CheckinInfo info) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "INSERT INTO checkin (userUuid, lastCheckin) VALUES (?, ?) " +
                         "ON CONFLICT(userUuid) DO UPDATE SET lastCheckin = excluded.lastCheckin";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, info.getUuid().toString());
                pstmt.setLong(2, info.getLastCheckin());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving check-in info to database: " + e.getMessage());
        }
    }

    /**
     * Determines if the player can check in at the given time.
     * @param uuid Player UUID
     * @param now Current epoch seconds
     * @return true if eligible, false otherwise
     */
    public boolean canCheckin(UUID uuid, long now) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }
        return now - info.getLastCheckin() >= 86400;
    }

    /**
     * Updates the player's last check-in time to now.
     * @param uuid Player UUID
     * @param now Current epoch seconds
     */
    public void checkin(UUID uuid, long now) {
        get(uuid).setLastCheckin(now);
    }

    /**
     * Returns seconds until the player can check in again.
     * @param uuid Player UUID
     * @param now Current epoch seconds
     * @return seconds left, or 0 if eligible
     */
    public long secondsUntilNextCheckin(UUID uuid, long now) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }
        long elapsed = now - info.getLastCheckin();
        long wait = 86400 - elapsed;
        return wait > 0 ? wait : 0;
    }
}
