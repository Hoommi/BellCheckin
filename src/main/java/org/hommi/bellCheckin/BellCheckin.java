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
import java.util.HashMap;
import java.util.Map;

public final class BellCheckin extends JavaPlugin {

    private static BellCheckin instance;
    public UltraEconomyAPI ultraEconomy;
    private CheckinManager checkinManager;
    private BellLocationManager bellLocationManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private File langFile;
    private YamlConfiguration langConfig;
    private String pluginVersion;

    // Định nghĩa các phiên bản database tương ứng với phiên bản plugin
    private static final Map<String, String> PLUGIN_DB_VERSION_MAP = new HashMap<>();

    static {
        // Định nghĩa mapping giữa phiên bản plugin và phiên bản database
        PLUGIN_DB_VERSION_MAP.put("1.0.0", "1.0"); // Phiên bản cơ bản
        PLUGIN_DB_VERSION_MAP.put("1.1.0", "2.0"); // Phiên bản với streak
        PLUGIN_DB_VERSION_MAP.put("1.2.0", "3.0"); // Phiên bản với các tính năng mới trong tương lai
    }

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

        // Lưu phiên bản plugin
        pluginVersion = this.getVersion();
        getLogger().info("Đang khởi động BellCheckin phiên bản " + pluginVersion);

        // Khởi tạo các manager
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);

        // Tải cấu hình và ngôn ngữ
        saveDefaultConfig();
        saveResource("language.yml", false);
        reloadConfig();
        loadLanguageFile();

        // Thiết lập database với quản lý phiên bản tự động theo phiên bản plugin
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

    private String getVersion() {
        return "1.0.0";
    }

    private void setupDatabase() {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            // Lấy phiên bản database tương ứng với phiên bản plugin hiện tại
            String targetDbVersion = getTargetDatabaseVersion();

            // Khởi tạo và cập nhật database theo phiên bản plugin
            getLogger().info("Đang cập nhật database lên phiên bản " + targetDbVersion + " (tương ứng với plugin v"
                    + pluginVersion + ")");
            sqLiteManager.initializeDatabaseForPluginVersion(targetDbVersion);
        } catch (Exception e) {
            getLogger().severe("Không thể thiết lập database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy phiên bản database tương ứng với phiên bản plugin hiện tại
     * 
     * @return Phiên bản database mục tiêu
     */
    public String getTargetDatabaseVersion() {
        // Nếu không tìm thấy mapping, sử dụng phiên bản mặc định
        return PLUGIN_DB_VERSION_MAP.getOrDefault(pluginVersion, "1.0");
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

    /**
     * Lấy phiên bản hiện tại của plugin
     * 
     * @return Chuỗi phiên bản
     */
    public String getPluginVersion() {
        return pluginVersion;
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
