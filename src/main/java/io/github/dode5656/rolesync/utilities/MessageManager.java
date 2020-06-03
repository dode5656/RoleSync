package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public final class MessageManager {
    private final RoleSync plugin;

    public MessageManager(RoleSync plugin) {
        this.plugin = plugin;
    }

    public final String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public final String usage(Command cmd) {
        return color(plugin.getConfig().getString(Message.PREFIX.getMessage())) + cmd.getUsage();
    }

    public final String format(String msg) {
        return color(plugin.getConfig().getString(Message.PREFIX.getMessage())) + color(msg);
    }

    public final String format(Message msg) { return format(plugin.getMessages().read().getString(msg.getMessage())); }

    public final String formatDiscord(Message msg) { return plugin.getMessages().read().getString(msg.getMessage()); }

    public final String replacePlaceholders(String msg, String discordTag, String playerName, String guildName) {
        return color(msg
                .replaceAll("\\{discord_tag}", discordTag)
                .replaceAll("\\{player_name}", playerName)
                .replaceAll("\\{discord_server_name}", guildName));
    }

    public final String defaultError(String value) {
        return plugin.getMessages().read().getString(Message.DEFAULT_VALUE.getMessage()).replaceAll("\\{value}", value);
    }
}
