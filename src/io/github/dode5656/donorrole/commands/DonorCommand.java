package io.github.dode5656.donorrole.commands;


import io.github.dode5656.donorrole.utilities.Message;
import io.github.dode5656.donorrole.utilities.MessageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.AccountType;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import io.github.dode5656.donorrole.DonorRole;
import org.bukkit.command.CommandExecutor;

public class DonorCommand implements CommandExecutor {
    private DonorRole plugin;
    public JDA jda;
    private EventWaiter waiter;

    public DonorCommand(final DonorRole plugin) {
        this.waiter = new EventWaiter();
        this.plugin = plugin;
        this.startBot();
        this.jda.addEventListener(this.waiter);
    }

    private void startBot() {
        try {
            this.jda = new JDABuilder(AccountType.BOT).setToken(plugin.getConfig().getString("bot-token")).build();
        } catch (LoginException e) {
            plugin.getLogger().log(Level.SEVERE, "Error when logging in!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        MessageManager messageManager = plugin.getMessageManager();
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (args.length >= 1) {
                final Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));
                if (guild != null) {
                    Member member = null;
                    try {
                        member = guild.getMemberByTag(args[0]);
                    } catch (Exception ignored) {
                    }
                    if (member != null) {
                        final Member finalMember = member;
                        member.getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage(messageManager.replacePlaceholders(messageManager.formatDiscord(Message.VERIFYREQUEST),
                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();
                            waiter.waitForEvent(PrivateMessageReceivedEvent.class, event -> event.getChannel().getId()
                                    .equals(privateChannel.getId()) &&
                                    !event.getMessage().getAuthor().isBot(), event -> {
                                if (event.getMessage().getContentRaw().equalsIgnoreCase("yes")) {

                                        if (plugin.getPlayerCache().read().contains("verified." + player.getUniqueId().toString()) ||
                                                finalMember.getRoles().contains(guild.getRoleById(plugin.getConfig().getString("role-id")))) {

                                            player.sendMessage(messageManager.replacePlaceholders(
                                                    messageManager.format(Message.ALREADYVERIFIED),
                                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName()));

                                            privateChannel.sendMessage(messageManager.replacePlaceholders(
                                                    messageManager.formatDiscord(Message.ALREADYVERIFIED),
                                                    privateChannel.getUser().getAsTag(), sender.getName(), guild.getName())).queue();

                                            return;

                                        } else {

                                            plugin.getPlayerCache().read().set("verified." + player.getUniqueId().toString(),
                                                    privateChannel.getUser().getId());
                                            plugin.getPlayerCache().save(plugin);

                                        }

                                        guild.addRoleToMember(finalMember,
                                                Objects.requireNonNull(guild.getRoleById(plugin.getConfig().getString("role-id")))).queue();

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

                    } else {

                        sender.sendMessage(messageManager.replacePlaceholders(
                                messageManager.format(Message.BADNAME),
                                args[0], sender.getName(), guild.getName()));
                    }
                } else {

                    sender.sendMessage(messageManager.format(Message.ERROR));
                    plugin.getLogger().severe(Message.INVALIDSERVERID.getMessage());

                }
                return true;

            }

            sender.sendMessage(messageManager.usage(cmd));

        } else {

            sender.sendMessage(messageManager.format(Message.PLAYERONLY));

        }
        return true;
    }
}
