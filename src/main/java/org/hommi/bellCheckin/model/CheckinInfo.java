package org.hommi.bellCheckin.model;

import java.util.UUID;

public class CheckinInfo {
    private final UUID uuid;
    private long lastCheckin;
    private int streak;
    private int lastStreakReward;

    public CheckinInfo(UUID uuid, long lastCheckin) {
        this.uuid = uuid;
        this.lastCheckin = lastCheckin;
        this.streak = 0;
        this.lastStreakReward = 0;
    }

    public CheckinInfo(UUID uuid, long lastCheckin, int streak, int lastStreakReward) {
        this.uuid = uuid;
        this.lastCheckin = lastCheckin;
        this.streak = streak;
        this.lastStreakReward = lastStreakReward;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getLastCheckin() {
        return lastCheckin;
    }

    public void setLastCheckin(long lastCheckin) {
        this.lastCheckin = lastCheckin;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getLastStreakReward() {
        return lastStreakReward;
    }

    public void setLastStreakReward(int lastStreakReward) {
        this.lastStreakReward = lastStreakReward;
    }
}
