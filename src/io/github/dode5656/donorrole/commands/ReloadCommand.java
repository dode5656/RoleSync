package io.github.dode5656.donorrole.commands;

import io.github.dode5656.donorrole.utilities.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import io.github.dode5656.donorrole.DonorRole;
import org.bukkit.command.CommandExecutor;

import java.util.logging.Level;

public class ReloadCommand implements CommandExecutor
{
    private DonorRole plugin;
    
    public ReloadCommand(final DonorRole plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        try {
            plugin.reloadConfig();
        }
        catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to reload config" + e);
            commandSender.sendMessage(plugin.getMessageManager().format(Message.CONFIGRELOADERROR));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return true;
        }
        commandSender.sendMessage(plugin.getMessageManager().format(Message.CONFIGRELOADED));
        return true;
    }
}
