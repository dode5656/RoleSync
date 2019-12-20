package io.github.dode5656.donorrole.commands;

import java.sql.ResultSet;
import java.sql.PreparedStatement;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.sql.SQLException;

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
            e.printStackTrace();
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
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
                            privateChannel.sendMessage(plugin.getConfig().getString("messages.verifyRequest")
                                    .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                    .replace("{player_name}", sender.getName())
                                    .replace("{discord_server_name}", guild.getName())).queue();
                            waiter.waitForEvent(PrivateMessageReceivedEvent.class, event -> event.getChannel().getId()
                                    .equals(privateChannel.getId()) &&
                                    !event.getMessage().getAuthor().isBot(), event -> {
                                if (event.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
                                    if (plugin.getConfig().getString("storage").equals("file")) {
                                        System.out.println(plugin.playerCacheConfig.contains("verified." + player.getUniqueId().toString()) ||
                                                finalMember.getRoles().contains(guild.getRoleById(plugin.getConfig().getString("role-id"))));
                                        if (plugin.playerCacheConfig.contains("verified." + player.getUniqueId().toString()) ||
                                                finalMember.getRoles().contains(guild.getRoleById(plugin.getConfig().getString("role-id")))) {
                                            player.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.alreadyVerified")
                                                    .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                                    .replace("{player_name}", sender.getName())
                                                    .replace("{discord_server_name}", guild.getName()));
                                            privateChannel.sendMessage(plugin.getConfig().getString("messages.alreadyVerified")
                                                    .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                                    .replace("{player_name}", sender.getName())
                                                    .replace("{discord_server_name}", guild.getName())).queue();
                                            return;
                                        } else {
                                            plugin.playerCacheConfig.set("verified." + player.getUniqueId().toString(), privateChannel.getUser().getId());
                                            plugin.savePlayerCache();
                                        }
                                    } else if (plugin.getConfig().getString("storage").equals("mysql")) {
                                        String sql;
                                        PreparedStatement stmt;
                                        ResultSet rs;
                                        sql = "SELECT UUID, DiscordID FROM Verified WHERE %UUID% === ? OR %DiscordID% === ?";
                                        boolean result = false;
                                        try {
                                            stmt = plugin.connection.prepareStatement(sql);
                                            stmt.setString(1, player.getUniqueId().toString().replace("-", ""));
                                            stmt.setLong(2, privateChannel.getUser().getIdLong());
                                            rs = stmt.executeQuery();
                                            result = rs.next();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        if (result || finalMember.getRoles().contains(guild.getRoleById(plugin.getConfig().getString("role-id")))) {
                                            player.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.alreadyVerified")
                                                    .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                                    .replace("{player_name}", sender.getName())
                                                    .replace("{discord_server_name}", guild.getName()));
                                            privateChannel.sendMessage(plugin.getConfig().getString("messages.alreadyVerified")
                                                    .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                                    .replace("{player_name}", sender.getName())
                                                    .replace("{discord_server_name}", guild.getName())).queue();
                                            return;
                                        } else {
                                            sql = "INSERT INTO Verified(UUID, DiscordID) VALUES (UNHEX(?), ?);";
                                            try {
                                                stmt = plugin.connection.prepareStatement(sql);
                                                stmt.setString(1, player.getUniqueId().toString().replace("-", ""));
                                                stmt.setLong(2, privateChannel.getUser().getIdLong());
                                                stmt.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    guild.addRoleToMember(finalMember, Objects.requireNonNull(guild.getRoleById(plugin.getConfig().getString("role-id")))).queue();
                                    sender.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.verifiedMinecraft")
                                            .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                            .replace("{player_name}", sender.getName())
                                            .replace("{discord_server_name}", guild.getName()));
                                    privateChannel.sendMessage(plugin.getConfig().getString("messages.verifiedDiscord")
                                            .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                            .replace("{player_name}", sender.getName())
                                            .replace("{discord_server_name}", guild.getName())).queue();
                                } else if (event.getMessage().getContentRaw().equalsIgnoreCase("no")) {
                                    event.getChannel().sendMessage(plugin.getConfig().getString("messages.deniedDiscord")
                                            .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                            .replace("{player_name}", sender.getName())
                                            .replace("{discord_server_name}", guild.getName())).queue();
                                    sender.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.deniedMinecraft")
                                            .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                            .replace("{player_name}", sender.getName())
                                            .replace("{discord_server_name}", guild.getName()));
                                }
                            }, plugin.getConfig().getInt("verifyTimeout"), TimeUnit.MINUTES, () -> {
                                privateChannel.sendMessage(plugin.getConfig().getString("messages.tooLongDiscord")
                                        .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                        .replace("{player_name}", sender.getName())
                                        .replace("{discord_server_name}", guild.getName())).queue();
                                sender.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.deniedMinecraft")
                                        .replace("{discord_tag}", privateChannel.getUser().getAsTag())
                                        .replace("{player_name}", sender.getName())
                                        .replace("{discord_server_name}", guild.getName()));
                            });
                        });
                    } else {
                        sender.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.badName").replace("{discord_tag}", args[0]).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild.getName()));
                    }
                } else {
                    sender.sendMessage(plugin.prefix + plugin.getConfig().getString("messages.error"));
                    plugin.getLogger().severe("Your supplied server id is invalid. Please update it as soon as possible.");
                }
                return true;
            }
            sender.sendMessage(plugin.color(plugin.prefix + "Usage: /donor <discordname> - Don't forget to add the numbers after the #."));
        } else {
            sender.sendMessage("This command is only for Players. It can't be run in console.");
        }
        return true;
    }
}
