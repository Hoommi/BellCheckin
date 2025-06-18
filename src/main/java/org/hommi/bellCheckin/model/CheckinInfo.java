package org.hommi.bellCheckin.model;

import java.util.UUID;

public class CheckinInfo {
    private final UUID uuid;
    private long lastCheckin;

    public CheckinInfo(UUID uuid, long lastCheckin) {
        this.uuid = uuid;
        this.lastCheckin = lastCheckin;
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
}

