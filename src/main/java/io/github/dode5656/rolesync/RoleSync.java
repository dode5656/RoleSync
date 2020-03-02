package io.github.dode5656.rolesync;

import io.github.dode5656.rolesync.commands.ReloadCommand;
import io.github.dode5656.rolesync.commands.SyncCommand;
import io.github.dode5656.rolesync.events.JoinEvent;
import io.github.dode5656.rolesync.storage.FileStorage;
import io.github.dode5656.rolesync.utilities.MessageManager;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.logging.Level;

public class RoleSync extends JavaPlugin {

    private FileStorage playerCache;
    private FileStorage messages;
    private MessageManager messageManager;
    private JDA jda;
    private PluginStatus pluginStatus;

    @Override
    public void onEnable() {
        pluginStatus = PluginStatus.ENABLED;

        saveDefaultConfig();

        playerCache = new FileStorage("playerCache.yml", new File(getDataFolder().getPath(), "cache"));

        messages = new FileStorage("messages.yml", new File(getDataFolder().getPath()));
        messages.saveDefaults(this);
        messageManager = new MessageManager(this);

        if (getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {

            getLogger().severe(messageManager.defaultError("Bot Token"));
            disablePlugin();

        } else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {

            getLogger().severe(messageManager.defaultError("Server ID"));
            disablePlugin();

        } else if (getConfig().getConfigurationSection("roles").getValues(false).containsValue("REPLACEROLEID")) {
            getLogger().severe(messageManager.defaultError("a Role ID"));
            disablePlugin();
        }

        startBot();

        getCommand("donor").setExecutor(new SyncCommand(this));
        getCommand("donorreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);

    }

    @Override
    public void onDisable() {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) jda.shutdown();
    }

    public final FileStorage getMessages() {
        return messages;
    }

    public final FileStorage getPlayerCache() {
        return playerCache;
    }

    public final MessageManager getMessageManager() {
        return messageManager;
    }

    public final JDA getJDA() {
        return jda;
    }

    public PluginStatus getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(PluginStatus pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    private void startBot() {
        try {
            this.jda = new JDABuilder(AccountType.BOT).setToken(getConfig().getString("bot-token")).build();
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "Error when logging in!");
            disablePlugin();
        }
    }

    private void disablePlugin() {
        pluginStatus = PluginStatus.DISABLED;
    }
}
