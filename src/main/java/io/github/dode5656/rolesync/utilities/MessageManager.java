package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessageManager {
    private final RoleSync plugin;
    private final FileConfiguration messages;

    public MessageManager(RoleSync plugin) {
        messages = plugin.getMessages().read();
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

    public final String format(Message msg) { return format(this.messages.getString(msg.getMessage())); }

    public final String formatDiscord(Message msg) { return this.messages.getString(msg.getMessage()); }

    public final String replacePlaceholders(String msg, String discordTag, String playerName, String guildName) {
        return color(msg
                .replaceAll("\\{discord_tag}", discordTag)
                .replaceAll("\\{player_name}", playerName)
                .replaceAll("\\{discord_server_name}", guildName));
    }

    public final String defaultError(String value) {
        return this.messages.getString(Message.DEFAULT_VALUE.getMessage()).replaceAll("\\{value}", value);
    }
}
