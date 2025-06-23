package org.hommi.bellCheckin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.manager.LanguageManager;
import org.hommi.bellCheckin.sqlite.SQLiteManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BciCommand implements CommandExecutor, TabCompleter {

    // Theo dõi người chơi trong chế độ thêm chuông
    public static final Set<UUID> addModePlayers = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String[] args) {

        if (!label.equalsIgnoreCase("bci")) {
            return false; // Nếu lệnh không phải /bci, bỏ qua
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();

        // Xử lý các lệnh con
        switch (args[0].toLowerCase()) {
            case "add":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Lệnh này chỉ có thể được sử dụng bởi người chơi.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                // Kiểm tra quyền
                if (!player.isOp() && !player.hasPermission("bellcheckin.admin")) {
                    player.sendMessage(Component.text(langManager.getNoPermissionMessage())
                            .color(NamedTextColor.RED));
                    return true;
                }

                handleAddCommand(player, langManager);
                break;

            case "reload":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage())
                            .color(NamedTextColor.RED));
                    return true;
                }

                handleReloadCommand(sender);
                break;

            case "version":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage())
                            .color(NamedTextColor.RED));
                    return true;
                }

                handleVersionCommand(sender);
                break;

            case "db":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage())
                            .color(NamedTextColor.RED));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(Component.text("Sử dụng: /bci db <update|rollback> [version]")
                            .color(NamedTextColor.YELLOW));
                    return true;
                }

                handleDbCommand(sender, args);
                break;

            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void handleAddCommand(Player player, LanguageManager langManager) {
        addModePlayers.add(player.getUniqueId());
        player.sendMessage(Component.text(langManager.getSetLocationMessage())
                .color(NamedTextColor.GREEN));
    }

    private void handleReloadCommand(CommandSender sender) {
        BellCheckin.getInstance().reloadAllConfigs();
        sender.sendMessage(Component.text("Đã tải lại cấu hình plugin!")
                .color(NamedTextColor.GREEN));
    }

    private void handleVersionCommand(CommandSender sender) {
        String pluginVersion = BellCheckin.getInstance().getPluginVersion();
        sender.sendMessage(Component.text("=== BellCheckin Thông tin phiên bản ===")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Phiên bản plugin: ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(pluginVersion)
                        .color(NamedTextColor.GREEN)));

        // Lấy thông tin phiên bản database
        try (SQLiteManager sqliteManager = new SQLiteManager()) {
            sqliteManager.connect();
            String currentVersion = sqliteManager.getDatabaseVersion();

            sender.sendMessage(Component.text("Phiên bản database: ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(currentVersion)
                            .color(NamedTextColor.GREEN)));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Lỗi khi kiểm tra phiên bản database: " + e.getMessage())
                    .color(NamedTextColor.RED));
            BellCheckin.getInstance().getLogger().severe("Lỗi khi kiểm tra phiên bản database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDbCommand(CommandSender sender, String[] args) {
        String operation = args[1].toLowerCase();
        String version = args.length > 2 ? args[2] : null;

        try (SQLiteManager sqliteManager = new SQLiteManager()) {
            sqliteManager.connect();

            switch (operation) {
                case "update":
                    if (version == null) {
                        sqliteManager.initializeDatabase();
                        sender.sendMessage(Component.text("Đã cập nhật database lên phiên bản mới nhất!")
                                .color(NamedTextColor.GREEN));
                    } else {
                        sqliteManager.updateDatabaseToVersion(version);
                        sender.sendMessage(Component.text("Đã cập nhật database lên phiên bản " + version + "!")
                                .color(NamedTextColor.GREEN));
                    }
                    break;

                case "rollback":
                    if (version == null) {
                        sender.sendMessage(Component.text("Vui lòng chỉ định phiên bản để rollback!")
                                .color(NamedTextColor.RED));
                    } else {
                        sqliteManager.rollbackDatabaseToVersion(version);
                        sender.sendMessage(Component.text("Đã rollback database về phiên bản " + version + "!")
                                .color(NamedTextColor.GREEN));
                    }
                    break;

                default:
                    sender.sendMessage(
                            Component.text("Thao tác không hợp lệ. Sử dụng: /bci db <update|rollback> [version]")
                                    .color(NamedTextColor.RED));
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage(Component.text("Lỗi khi thực hiện thao tác database: " + e.getMessage())
                    .color(NamedTextColor.RED));
            BellCheckin.getInstance().getLogger().severe("Lỗi khi thực hiện thao tác database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== BellCheckin Commands ===")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/bci add")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" - Thêm chuông điểm danh mới")
                        .color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/bci reload")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" - Tải lại cấu hình plugin")
                        .color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/bci version")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" - Hiển thị thông tin phiên bản")
                        .color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/bci db <update|rollback> [version]")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" - Quản lý phiên bản database")
                        .color(NamedTextColor.WHITE)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("add");
            completions.add("reload");
            completions.add("version");
            completions.add("db");

            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase("db")) {
            List<String> completions = new ArrayList<>();
            completions.add("update");
            completions.add("rollback");

            return completions.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
