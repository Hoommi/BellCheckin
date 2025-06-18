package org.hommi.bellCheckin;

import org.bukkit.command.PluginCommand;
import org.hommi.bellCheckin.commands.BciCommand;

public class CommandManager {

    public static void registerCommands() {
        BellCheckin plugin = BellCheckin.getInstance();
        PluginCommand bciCommand = plugin.getCommand("bci");
        if (bciCommand != null) {
            BciCommand executor = new BciCommand();
            bciCommand.setExecutor(executor);
            bciCommand.setTabCompleter(executor);
        }
    }
}

