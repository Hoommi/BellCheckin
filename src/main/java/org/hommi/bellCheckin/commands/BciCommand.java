package org.hommi.bellCheckin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

        if (label.equalsIgnoreCase("bci")) {
            if (args.length == 0) {
                sender.sendMessage("§eUsage: /bci add");
                return true;
            }
        }
        else {
            return false; // If the command is not /bci, ignore it
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!player.isOp() && !player.hasPermission("bellcheckin.add")) {
            player.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            addModePlayers.add(player.getUniqueId());
            player.sendMessage("§aChuột trái vào chuông để set điểm checkin.");
            return true;
        }
        player.sendMessage("§eUsage: /bci add");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("add");
        }
        return Collections.emptyList();
    }
}
