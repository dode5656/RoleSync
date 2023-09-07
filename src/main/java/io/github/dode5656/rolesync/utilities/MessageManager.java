package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageManager {
    private final RoleSync plugin;

    public MessageManager(RoleSync plugin) {
        this.plugin = plugin;
    }

    public final String color(String message) {

        if (Integer.parseInt(plugin.getServer().getVersion().split("\\.")[1].replaceAll("\\)","")) >= 16) { // Check if 1.16+
            return ChatColor.translateAlternateColorCodes('&', convertHexToColor(message));
        }

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
        String temp = msg
                .replaceAll("\\{discord_tag}", Matcher.quoteReplacement(discordTag))
                .replaceAll("\\{player_name}", Matcher.quoteReplacement(playerName))
                .replaceAll("\\{discord_server_name}", Matcher.quoteReplacement(guildName));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return color(PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(playerName), temp));
        }
        return color(temp);
    }

    public final String replaceDiscordPlaceholders(String msg, String discordTag, String playerName, String guildName) {
        String temp = msg
                .replaceAll("\\{discord_tag}", Matcher.quoteReplacement(discordTag))
                .replaceAll("\\{player_name}", Matcher.quoteReplacement(playerName))
                .replaceAll("\\{discord_server_name}", Matcher.quoteReplacement(guildName));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(playerName), temp);
        }
        return temp;
    }

    public final String defaultError(String value) {
        return plugin.getMessages().read().getString(Message.DEFAULT_VALUE.getMessage()).replaceAll("\\{value}", value);
    }

    String convertHexToColor(String msg) {
        Pattern p = Pattern.compile("&x[a-f0-9A-F]{6}");
        Matcher m = p.matcher(msg);
        String s = msg;
        while (m.find()) {
            String hexString = net.md_5.bungee.api.ChatColor.of('#' + m.group().substring(2)).toString();
            s = s.replace(m.group(), hexString);
        }
        return s;
    }

}
