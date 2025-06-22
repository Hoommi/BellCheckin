package org.hommi.bellCheckin.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.ConfigKey;

/**
 * Quản lý cấu hình của plugin
 */
public class ConfigManager {

    private final BellCheckin plugin;

    public ConfigManager(BellCheckin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lấy cấu hình từ file config.yml
     * 
     * @return FileConfiguration
     */
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * Kiểm tra xem tính năng điểm danh hàng ngày có được bật không
     * 
     * @return true nếu được bật
     */
    public boolean isDailyRewardEnabled() {
        return getConfig().getBoolean(ConfigKey.REWARDS_DAILY_ENABLED, true);
    }

    /**
     * Lấy số tiền thưởng cho việc điểm danh hàng ngày
     * 
     * @return số tiền thưởng
     */
    public int getDailyRewardAmount() {
        return getConfig().getInt(ConfigKey.REWARDS_DAILY_AMOUNT, 100);
    }

    /**
     * Lấy lệnh thực thi khi người chơi điểm danh
     * 
     * @return lệnh thực thi hoặc chuỗi rỗng nếu không có
     */
    public String getDailyRewardCommand() {
        return getConfig().getString(ConfigKey.REWARDS_DAILY_COMMAND, "");
    }

    /**
     * Kiểm tra xem tính năng thưởng streak có được bật không
     * 
     * @return true nếu được bật
     */
    public boolean isStreakRewardEnabled() {
        return getConfig().getBoolean(ConfigKey.REWARDS_STREAK_ENABLED, true);
    }

    /**
     * Lấy lệnh thực thi khi người chơi đạt được streak
     * 
     * @return lệnh thực thi hoặc chuỗi rỗng nếu không có
     */
    public String getStreakRewardCommand() {
        return getConfig().getString(ConfigKey.REWARDS_STREAK_COMMAND, "");
    }

    /**
     * Kiểm tra xem một streak có phải là milestone không
     * 
     * @param streak Số ngày streak liên tiếp
     * @return true nếu là milestone
     */
    public boolean isStreakMilestone(int streak) {
        return getConfig().getConfigurationSection(ConfigKey.REWARDS_STREAK_MILESTONES)
                .getKeys(false)
                .stream()
                .anyMatch(key -> Integer.parseInt(key) == streak);
    }

    /**
     * Lấy số tiền thưởng cho một milestone streak
     * 
     * @param streak Số ngày streak liên tiếp
     * @return số tiền thưởng hoặc 0 nếu không phải milestone
     */
    public int getStreakMilestoneReward(int streak) {
        String path = ConfigKey.REWARDS_STREAK_MILESTONES + "." + streak;
        return getConfig().getInt(path, 0);
    }

    /**
     * Reload cấu hình từ file
     */
    public void reloadConfig() {
        plugin.reloadConfig();
    }
}