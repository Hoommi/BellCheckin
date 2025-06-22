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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BciCommand implements CommandExecutor, TabCompleter {

    // Track players in add mode
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

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Lệnh này chỉ có thể được sử dụng bởi người chơi.")
                    .color(NamedTextColor.RED));
            return true;
        }

        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();

        // Kiểm tra quyền
        if (!player.isOp() && !player.hasPermission("bellcheckin.admin")) {
            player.sendMessage(Component.text(langManager.getNoPermissionMessage())
                    .color(NamedTextColor.RED));
            return true;
        }

        // Xử lý các lệnh con
        switch (args[0].toLowerCase()) {
            case "add":
                handleAddCommand(player, langManager);
                break;

            case "reload":
                handleReloadCommand(player);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void handleAddCommand(Player player, LanguageManager langManager) {
        addModePlayers.add(player.getUniqueId());
        player.sendMessage(Component.text(langManager.getSetLocationMessage())
                .color(NamedTextColor.GREEN));
    }

    private void handleReloadCommand(Player player) {
        BellCheckin.getInstance().reloadAllConfigs();
        player.sendMessage(Component.text("Đã tải lại cấu hình plugin!")
                .color(NamedTextColor.GREEN));
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

            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
