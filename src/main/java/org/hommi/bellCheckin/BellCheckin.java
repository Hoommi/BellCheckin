package org.hommi.bellCheckin;

import me.TechsCode.UltraEconomy.UltraEconomyAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.hommi.bellCheckin.manager.ConfigManager;
import org.hommi.bellCheckin.manager.LanguageManager;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.io.File;

public final class BellCheckin extends JavaPlugin {

    private static BellCheckin instance;
    public UltraEconomyAPI ultraEconomy;
    private CheckinManager checkinManager;
    private BellLocationManager bellLocationManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private File langFile;
    private YamlConfiguration langConfig;

    public static BellCheckin getInstance() {
        return instance;
    }

    private void createDataFolder() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        createDataFolder();

        // Khởi tạo các manager
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);

        // Tải cấu hình và ngôn ngữ
        saveDefaultConfig();
        saveResource("language.yml", false);
        reloadConfig();
        loadLanguageFile();

        // Thiết lập database
        setupDatabase();

        // Khởi tạo các manager khác
        this.checkinManager = new CheckinManager();
        this.bellLocationManager = new BellLocationManager();
        this.bellLocationManager.loadFromDb();

        // Đăng ký sự kiện, hook và lệnh
        EventManager.registerEvents();
        HookManager.loadHooks();
        CommandManager.registerCommands();

        getLogger().info("BellCheckin đã được kích hoạt thành công!");
    }

    private void setupDatabase() {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            sqLiteManager.createCheckinTable();
            sqLiteManager.createBellLocationTable();
        } catch (Exception e) {
            getLogger().severe("Không thể thiết lập database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLanguageFile() {
        langFile = new File(getDataFolder(), "language.yml");
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    @Override
    public void onDisable() {
        // Lưu dữ liệu trước khi tắt plugin
        if (checkinManager != null) {
            checkinManager.saveAllToDb();
        }

        if (bellLocationManager != null) {
            bellLocationManager.saveAllToDb();
        }

        instance = null;
        getLogger().info("BellCheckin đã được tắt!");
    }

    public CheckinManager getCheckinManager() {
        return checkinManager;
    }

    public BellLocationManager getBellLocationManager() {
        return bellLocationManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    // Tải giá trị từ config.yml
    public Object getRewardConfig(String path) {
        return getConfig().get(path);
    }

    // Tải thông báo từ language.yml
    public String getMessage(String path) {
        return langConfig.getString("messages." + path, "Message not found: " + path);
    }

    // Reload cấu hình và ngôn ngữ
    public void reloadAllConfigs() {
        reloadConfig();
        loadLanguageFile();
    }
}
