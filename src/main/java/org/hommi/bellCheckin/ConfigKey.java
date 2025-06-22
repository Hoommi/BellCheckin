package org.hommi.bellCheckin;

public class ConfigKey {
    // Phần thưởng hàng ngày
    public static final String REWARDS_DAILY_ENABLED = "rewards.daily.enabled";
    public static final String REWARDS_DAILY_AMOUNT = "rewards.daily.amount";
    public static final String REWARDS_DAILY_COMMAND = "rewards.daily.command";

    // Phần thưởng streak
    public static final String REWARDS_STREAK_ENABLED = "rewards.streak.enabled";
    public static final String REWARDS_STREAK_MILESTONES = "rewards.streak.milestones";
    public static final String REWARDS_STREAK_COMMAND = "rewards.streak.command";

    // Cài đặt cơ sở dữ liệu
    public static final String DATABASE_AUTO_SAVE_INTERVAL = "database.auto_save_interval";

    // Cài đặt chung
    public static final String SETTINGS_DEBUG_MODE = "settings.debug_mode";
}
