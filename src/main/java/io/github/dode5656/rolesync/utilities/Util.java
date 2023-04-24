package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;
import io.github.dode5656.rolesync.storage.FileStorage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class Util {

    private final RoleSync plugin;

    public Util(RoleSync plugin) {
        this.plugin = plugin;
    }

    public boolean modifyMemberRoles(Guild guild, Member member, Collection<Role> added, Collection<Role> removed, Player player) {
        try {
            guild.modifyMemberRoles(member, added, removed).queue();
        } catch (InsufficientPermissionException | HierarchyException e) {
            player.sendMessage(plugin.getMessageManager().format(Message.ERROR));
            if (e instanceof InsufficientPermissionException) {
                plugin.getLogger().log(Level.SEVERE, "Bot has insufficient permissions. Cannot manage roles.");
                return false;
            }
            player.sendMessage(plugin.getMessageManager().format(Message.HIERARCHY_ERROR));
            plugin.getLogger().log(Level.SEVERE,
                    plugin.getMessageManager().replacePlaceholders("Failed to apply role changes to {discord_tag} with IGN {player_name} due to Hierarchy error. Some of the specified roles are higher than the bot's role.",
                            member.getUser().getAsTag(), player.getName(), guild.getName()));
            return false;
        }
        return true;
    }

    public boolean changeNickname(Guild guild, Member member, Player player) {
        return changeNickname(guild, member, player, plugin.getConfig().getString("nickname-format")
                .replaceAll("\\{ign}", player.getName()));
    }

    public boolean changeNickname(Guild guild, Member member, Player player, String nickname) {
        try {
            member.modifyNickname(nickname).queue();
        } catch (InsufficientPermissionException | HierarchyException e) {
            player.sendMessage(plugin.getMessageManager().format(Message.ERROR));
            if (e instanceof InsufficientPermissionException) {
                plugin.getLogger().log(Level.SEVERE, "Bot has insufficient permissions. Cannot give nicknames to people.");
                return false;
            }
            player.sendMessage(plugin.getMessageManager().format(Message.HIERARCHY_ERROR));
            plugin.getLogger().log(Level.SEVERE,
                    plugin.getMessageManager().replacePlaceholders("Failed to change nickname of {discord_tag} with IGN {player_name} due to Hierarchy error. Their role is higher than the bot's role.",
                            member.getUser().getAsTag(), player.getName(), guild.getName()));
            return false;
        }
        return true;
    }

    public void populateAddedRemoved(Guild guild, Map<String, Object> roles, Player player, List<Role> memberRoles, Collection<Role> added, Collection<Role> removed) {
        boolean foundRole = false;
        for (Map.Entry<String, Object> entry : roles.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            Role role = guild.getRoleById(value);
            if (role == null) {
                continue;
            }
            if (!foundRole && player.hasPermission("rolesync.role." + key)) {
                if (!memberRoles.contains(role)) {
                    added.add(role);
                }
                foundRole = true;
            } else if (!player.hasPermission("rolesync.role." + key) && memberRoles.contains(role)) {
                removed.add(role);
            }
        }
    }

    public boolean saveDefaults(File file, FileStorage fileStorage) {
        if (!file.exists()) {
            return false;
        }

        FileConfiguration tempConfig = new YamlConfiguration();
        try {
            tempConfig.load(new File(plugin.getDataFolder().getPath(), "config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't load config.yml", e);
            return false;
        }

        if (fileStorage != null) {
            fileStorage.reload();
        } else {
            plugin.reloadConfig();
        }
        return true;
    }

    public void moveToOld(File file) {
        File oldDir = new File(plugin.getDataFolder().getPath(), "old");

        if (!oldDir.exists()) {
            oldDir.mkdirs();
        }

        file.renameTo(new File(plugin.getDataFolder().getPath() + File.separator + "old",
                "old_" + file.getName()));
    }

}
