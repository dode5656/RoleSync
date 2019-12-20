package io.github.dode5656.donorrole;

import java.io.IOException;
import org.bukkit.ChatColor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import io.github.dode5656.donorrole.commands.ReloadCommand;
import org.bukkit.command.CommandExecutor;
import io.github.dode5656.donorrole.commands.DonorCommand;
import org.bukkit.plugin.Plugin;
import java.sql.Connection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class DonorRole extends JavaPlugin
{
    private final String username;
    private final String password;
    private final String url;
    private File playerCache;
    public YamlConfiguration playerCacheConfig;
    public Connection connection;
    public String prefix;
    
    public DonorRole() {
        this.username = this.getConfig().getString("mysql_username");
        this.password = this.getConfig().getString("mysql_password");
        this.url = this.getConfig().getString("mysql_url");
        this.prefix = this.color(this.getConfig().getString("prefix") + " ");
    }
    
    public void onEnable() {
        this.getLogger().info("-----------------------");
        this.getLogger().info(" DonorRole is enabled!");
        this.getLogger().info("-----------------------");
        this.saveDefaultConfig();
        if (this.getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {
            this.getLogger().warning("Found the default value for Bot Token value. This is an important value, without it the plugin can't work. Disabling plugin!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        else if (this.getConfig().getString("server-id").equals("REPLACESERVERID")) {
            this.getLogger().warning("Found the default value for Server ID value. This is an important value, without it the plugin can't work. Disabling plugin!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        else if (this.getConfig().getString("role-id").equals("REPLACEROLEID")) {
            this.getLogger().warning("Found the default value for Role ID value. This is an important value, without it the plugin can't work. Disabling plugin!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        this.getCommand("donor").setExecutor((CommandExecutor)new DonorCommand(this));
        this.getCommand("donorreload").setExecutor((CommandExecutor)new ReloadCommand(this));
        if (this.getConfig().getString("storage").equalsIgnoreCase("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                this.getLogger().warning("JDBC driver unavailable! Please install it to be used by the plugin.");
                return;
            }
            try {
                this.connection = DriverManager.getConnection(this.url, this.username, this.password);
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            final String sql = "CREATE TABLE IF NOT EXISTS Verified(UUID BINARY(16), DiscordID BIGINT UNSIGNED)";
            try {
                final PreparedStatement stmt = this.connection.prepareStatement(sql);
                stmt.executeUpdate();
            }
            catch (SQLException e3) {
                e3.printStackTrace();
            }
        }
        if (this.getConfig().getString("storage").equalsIgnoreCase("file")) {
            this.playerCache = new File(this.getDataFolder() + File.separator + "data", "playerCache.yml");
            this.playerCacheConfig = YamlConfiguration.loadConfiguration(this.playerCache);
        }
    }
    
    public void onDisable() {
        this.getLogger().info("-----------------------");
        this.getLogger().info(" DonorRole is disabled!");
        this.getLogger().info("-----------------------");
        if (this.getConfig().getString("storage").equalsIgnoreCase("mysql")) {
            try {
                if (this.connection != null && !this.connection.isClosed()) {
                    this.connection.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    public void savePlayerCache() {
        try {
            this.playerCacheConfig.save(this.playerCache);
        }
        catch (IOException e) {
            this.getLogger().warning("Could not save PlayerCache file!");
            e.printStackTrace();
        }
    }
}
