package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class Util {
    private final RoleSync plugin;
    public Util(RoleSync plugin) { this.plugin = plugin; }

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
                            member.getUser().getAsTag(),player.getName(), guild.getName()));
            return false;
        }
        return true;
    }

    public boolean changeNickname(Guild guild, Member member, Player player) {
        return changeNickname(guild,member,player,plugin.getConfig().getString("nickname-format")
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
                            member.getUser().getAsTag(),player.getName(), guild.getName()));
            return false;
        }
        return true;
    }

    public void populateAddedRemoved(Guild guild, Map<String, Object> roles, Player player, List<Role> memberRoles, Collection<Role> added, Collection<Role> removed) {
        for (Map.Entry<String, Object> entry : roles.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            Role role = guild.getRoleById(value);
            if (role == null) continue;
            if (player.hasPermission("rolesync.role." + key) && !memberRoles.contains(role)) {
                added.add(role);
            } else if (removed != null && !player.hasPermission("rolesync.role." + key) && memberRoles.contains(role)) {
                removed.add(role);
            }

        }
    }

    public void joinEvent(Player player) {
        if (plugin.getPluginStatus() == PluginStatus.DISABLED) return;
        JDA jda = plugin.getJDA();
        FileConfiguration playerCache = plugin.getPlayerCache().read();
        MessageManager messageManager = plugin.getMessageManager();

        if (playerCache == null) return;
        if (playerCache.contains("verified." + player.getUniqueId().toString())) {
            Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

            if (guild == null) {
                player.sendMessage(messageManager.format(Message.ERROR));
                plugin.getLogger().severe(Message.INVALID_SERVER_ID.getMessage());
                return;
            }

            Member member = guild.retrieveMemberById(playerCache.getString("verified." + player.getUniqueId().toString())).complete();
            if (member == null) return;
            List<Role> memberRoles = member.getRoles();

            Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
            Collection<Role> added = new ArrayList<>();
            Collection<Role> removed = new ArrayList<>();
            populateAddedRemoved(guild,roles,player,memberRoles,added,removed);
            boolean changed = false;
            if (!added.isEmpty() || !removed.isEmpty()) {
                if (!modifyMemberRoles(guild, member, added, removed, player)) return;
                changed = true;
            }

            String nickname = this.plugin.getConfig().getString("nickname-format").replaceAll("\\{ign}", player.getName());
            if (this.plugin.getConfig().getBoolean("change-nickname") && (member.getNickname() == null || !member.getNickname().equals(nickname))) {
                if (!changeNickname(guild, member, player)) return;
                changed = true;
            }

            if (changed) player.sendMessage(messageManager.format(Message.UPDATED_ROLES));
        }
    }

}
