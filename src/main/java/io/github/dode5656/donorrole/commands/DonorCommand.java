package io.github.dode5656.donorrole.commands;


import io.github.dode5656.donorrole.utilities.Message;
import io.github.dode5656.donorrole.utilities.MessageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import io.github.dode5656.donorrole.DonorRole;
import org.bukkit.command.CommandExecutor;

public class DonorCommand implements CommandExecutor {
    private DonorRole plugin;
    private EventWaiter waiter;
    private JDA jda;

    public DonorCommand(final DonorRole plugin) {
        this.waiter = new EventWaiter();
        this.plugin = plugin;
        this.jda = plugin.getJDA();
        this.jda.addEventListener(this.waiter);
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        MessageManager messageManager = plugin.getMessageManager();
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.format(Message.PLAYERONLY));
            return true;
        }

        if (!sender.hasPermission("donorrole.use")) {
            sender.sendMessage(messageManager.format(Message.NOPERMCMD));
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
            plugin.getLogger().severe(Message.INVALIDSERVERID.getMessage());
            return true;

        }
        Member member = null;
        try {
            member = guild.getMemberByTag(args[0]);
        } catch (Exception ignored) {
        }
        if (member == null) {

            sender.sendMessage(messageManager.replacePlaceholders(
                    messageManager.format(Message.BADNAME),
                    args[0], sender.getName(), guild.getName()));

            return true;
        }
        final Member finalMember = member;
        member.getUser().openPrivateChannel().queue(privateChannel -> {

            privateChannel.sendMessage(messageManager.replacePlaceholders(messageManager.formatDiscord(Message.VERIFYREQUEST),
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
                                    messageManager.format(Message.ALREADYVERIFIED),
                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                            privateChannel.sendMessage(messageManager.replacePlaceholders(
                                    messageManager.formatDiscord(Message.ALREADYVERIFIED),
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
                    List<String> roleIDs = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : roles.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (sender.hasPermission("donorrole.role." + key)) {
                            roleIDs.add((String) value);
                        }
                    }

                    for (String roleID : roleIDs) {
                        Role role = guild.getRoleById(roleID);
                        if (role == null) {
                            continue;
                        }
                        guild.addRoleToMember(finalMember, role).queue();
                    }

                    sender.sendMessage(messageManager.replacePlaceholders(
                            messageManager.format(Message.VERIFIEDMINECRAFT),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                    privateChannel.sendMessage(messageManager.replacePlaceholders(
                            messageManager.formatDiscord(Message.VERIFIEDDISCORD),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                } else if (event.getMessage().getContentRaw().equalsIgnoreCase("no")) {

                    event.getChannel().sendMessage(messageManager.replacePlaceholders(
                            messageManager.formatDiscord(Message.DENIEDDISCORD),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();
                    sender.sendMessage(messageManager.replacePlaceholders(
                            messageManager.format(Message.DENIEDMINECRAFT),
                            privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                }

            }, plugin.getConfig().getInt("verifyTimeout"), TimeUnit.MINUTES, () -> {

                privateChannel.sendMessage(messageManager.replacePlaceholders(
                        messageManager.formatDiscord(Message.TOOLONGDISCORD),
                        privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                sender.sendMessage(messageManager.replacePlaceholders(
                        messageManager.format(Message.TOOLONGMC),
                        privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

            });

        });

        return true;

    }
}
