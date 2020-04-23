package io.github.dode5656.rolesync.events;

import fr.xephi.authme.events.RegisterEvent;
import io.github.dode5656.rolesync.RoleSync;
import io.github.dode5656.rolesync.utilities.Message;
import io.github.dode5656.rolesync.utilities.MessageManager;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class AuthMeRegisterEvent implements Listener {

    private final RoleSync plugin;

    public AuthMeRegisterEvent(RoleSync plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegister(RegisterEvent e) {
        if (plugin.getPluginStatus() == PluginStatus.DISABLED) return;
        JDA jda = plugin.getJDA();
        Player player = e.getPlayer();
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

            Member member = guild.getMemberById(playerCache.getString("verified." + player.getUniqueId().toString()));

            if (member == null) return;

            List<Role> memberRoles = member.getRoles();

            Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
            Collection<Role> added = new ArrayList<>();
            Collection<Role> removed = new ArrayList<>();
            for (Map.Entry<String, Object> entry : roles.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Role role = guild.getRoleById((String) value);
                if (role == null) continue;
                if (player.hasPermission("rolesync.role." + key) && !memberRoles.contains(guild.getRoleById((String) value))) {
                    added.add(role);
                } else if (!player.hasPermission("rolesync.role." + key) && memberRoles.contains(guild.getRoleById((String) value))) {
                    removed.add(role);
                }

            }

            if (added.isEmpty() && removed.isEmpty()) return;

            guild.modifyMemberRoles(member, added, removed).queue();

            player.sendMessage(messageManager.format(Message.UPDATED_ROLES));
        }
    }

}
