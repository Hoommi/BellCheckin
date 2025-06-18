package org.hommi.bellCheckin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.hommi.bellCheckin.events.BellInteractEventListener;
import org.hommi.bellCheckin.events.CheckinPlayerListener;
import org.hommi.bellCheckin.events.BellSetLocationListener;
import org.hommi.bellCheckin.events.BellBreakListener;

public class EventManager {

    public static void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BellInteractEventListener(), BellCheckin.getInstance());
        pm.registerEvents(new CheckinPlayerListener(), BellCheckin.getInstance());
        pm.registerEvents(new BellSetLocationListener(), BellCheckin.getInstance());
        pm.registerEvents(new BellBreakListener(), BellCheckin.getInstance());

    }
}
