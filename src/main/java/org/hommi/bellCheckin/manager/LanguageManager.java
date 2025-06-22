package org.hommi.bellCheckin.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.LangKey;

import java.io.File;

/**
 * Quản lý ngôn ngữ của plugin
 */
public class LanguageManager {

    private final BellCheckin plugin;
    private File langFile;
    private YamlConfiguration langConfig;

    public LanguageManager(BellCheckin plugin) {
        this.plugin = plugin;
        loadLanguageFile();
    }

    /**
     * Tải file ngôn ngữ
     */
    private void loadLanguageFile() {
        langFile = new File(plugin.getDataFolder(), "language.yml");
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    /**
     * Lấy thông báo từ file ngôn ngữ
     * 
     * @param path đường dẫn đến thông báo
     * @return thông báo hoặc thông báo lỗi nếu không tìm thấy
     */
    public String getMessage(String path) {
        return langConfig.getString("messages." + path, "Message not found: " + path);
    }

    /**
     * Lấy thông báo thành công khi điểm danh
     * 
     * @return thông báo
     */
    public String getCheckinSuccessMessage() {
        return getMessage(LangKey.CHECKIN_SUCCESS);
    }

    /**
     * Lấy thông báo khi người chơi đã điểm danh rồi
     * 
     * @return thông báo
     */
    public String getCheckinAlreadyMessage() {
        return getMessage(LangKey.CHECKIN_ALREADY);
    }

    /**
     * Lấy thông báo khi người chơi nhận được phần thưởng streak
     * 
     * @param streak số ngày streak liên tiếp
     * @return thông báo đã thay thế placeholder
     */
    public String getStreakRewardMessage(int streak) {
        return getMessage(LangKey.STREAK_REWARD).replace("{streak}", String.valueOf(streak));
    }

    /**
     * Lấy thông báo khi người chơi nhận được phần thưởng hàng ngày
     * 
     * @param amount số tiền thưởng
     * @return thông báo đã thay thế placeholder
     */
    public String getDailyRewardMessage(int amount) {
        return getMessage(LangKey.DAILY_REWARD).replace("{amount}", String.valueOf(amount));
    }

    /**
     * Lấy thông báo khi người chơi không có quyền
     * 
     * @return thông báo
     */
    public String getNoPermissionMessage() {
        return getMessage(LangKey.NO_PERMISSION);
    }

    /**
     * Lấy thông báo cách sử dụng lệnh
     * 
     * @return thông báo
     */
    public String getUsageMessage() {
        return getMessage(LangKey.USAGE);
    }

    /**
     * Lấy thông báo khi thiết lập vị trí chuông
     * 
     * @return thông báo
     */
    public String getSetLocationMessage() {
        return getMessage(LangKey.SET_LOCATION);
    }

    /**
     * Reload file ngôn ngữ
     */
    public void reloadLanguage() {
        loadLanguageFile();
    }
}