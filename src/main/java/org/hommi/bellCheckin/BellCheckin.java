package org.hommi.bellCheckin;

import me.TechsCode.UltraEconomy.UltraEconomyAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

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
        // Load config and language files
        saveDefaultConfig(); // Loads config.yml if not present
        saveResource("language.yml", false); // Loads language.yml if not present
        reloadConfig();
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

    // Load a value from config.yml
    public Object getRewardConfig(String path) {
        return getConfig().get(path);
    }

    // Load a message from language.yml
    public String getMessage(String path) {
        // Load language.yml as a YamlConfiguration
        java.io.File langFile = new java.io.File(getDataFolder(), "language.yml");
        org.bukkit.configuration.file.YamlConfiguration langConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(langFile);
        return langConfig.getString("messages." + path, "Message not found: " + path);
    }
}
