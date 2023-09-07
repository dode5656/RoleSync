package io.github.dode5656.rolesync.commands;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.dode5656.rolesync.RoleSync;
import io.github.dode5656.rolesync.utilities.Message;
import io.github.dode5656.rolesync.utilities.MessageManager;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class SyncCommand implements CommandExecutor {
    private final RoleSync plugin;
    private EventWaiter waiter;
    private JDA jda;

    public SyncCommand(final RoleSync plugin) {
        this.plugin = plugin;
        if (plugin.getPluginStatus() == PluginStatus.ENABLED) {
            this.waiter = new EventWaiter();
            this.jda = plugin.getJDA();
            this.jda.addEventListener(this.waiter);
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        MessageManager messageManager = plugin.getMessageManager();
        if (plugin.getPluginStatus() == PluginStatus.DISABLED) {
            sender.sendMessage(messageManager.format(Message.PLUGIN_DISABLED));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.format(Message.PLAYER_ONLY));
            return true;
        }

        if (!sender.hasPermission("rolesync.use")) {
            sender.sendMessage(messageManager.format(Message.NO_PERM_CMD));
            return true;
        }

        final Player player = (Player) sender;

        if (args.length < 1) {
            sender.sendMessage(messageManager.format(Message.USAGE));
            return true;
        }

        final Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

        if (guild == null) {
            sender.sendMessage(messageManager.format(Message.ERROR));
            plugin.getLogger().severe(messageManager.format(Message.INVALID_SERVER_ID));
            return true;
        }

        Member member = null;
        try {
            member = guild.getMemberByTag(args[0]);
        } catch (Exception ignored) {
        }

        if (member == null) {
            boolean result = false;
            if (args[0].equals("id")) {
                Member idMember = guild.retrieveMemberById(args[1]).complete();
                if (idMember != null) {
                    member = idMember;
                    result = true;
                }
            }
            if (!result) {
                sender.sendMessage(messageManager.replacePlaceholders(messageManager.format(Message.BAD_NAME),
                        args[0], sender.getName(), guild.getName()));
                return true;
            }
        }

        if (plugin.getPlayerCache().read() != null && plugin.getPlayerCache().read().contains("verified." + player.getUniqueId().toString())) {
            List<Role> memberRoles = member.getRoles();
            if (!plugin.getPlayerCache().read().getString("verified." + player.getUniqueId().toString()).equals(member.getId())) {
                player.sendMessage(messageManager.replacePlaceholders(messageManager
                        .format(Message.ALREADY_VERIFIED), member
                        .getUser().getAsTag(), sender.getName(), guild.getName()));
                return true;
            }
            Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
            Collection<Role> added = new ArrayList<>();
            Collection<Role> removed = new ArrayList<>();
            plugin.getUtil().populateAddedRemoved(guild,roles,player,memberRoles,added,removed);

            String nickname = plugin.getMessageManager().replaceDiscordPlaceholders(plugin.getConfig().getString("nickname-format")
                    .replaceAll("\\{ign}", player.getName()),member.getUser().getAsTag(),player.getName(),guild.getName());
            if (added.isEmpty() && removed.isEmpty() && member.getNickname() != null && member.getNickname().equals(nickname)) {
                player.sendMessage(messageManager.replacePlaceholders(
                        messageManager.format(Message.ALREADY_VERIFIED),
                        member.getUser().getAsTag(), sender.getName(), guild.getName()));

                return true;
            }

            if (!added.isEmpty() || !removed.isEmpty())
                if (!plugin.getUtil().modifyMemberRoles(guild,member,added,removed,player)) return true;
            if (this.plugin.getConfig().getBoolean("change-nickname"))
                if (!plugin.getUtil().changeNickname(guild,member,player)) return true;
            player.sendMessage(messageManager.format(Message.UPDATED_ROLES));

            return true;
        }

        ConfigurationSection verified = plugin.getPlayerCache().read().getConfigurationSection("verified");
        if (plugin.getPlayerCache().read() != null && verified != null &&
                verified.getValues(false).containsValue(member.getId())) {
            player.sendMessage(messageManager.replacePlaceholders(messageManager
                    .format(Message.ALREADY_VERIFIED), member
                    .getUser().getAsTag(), sender.getName(), guild.getName()));
            return true;
        }

        final Member finalMember = member;
        member.getUser().openPrivateChannel().queue(privateChannel ->
            privateChannel.sendMessage(messageManager.replacePlaceholders(messageManager.formatDiscord(Message.VERIFY_REQUEST),
                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue(m->{
                player.sendMessage(messageManager.replacePlaceholders(messageManager.format(Message.REQUEST_REPLY),
                        privateChannel.getUser().getAsTag(),sender.getName(),guild.getName()));
                waiter.waitForEvent(PrivateMessageReceivedEvent.class, event -> event.getChannel().getId()
                        .equals(privateChannel.getId()) &&
                        !event.getMessage().getAuthor().isBot(), event -> {

                    if (event.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
                        FileConfiguration playerCache = plugin.getPlayerCache().read();
                        if (playerCache != null) {
                            playerCache.set("verified." + player.getUniqueId().toString(),
                                    privateChannel.getUser().getId());
                        }

                        plugin.getPlayerCache().save();
                        plugin.getPlayerCache().reload();


                        Map<String, Object> roles = plugin.getConfig().getConfigurationSection("roles").getValues(false);
                        Collection<Role> added = new ArrayList<>();
                        for (Map.Entry<String, Object> entry : roles.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (sender.hasPermission("rolesync.role." + key)) {
                                Role role = guild.getRoleById((String) value);
                                if (role == null) continue;
                                added.add(role);
                            }
                        }

                        if (!plugin.getUtil().modifyMemberRoles(guild,finalMember,added,null,player)) return;

                        if (plugin.getConfig().getBoolean("change-nickname"))
                            if (!plugin.getUtil().changeNickname(guild,finalMember,player)) return;

                        sender.sendMessage(messageManager.replacePlaceholders(
                                messageManager.format(Message.VERIFIED_MINECRAFT),
                                privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                        privateChannel.sendMessage(messageManager.replacePlaceholders(
                                messageManager.formatDiscord(Message.VERIFIED_DISCORD),
                                privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                    } else if (event.getMessage().getContentRaw().equalsIgnoreCase("no")) {

                        event.getChannel().sendMessage(messageManager.replacePlaceholders(
                                messageManager.formatDiscord(Message.DENIED_DISCORD),
                                privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();
                        sender.sendMessage(messageManager.replacePlaceholders(
                                messageManager.format(Message.DENIED_MINECRAFT),
                                privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                    }

                }, plugin.getConfig().getInt("verifyTimeout"), TimeUnit.MINUTES, () -> {

                    privateChannel.sendMessage(messageManager.replacePlaceholders(
                            messageManager.formatDiscord(Message.TOO_LONG_DISCORD),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                    sender.sendMessage(messageManager.replacePlaceholders(
                            messageManager.format(Message.TOO_LONG_MC),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                });
            },e-> player.sendMessage(messageManager.replacePlaceholders(messageManager.format(Message.DM_FAILED),
                    finalMember.getUser().getAsTag(), sender.getName(), guild.getName())))
        );
        return true;
    }
}
