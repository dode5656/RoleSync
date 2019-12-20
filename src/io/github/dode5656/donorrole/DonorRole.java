package io.github.dode5656.donorrole;

import java.io.IOException;
import org.bukkit.ChatColor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import io.github.dode5656.donorrole.commands.ReloadCommand;
import io.github.dode5656.donorrole.commands.DonorCommand;
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
        username = getConfig().getString("mysql_username");
        password = getConfig().getString("mysql_password");
        url = getConfig().getString("mysql_url");
        prefix = color(getConfig().getString("prefix") + " ");
    }
    
    public void onEnable() {
        getLogger().info("-----------------------");
        getLogger().info(" DonorRole is enabled!");
        getLogger().info("-----------------------");
        saveDefaultConfig();
        if (getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {
            getLogger().warning("Found the default value for Bot Token value. This is an important value, without it the plugin can't work. Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
        else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {
            getLogger().warning("Found the default value for Server ID value. This is an important value, without it the plugin can't work. Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
        else if (getConfig().getString("role-id").equals("REPLACEROLEID")) {
            getLogger().warning("Found the default value for Role ID value. This is an important value, without it the plugin can't work. Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
        getCommand("donor").setExecutor(new DonorCommand(this));
        getCommand("donorreload").setExecutor(new ReloadCommand(this));
        if (getConfig().getString("storage").equalsIgnoreCase("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                getLogger().warning("JDBC driver unavailable! Please install it to be used by the plugin.");
                return;
            }
            try {
                connection = DriverManager.getConnection(url, username, password);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            final String sql = "CREATE TABLE IF NOT EXISTS Verified(UUID BINARY(16), DiscordID BIGINT UNSIGNED)";
            try {
                final PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (getConfig().getString("storage").equalsIgnoreCase("file")) {
            playerCache = new File(getDataFolder() + File.separator + "data", "playerCache.yml");
            playerCacheConfig = YamlConfiguration.loadConfiguration(playerCache);
        }
    }
    
    public void onDisable() {
        getLogger().info("-----------------------");
        getLogger().info(" DonorRole is disabled!");
        getLogger().info("-----------------------");
        if (getConfig().getString("storage").equalsIgnoreCase("mysql")) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
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
            playerCacheConfig.save(playerCache);
        }
        catch (IOException e) {
            getLogger().warning("Could not save PlayerCache file!");
            e.printStackTrace();
        }
    }
}
