package org.hommi.bellCheckin;

import me.TechsCode.UltraEconomy.UltraEconomyAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.sql.SQLException;

public final class BellCheckin extends JavaPlugin {

    private static BellCheckin instance;
    public UltraEconomyAPI ultraEconomy;
    private CheckinManager checkinManager;
    private BellLocationManager bellLocationManager;

    public static BellCheckin getInstance() {
        return instance;
    }

    private void createDataFolder() {
        if (!this.getDataFolder()
                .exists()) {
            this.getDataFolder()
                    .mkdirs();
        }
    }

    @Override
    public void onEnable() {
        BellCheckin.instance = this;
        createDataFolder();
        setUpDb();
        this.checkinManager = new CheckinManager();
        this.bellLocationManager = new BellLocationManager();
        this.bellLocationManager.loadFromDb();
        EventManager.registerEvents();
        HookManager.loadHooks();
        CommandManager.registerCommands();
    }

    private void setUpDb() {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.createCheckinTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        BellCheckin.instance = null;
    }

    public CheckinManager getCheckinManager() {
        return checkinManager;
    }

    public BellLocationManager getBellLocationManager() {
        return bellLocationManager;
    }
}
