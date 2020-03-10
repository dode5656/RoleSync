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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SyncCommand implements CommandExecutor {
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
            sender.sendMessage(messageManager.usage(cmd));
            return true;
        }

        final Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

        if (guild == null) {

            sender.sendMessage(messageManager.format(Message.ERROR));
            plugin.getLogger().severe(Message.INVALID_SERVER_ID.getMessage());
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

                Member idMember = guild.getMemberById(args[1]);
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
        final Member finalMember = member;
        member.getUser().openPrivateChannel().queue(privateChannel -> {

            privateChannel.sendMessage(messageManager.replacePlaceholders(messageManager.formatDiscord(Message.VERIFY_REQUEST),
                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();
            waiter.waitForEvent(PrivateMessageReceivedEvent.class, event -> event.getChannel().getId()
                    .equals(privateChannel.getId()) &&
                    !event.getMessage().getAuthor().isBot(), event -> {

                if (event.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
                    if (plugin.getPlayerCache().read() != null && plugin.getPlayerCache().read().contains("verified." + player.getUniqueId().toString())) {
                        boolean result = false;
                        for ( Map.Entry<String, Object> entry : plugin.getConfig().getConfigurationSection("roles").getValues(false).entrySet()) {
                            Object value = entry.getValue();
                            Role role = guild.getRoleById((String) value);
                            if (role == null) {
                                continue;
                            }
                            if(finalMember.getRoles().contains(role)) {
                                result = true;
                            }
                        }
                        if (result) {
                            player.sendMessage(messageManager.replacePlaceholders(
                                    messageManager.format(Message.ALREADY_VERIFIED),
                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                            privateChannel.sendMessage(messageManager.replacePlaceholders(
                                    messageManager.formatDiscord(Message.ALREADY_VERIFIED),
                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                            return;
                        }

                    } else {
                        FileConfiguration playerCache = plugin.getPlayerCache().read();
                        if (playerCache != null) {
                            playerCache.set("verified." + player.getUniqueId().toString(),
                                    privateChannel.getUser().getId());
                        }

                        plugin.getPlayerCache().save(plugin);
                        plugin.getPlayerCache().reload();

                    }

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

                    guild.modifyMemberRoles(finalMember, added, null).queue();

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

        });

        return true;

    }
}
