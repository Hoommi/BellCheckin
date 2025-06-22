package org.hommi.bellCheckin.manager;

import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.model.CheckinInfo;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckinManager {
    private final BellCheckin plugin;
    private final Map<UUID, CheckinInfo> checkinMap = new ConcurrentHashMap<>();
    private static final long ONE_DAY_IN_SECONDS = 86400;

    public CheckinManager() {
        plugin = BellCheckin.getInstance();
    }

    /**
     * Thêm hoặc cập nhật thông tin điểm danh
     * 
     * @param info thông tin điểm danh
     */
    public void addOrUpdate(CheckinInfo info) {
        checkinMap.put(info.getUuid(), info);
    }

    /**
     * Lấy thông tin điểm danh của người chơi
     * 
     * @param uuid UUID của người chơi
     * @return thông tin điểm danh hoặc null nếu không tìm thấy
     */
    public CheckinInfo get(UUID uuid) {
        return checkinMap.get(uuid);
    }

    /**
     * Xóa thông tin điểm danh của người chơi
     * 
     * @param uuid UUID của người chơi
     */
    public void remove(UUID uuid) {
        checkinMap.remove(uuid);
    }

    /**
     * Kiểm tra xem người chơi đã có thông tin điểm danh chưa
     * 
     * @param uuid UUID của người chơi
     * @return true nếu đã có
     */
    public boolean contains(UUID uuid) {
        return checkinMap.containsKey(uuid);
    }

    /**
     * Lấy thông tin điểm danh từ database
     * 
     * @param uuid UUID của người chơi
     * @return thông tin điểm danh
     */
    public CheckinInfo getCheckinFromDb(UUID uuid) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "SELECT lastCheckin, streak, lastStreakReward FROM checkin WHERE userUuid = ?";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (var rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        long lastCheckin = rs.getLong("lastCheckin");
                        int streak = rs.getInt("streak");
                        int lastStreakReward = rs.getInt("lastStreakReward");
                        return new CheckinInfo(uuid, lastCheckin, streak, lastStreakReward);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi khi lấy thông tin điểm danh từ database: " + e.getMessage());
        }
        return new CheckinInfo(uuid, 0, 0, 0);
    }

    /**
     * Lưu thông tin điểm danh vào database
     * 
     * @param info thông tin điểm danh
     */
    public void saveToDb(CheckinInfo info) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "INSERT INTO checkin (userUuid, lastCheckin, streak, lastStreakReward) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(userUuid) DO UPDATE SET lastCheckin = excluded.lastCheckin, " +
                    "streak = excluded.streak, lastStreakReward = excluded.lastStreakReward";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, info.getUuid().toString());
                pstmt.setLong(2, info.getLastCheckin());
                pstmt.setInt(3, info.getStreak());
                pstmt.setInt(4, info.getLastStreakReward());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi khi lưu thông tin điểm danh vào database: " + e.getMessage());
        }
    }

    /**
     * Lưu tất cả thông tin điểm danh vào database
     */
    public void saveAllToDb() {
        checkinMap.values().forEach(this::saveToDb);
    }

    /**
     * Kiểm tra xem người chơi có thể điểm danh không
     * 
     * @param uuid UUID của người chơi
     * @param now  Thời gian hiện tại (epoch seconds)
     * @return true nếu có thể điểm danh
     */
    public boolean canCheckin(UUID uuid, long now) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }
        return now - info.getLastCheckin() >= ONE_DAY_IN_SECONDS;
    }

    /**
     * Cập nhật thông tin điểm danh của người chơi
     * 
     * @param uuid UUID của người chơi
     * @param now  Thời gian hiện tại (epoch seconds)
     * @return true nếu đạt được milestone streak
     */
    public boolean checkin(UUID uuid, long now) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }

        long lastCheckinTime = info.getLastCheckin();
        LocalDate lastDate = Instant.ofEpochSecond(lastCheckinTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = Instant.ofEpochSecond(now)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        // Nếu điểm danh ngày hôm qua, tăng streak
        if (lastDate.equals(yesterday)) {
            info.setStreak(info.getStreak() + 1);
        }
        // Nếu không phải ngày hôm qua và không phải hôm nay, reset streak
        else if (!lastDate.equals(today)) {
            info.setStreak(1);
        }

        info.setLastCheckin(now);

        // Kiểm tra xem có đạt được milestone streak không
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager.isStreakRewardEnabled() &&
                configManager.isStreakMilestone(info.getStreak()) &&
                info.getLastStreakReward() < info.getStreak()) {
            info.setLastStreakReward(info.getStreak());
            return true;
        }

        return false;
    }

    /**
     * Tính số giây còn lại cho đến khi người chơi có thể điểm danh lại
     * 
     * @param uuid UUID của người chơi
     * @param now  Thời gian hiện tại (epoch seconds)
     * @return số giây còn lại
     */
    public long secondsUntilNextCheckin(UUID uuid, long now) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }
        long elapsed = now - info.getLastCheckin();
        long wait = ONE_DAY_IN_SECONDS - elapsed;
        return wait > 0 ? wait : 0;
    }

    /**
     * Lấy thông tin streak hiện tại của người chơi
     * 
     * @param uuid UUID của người chơi
     * @return số ngày streak liên tiếp
     */
    public int getCurrentStreak(UUID uuid) {
        CheckinInfo info = get(uuid);
        if (info == null) {
            info = getCheckinFromDb(uuid);
            addOrUpdate(info);
        }
        return info.getStreak();
    }
}
