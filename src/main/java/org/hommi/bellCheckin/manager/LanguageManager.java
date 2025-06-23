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
     * Chuyển đổi mã màu Minecraft từ & sang §
     * 
     * @param text văn bản cần chuyển đổi
     * @return văn bản đã chuyển đổi
     */
    public String colorize(String text) {
        if (text == null)
            return "";
        return text.replace('&', '§');
    }

    /**
     * Lấy thông báo chỉ dành cho người chơi
     * 
     * @return thông báo
     */
    public String getPlayerOnlyMessage() {
        return colorize(getMessage(LangKey.PLAYER_ONLY));
    }

    /**
     * Lấy thông báo không có chuông nào
     * 
     * @return thông báo
     */
    public String getNoBellsMessage() {
        return colorize(getMessage(LangKey.NO_BELLS));
    }

    /**
     * Lấy tiêu đề danh sách chuông
     * 
     * @return thông báo
     */
    public String getBellListTitleMessage() {
        return colorize(getMessage(LangKey.BELL_LIST_TITLE));
    }

    /**
     * Lấy định dạng hiển thị chuông
     * 
     * @param index số thứ tự
     * @param world tên thế giới
     * @param x     tọa độ x
     * @param y     tọa độ y
     * @param z     tọa độ z
     * @return thông báo đã thay thế placeholder
     */
    public String getBellListFormatMessage(int index, String world, int x, int y, int z) {
        return colorize(getMessage(LangKey.BELL_LIST_FORMAT)
                .replace("{index}", String.valueOf(index))
                .replace("{world}", world)
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z)));
    }

    /**
     * Lấy nút xóa chuông
     * 
     * @return thông báo
     */
    public String getBellDeleteButtonMessage() {
        return colorize(getMessage(LangKey.BELL_DELETE_BUTTON));
    }

    /**
     * Lấy thông báo hover khi di chuột qua nút xóa
     * 
     * @return thông báo
     */
    public String getBellDeleteHoverMessage() {
        return colorize(getMessage(LangKey.BELL_DELETE_HOVER));
    }

    /**
     * Lấy thông báo cách sử dụng lệnh remove
     * 
     * @return thông báo
     */
    public String getRemoveUsageMessage() {
        return colorize(getMessage(LangKey.REMOVE_USAGE));
    }

    /**
     * Lấy thông báo xóa chuông thành công
     * 
     * @param world tên thế giới
     * @param x     tọa độ x
     * @param y     tọa độ y
     * @param z     tọa độ z
     * @return thông báo đã thay thế placeholder
     */
    public String getRemoveSuccessMessage(String world, int x, int y, int z) {
        return colorize(getMessage(LangKey.REMOVE_SUCCESS)
                .replace("{world}", world)
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z)));
    }

    /**
     * Lấy thông báo không tìm thấy chuông
     * 
     * @return thông báo
     */
    public String getRemoveNotFoundMessage() {
        return colorize(getMessage(LangKey.REMOVE_NOT_FOUND));
    }

    /**
     * Lấy thông báo tọa độ không hợp lệ
     * 
     * @return thông báo
     */
    public String getRemoveInvalidCoordsMessage() {
        return colorize(getMessage(LangKey.REMOVE_INVALID_COORDS));
    }

    /**
     * Lấy tiêu đề thông tin phiên bản
     * 
     * @return thông báo
     */
    public String getVersionTitleMessage() {
        return colorize(getMessage(LangKey.VERSION_TITLE));
    }

    /**
     * Lấy thông báo phiên bản plugin
     * 
     * @param version phiên bản plugin
     * @return thông báo đã thay thế placeholder
     */
    public String getVersionPluginMessage(String version) {
        return colorize(getMessage(LangKey.VERSION_PLUGIN).replace("{version}", version));
    }

    /**
     * Lấy thông báo phiên bản database
     * 
     * @param version phiên bản database
     * @return thông báo đã thay thế placeholder
     */
    public String getVersionDatabaseMessage(String version) {
        return colorize(getMessage(LangKey.VERSION_DATABASE).replace("{version}", version));
    }

    /**
     * Lấy tiêu đề trợ giúp
     * 
     * @return thông báo
     */
    public String getHelpTitleMessage() {
        return colorize(getMessage(LangKey.HELP_TITLE));
    }

    /**
     * Lấy trợ giúp lệnh add
     * 
     * @return thông báo
     */
    public String getHelpAddMessage() {
        return colorize(getMessage(LangKey.HELP_ADD));
    }

    /**
     * Lấy trợ giúp lệnh list
     * 
     * @return thông báo
     */
    public String getHelpListMessage() {
        return colorize(getMessage(LangKey.HELP_LIST));
    }

    /**
     * Lấy trợ giúp lệnh remove
     * 
     * @return thông báo
     */
    public String getHelpRemoveMessage() {
        return colorize(getMessage(LangKey.HELP_REMOVE));
    }

    /**
     * Lấy trợ giúp lệnh reload
     * 
     * @return thông báo
     */
    public String getHelpReloadMessage() {
        return colorize(getMessage(LangKey.HELP_RELOAD));
    }

    /**
     * Lấy trợ giúp lệnh version
     * 
     * @return thông báo
     */
    public String getHelpVersionMessage() {
        return colorize(getMessage(LangKey.HELP_VERSION));
    }

    /**
     * Reload file ngôn ngữ
     */
    public void reloadLanguage() {
        loadLanguageFile();
    }
}