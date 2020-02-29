package io.github.dode5656.donorrole.events;

import io.github.dode5656.donorrole.DonorRole;
import io.github.dode5656.donorrole.utilities.Message;
import io.github.dode5656.donorrole.utilities.MessageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinEvent implements Listener {
    private DonorRole plugin;

    public JoinEvent(DonorRole plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        JDA jda = plugin.getJDA();
        Player player = e.getPlayer();
        FileConfiguration playerCache = plugin.getPlayerCache().read();
        MessageManager messageManager = plugin.getMessageManager();

        if (playerCache == null) return;
        if (playerCache.contains("verified." + player.getUniqueId().toString())) {
            Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

            if (guild == null) {

                player.sendMessage(messageManager.format(Message.ERROR));
                plugin.getLogger().severe(Message.INVALIDSERVERID.getMessage());

                return;

            }

            Member member = guild.getMemberById(playerCache.getString("verified." + player.getUniqueId().toString()));

            if (member == null) return;

            List<Role> memberRoles = member.getRoles();

            Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
            List<String> roleIDs = new ArrayList<>();
            for (Map.Entry<String, Object> entry : roles.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (player.hasPermission("donorrole.role." + key) && !memberRoles.contains(guild.getRoleById((String) value))) {
                    roleIDs.add((String) value);
                }
            }

            if (roleIDs.isEmpty()) return;

            for (String roleID : roleIDs) {
                Role role = guild.getRoleById(roleID);
                if (role == null) {
                    continue;
                }
                guild.addRoleToMember(member, role).queue();
            }

            player.sendMessage(messageManager.format(Message.UPDATEDROLES));
        }

    }

}
