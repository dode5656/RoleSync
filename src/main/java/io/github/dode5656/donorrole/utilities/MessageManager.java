package io.github.dode5656.donorrole.utilities;

import io.github.dode5656.donorrole.DonorRole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {
    private String prefix;
    private FileConfiguration messages;

    public MessageManager(DonorRole plugin) {
        messages = plugin.getMessages().read();
        prefix = color(plugin.getConfig().getString(Message.PREFIX.getMessage()) + " ");
    }

    public final String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public final String usage(Command cmd) {
        return prefix + cmd.getUsage();
    }

    public final String format(String msg) {
        return prefix + color(msg);
    }

    public final String format(Message msg) {
        return prefix + color(this.messages.getString(msg.getMessage()));
    }

    public final String formatDiscord(Message msg) { return this.messages.getString(msg.getMessage()); }

    public final String replacePlaceholders(String msg, String discordTag, String playerName, String guildName) {
        return color(msg
                .replaceAll("\\{discord_tag}", discordTag)
                .replaceAll("\\{player_name}", playerName)
                .replaceAll("\\{discord_server_name}", guildName));
    }

    public final String defaultError(String value) {
        return this.messages.getString(Message.DEFAULTVALUE.getMessage()).replaceAll("\\{value}", value);
    }
}
