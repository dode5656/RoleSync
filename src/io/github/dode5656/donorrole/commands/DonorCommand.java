package io.github.dode5656.donorrole.commands;

import net.dv8tion.jda.api.entities.PrivateChannel;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Role;
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

public class DonorCommand implements CommandExecutor
{
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
            this.jda = new JDABuilder(AccountType.BOT).setToken(this.plugin.getConfig().getString("bot-token")).build();
        }
        catch (LoginException e) {
            e.printStackTrace();
        }
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (args.length >= 1) {
                final Guild guild = this.jda.getGuildById(this.plugin.getConfig().getString("server-id"));
                if (guild != null) {
                    Member member = null;
                    try {
                        member = guild.getMemberByTag(args[0]);
                    }
                    catch (Exception ex) {}
                    if (member != null) {
                        final Member finalMember = member;
                        final Guild guild2;
                        final Player player2;
                        final Member member2;
                        final Guild guild3;
                        String sql;
                        boolean result;
                        PreparedStatement stmt;
                        ResultSet rs;
                        String sql2;
                        PreparedStatement stmt2;
                        final Guild guild4;
                        member.getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage(this.plugin.getConfig().getString("messages.verifyRequest").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild2.getName())).queue();
                            this.waiter.waitForEvent(PrivateMessageReceivedEvent.class, event -> event.getChannel().getId().equals(privateChannel.getId()) && !event.getMessage().getAuthor().isBot(), event -> {
                                if (event.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
                                    if (this.plugin.getConfig().getString("storage").equals("file")) {
                                        System.out.println(this.plugin.playerCacheConfig.contains("verified." + player2.getUniqueId().toString()) || member2.getRoles().contains(guild3.getRoleById(this.plugin.getConfig().getString("role-id"))));
                                        if (this.plugin.playerCacheConfig.contains("verified." + player2.getUniqueId().toString()) || member2.getRoles().contains(guild3.getRoleById(this.plugin.getConfig().getString("role-id")))) {
                                            player2.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.alreadyVerified").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName()));
                                            privateChannel.sendMessage(this.plugin.getConfig().getString("messages.alreadyVerified").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName())).queue();
                                            return;
                                        }
                                        else {
                                            this.plugin.playerCacheConfig.set("verified." + player2.getUniqueId().toString(), (Object)privateChannel.getUser().getId());
                                            this.plugin.savePlayerCache();
                                        }
                                    }
                                    else if (this.plugin.getConfig().getString("storage").equals("mysql")) {
                                        sql = "SELECT UUID, DiscordID FROM Verified WHERE %UUID% === ? OR %DiscordID% === ?";
                                        result = false;
                                        try {
                                            stmt = this.plugin.connection.prepareStatement(sql);
                                            stmt.setString(1, player2.getUniqueId().toString().replace("-", ""));
                                            stmt.setLong(2, privateChannel.getUser().getIdLong());
                                            rs = stmt.executeQuery();
                                            result = rs.next();
                                        }
                                        catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        if (result || member2.getRoles().contains(guild3.getRoleById(this.plugin.getConfig().getString("role-id")))) {
                                            player2.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.alreadyVerified").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName()));
                                            privateChannel.sendMessage(this.plugin.getConfig().getString("messages.alreadyVerified").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName())).queue();
                                            return;
                                        }
                                        else {
                                            sql2 = "INSERT INTO Verified(UUID, DiscordID) VALUES (UNHEX(?), ?);";
                                            try {
                                                stmt2 = this.plugin.connection.prepareStatement(sql2);
                                                stmt2.setString(1, player2.getUniqueId().toString().replace("-", ""));
                                                stmt2.setLong(2, privateChannel.getUser().getIdLong());
                                                stmt2.executeUpdate();
                                            }
                                            catch (SQLException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                    }
                                    guild3.addRoleToMember(member2, Objects.requireNonNull(guild3.getRoleById(this.plugin.getConfig().getString("role-id")))).queue();
                                    sender.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.verifiedMinecraft").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName()));
                                    privateChannel.sendMessage(this.plugin.getConfig().getString("messages.verifiedDiscord").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName())).queue();
                                }
                                else if (event.getMessage().getContentRaw().equalsIgnoreCase("no")) {
                                    event.getChannel().sendMessage(this.plugin.getConfig().getString("messages.deniedDiscord").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName())).queue();
                                    sender.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.deniedMinecraft").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild3.getName()));
                                }
                            }, this.plugin.getConfig().getInt("verifyTimeout"), TimeUnit.MINUTES, () -> {
                                privateChannel.sendMessage(this.plugin.getConfig().getString("messages.tooLongDiscord").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild4.getName())).queue();
                                sender.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.deniedMinecraft").replace("{discord_tag}", privateChannel.getUser().getAsTag()).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild4.getName()));
                            });
                            return;
                        });
                    }
                    else {
                        sender.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.badName").replace("{discord_tag}", args[0]).replace("{player_name}", sender.getName()).replace("{discord_server_name}", guild.getName()));
                    }
                }
                else {
                    sender.sendMessage(this.plugin.prefix + this.plugin.getConfig().getString("messages.error"));
                    this.plugin.getLogger().severe("Your supplied server id is invalid. Please update it as soon as possible.");
                }
                return true;
            }
            sender.sendMessage(this.plugin.color(this.plugin.prefix + "Usage: /donor <discordname> - Don't forget to add the numbers after the #."));
        }
        else {
            sender.sendMessage("This command is only for Players. It can't be run in console.");
        }
        return true;
    }
}
