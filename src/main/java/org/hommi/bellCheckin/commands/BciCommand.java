package org.hommi.bellCheckin.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.LangKey;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.LanguageManager;
import org.hommi.bellCheckin.model.BellLocation;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

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
                    sender.sendMessage(Component.text(langManager.getPlayerOnlyMessage()));
                    return true;
                }

                // Kiểm tra quyền
                if (!player.isOp() && !player.hasPermission("bellcheckin.admin")) {
                    player.sendMessage(Component.text(langManager.getNoPermissionMessage()));
                    return true;
                }

                handleAddCommand(player, langManager);
                break;

            case "list":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")
                        && !sender.hasPermission("bellcheckin.list")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage()));
                    return true;
                }

                handleListCommand(sender);
                break;

            case "remove":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage()));
                    return true;
                }

                if (args.length < 5) {
                    sender.sendMessage(Component.text(langManager.getRemoveUsageMessage()));
                    return true;
                }

                handleRemoveCommand(sender, args[1], args[2], args[3], args[4]);
                break;

            case "reload":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage()));
                    return true;
                }

                handleReloadCommand(sender);
                break;

            case "version":
                // Kiểm tra quyền
                if (!sender.isOp() && !sender.hasPermission("bellcheckin.admin")) {
                    sender.sendMessage(Component.text(langManager.getNoPermissionMessage()));
                    return true;
                }

                handleVersionCommand(sender);
                break;

            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void handleAddCommand(Player player, LanguageManager langManager) {
        addModePlayers.add(player.getUniqueId());
        player.sendMessage(Component.text(langManager.getSetLocationMessage()));
    }

    private void handleListCommand(CommandSender sender) {
        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();
        BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();
        List<BellLocation> bellLocations = bellLocationManager.getBellLocations();

        if (bellLocations.isEmpty()) {
            sender.sendMessage(Component.text(langManager.getNoBellsMessage()));
            return;
        }

        sender.sendMessage(Component.text(langManager.getBellListTitleMessage()));

        int index = 1;
        for (BellLocation location : bellLocations) {
            // Tạo component cho thông tin vị trí
            Component locationText = Component.text(langManager.getBellListFormatMessage(
                    index, location.worldName(), location.x(), location.y(), location.z()));

            // Nút xóa
            Component deleteButton = Component.text(langManager.getBellDeleteButtonMessage())
                    .clickEvent(ClickEvent.runCommand("/bci remove " + location.worldName() + " " + location.x() + " "
                            + location.y() + " " + location.z()))
                    .hoverEvent(HoverEvent.showText(Component.text(langManager.getBellDeleteHoverMessage())));

            // Kết hợp thông tin vị trí và nút xóa
            sender.sendMessage(locationText.append(deleteButton));
            index++;
        }
    }

    private void handleRemoveCommand(CommandSender sender, String worldName, String xStr, String yStr, String zStr) {
        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            int z = Integer.parseInt(zStr);

            BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();
            boolean removed = bellLocationManager.removeBellLocation(worldName, x, y, z);

            if (removed) {
                sender.sendMessage(Component.text(langManager.getRemoveSuccessMessage(worldName, x, y, z)));
            } else {
                sender.sendMessage(Component.text(langManager.getRemoveNotFoundMessage()));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(langManager.getRemoveInvalidCoordsMessage()));
        }
    }

    private void handleReloadCommand(CommandSender sender) {
        BellCheckin.getInstance().reloadAllConfigs();
        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();
        sender.sendMessage(Component.text(langManager.colorize(langManager.getMessage(LangKey.CONFIG_RELOADED))));
    }

    private void handleVersionCommand(CommandSender sender) {
        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();
        String pluginVersion = BellCheckin.getInstance().getPluginVersion();
        String targetVersion = BellCheckin.getInstance().getTargetDatabaseVersion();

        sender.sendMessage(Component.text(langManager.getVersionTitleMessage()));
        sender.sendMessage(Component.text(langManager.getVersionPluginMessage(pluginVersion)));
        sender.sendMessage(Component.text(langManager.getVersionDatabaseMessage(targetVersion)));
    }

    private void showHelp(CommandSender sender) {
        LanguageManager langManager = BellCheckin.getInstance().getLanguageManager();

        sender.sendMessage(Component.text(langManager.getHelpTitleMessage()));
        sender.sendMessage(Component.text(langManager.getHelpAddMessage()));
        sender.sendMessage(Component.text(langManager.getHelpListMessage()));
        sender.sendMessage(Component.text(langManager.getHelpRemoveMessage()));
        sender.sendMessage(Component.text(langManager.getHelpReloadMessage()));
        sender.sendMessage(Component.text(langManager.getHelpVersionMessage()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("add");
            completions.add("list");
            completions.add("remove");
            completions.add("reload");
            completions.add("version");

            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        // Gợi ý cho lệnh remove
        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            if (args.length == 2) {
                // Gợi ý tên thế giới
                BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();
                List<BellLocation> bellLocations = bellLocationManager.getBellLocations();

                return bellLocations.stream()
                        .map(BellLocation::worldName)
                        .distinct()
                        .filter(s -> s.startsWith(args[1]))
                        .toList();
            } else if (args.length >= 3 && args.length <= 5) {
                // Gợi ý tọa độ
                BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();
                List<BellLocation> bellLocations = bellLocationManager.getBellLocations();

                String worldName = args[1];
                List<BellLocation> filteredLocations = bellLocations.stream()
                        .filter(loc -> loc.worldName().equals(worldName))
                        .toList();

                if (args.length == 3) {
                    // Gợi ý tọa độ X
                    return filteredLocations.stream()
                            .map(loc -> String.valueOf(loc.x()))
                            .distinct()
                            .filter(s -> s.startsWith(args[2]))
                            .toList();
                } else if (args.length == 4) {
                    // Gợi ý tọa độ Y
                    String x = args[2];
                    return filteredLocations.stream()
                            .filter(loc -> String.valueOf(loc.x()).equals(x))
                            .map(loc -> String.valueOf(loc.y()))
                            .distinct()
                            .filter(s -> s.startsWith(args[3]))
                            .toList();
                } else if (args.length == 5) {
                    // Gợi ý tọa độ Z
                    String x = args[2];
                    String y = args[3];
                    return filteredLocations.stream()
                            .filter(loc -> String.valueOf(loc.x()).equals(x) && String.valueOf(loc.y()).equals(y))
                            .map(loc -> String.valueOf(loc.z()))
                            .distinct()
                            .filter(s -> s.startsWith(args[4]))
                            .toList();
                }
            }
        }

        return Collections.emptyList();
    }
}
