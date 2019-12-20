package io.github.dode5656.donorrole.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import io.github.dode5656.donorrole.DonorRole;
import org.bukkit.command.CommandExecutor;

public class ReloadCommand implements CommandExecutor
{
    private DonorRole plugin;
    
    public ReloadCommand(final DonorRole plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        try {
            this.plugin.reloadConfig();
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Error while trying to reload config" + e);
            commandSender.sendMessage(this.plugin.color(this.plugin.prefix + "Error while trying to reload config, error in the console."));
            return true;
        }
        commandSender.sendMessage(this.plugin.color(this.plugin.prefix + "Config file reloaded successfully!"));
        return true;
    }
}
