package io.github.dode5656.rolesync.commands;

import io.github.dode5656.rolesync.RoleSync;
import io.github.dode5656.rolesync.utilities.Message;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ReloadCommand implements CommandExecutor {
    private final RoleSync plugin;

    public ReloadCommand(final RoleSync plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (commandSender instanceof Player && !commandSender.hasPermission("rolesync.reload")) {
            commandSender.sendMessage(plugin.getMessageManager().format(Message.NO_PERM_CMD));
            return true;
        }
        try {
            plugin.reloadConfig();
            if (plugin.getPluginStatus() == PluginStatus.DISABLED) plugin.setPluginStatus(PluginStatus.ENABLED);
            plugin.getConfigChecker().checkDefaults();
            plugin.startBot();
            plugin.getCommand("sync").setExecutor(new SyncCommand(plugin));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to reload config" + e);
            commandSender.sendMessage(plugin.getMessageManager().format(Message.CONFIG_RELOAD_ERROR));
            return true;
        }
        commandSender.sendMessage(plugin.getMessageManager().format(Message.CONFIG_RELOADED));
        return true;
    }
}
