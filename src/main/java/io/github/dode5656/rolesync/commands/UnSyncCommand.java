package io.github.dode5656.rolesync.commands;

import io.github.dode5656.rolesync.RoleSync;
import io.github.dode5656.rolesync.utilities.Message;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

public final class UnSyncCommand implements CommandExecutor {

    private final RoleSync plugin;
    private JDA jda;

    public UnSyncCommand(RoleSync plugin) {
        this.plugin = plugin;
        if (plugin.getPluginStatus() == PluginStatus.ENABLED) {
            this.jda = plugin.getJDA();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rolesync.unsync")) {
            sender.sendMessage(plugin.getMessageManager().format(Message.NO_PERM_CMD));
            return true;
        }

        if (sender.hasPermission("rolesync.unsync.others")) {
            if (args.length < 1) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(plugin.getMessageManager().format(Message.PLAYER_ONLY));
                    return true;
                }
                unsync((Player) sender, sender);
                return true;
            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(plugin.getMessageManager().format(Message.PLAYER_NOT_FOUND));
                return true;
            }

            unsync(player, sender);
        } else {
            unsync((Player) sender, sender);
        }

        return true;
    }

    private void unsync(Player player, CommandSender sender) {
        if (!(plugin.getPlayerCache().read() != null && plugin.getPlayerCache().read().contains("verified." + player.getUniqueId().toString()))) {
            sender.sendMessage(plugin.getMessageManager().format(Message.NOT_SYNCED));
            return;
        }

        Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

        if (guild == null) {

            sender.sendMessage(plugin.getMessageManager().format(Message.ERROR));
            plugin.getLogger().severe(plugin.getMessageManager().format(Message.INVALID_SERVER_ID));

            return;
        }

        Member member = guild.retrieveMemberById(plugin.getPlayerCache().read().getString("verified." + player.getUniqueId().toString())).complete();

        if (member == null) return;

        Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
        Collection<Role> removed = new ArrayList<>();
        for (Map.Entry<String, Object> entry : roles.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Role role = guild.getRoleById((String) value);
            if (role == null) continue;
            removed.add(role);

        }

        if (removed.isEmpty()) return;

        if(!plugin.getUtil().modifyMemberRoles(guild,member,null,removed,player)) return;
        if(!plugin.getUtil().changeNickname(guild,member,player,null)) return;

        plugin.getPlayerCache().read().set("verified."+player.getUniqueId().toString() ,null);

        plugin.getPlayerCache().save();
        plugin.getPlayerCache().reload();

        sender.sendMessage(plugin.getMessageManager().replacePlaceholders(plugin.getMessageManager().format(Message.UNSYNCED_SUCCESSFULLY),member
                .getUser().getAsTag(), sender.getName(), guild.getName()));

    }

}
