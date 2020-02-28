package io.github.dode5656.donorrole;

import io.github.dode5656.donorrole.events.JoinEvent;
import io.github.dode5656.donorrole.storage.FileStorage;
import io.github.dode5656.donorrole.commands.ReloadCommand;
import io.github.dode5656.donorrole.commands.DonorCommand;

import java.io.File;
import java.util.logging.Level;

import io.github.dode5656.donorrole.utilities.MessageManager;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public class DonorRole extends JavaPlugin {

    private FileStorage playerCache;
    private FileStorage messages;
    private MessageManager messageManager;
    private JDA jda;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        playerCache = new FileStorage("playerCache.yml", new File(getDataFolder().getPath(), "cache"));

        messages = new FileStorage("messages.yml", new File(getDataFolder().getPath()));
        messages.saveDefaults(this);
        messageManager = new MessageManager(this);

        if (getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {

            getLogger().severe(messageManager.defaultError("Bot Token"));
            getServer().getPluginManager().disablePlugin(this);
            return;

        } else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {

            getLogger().severe(messageManager.defaultError("Server ID"));
            getServer().getPluginManager().disablePlugin(this);
            return;

        } else if (getConfig().getConfigurationSection("roles").getValues(false).containsValue("REPLACEROLEID")) {

            getLogger().severe(messageManager.defaultError("a Role ID"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!startBot()) { return; }

        getCommand("donor").setExecutor(new DonorCommand(this));
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

    private boolean startBot() {
        try {
            this.jda = new JDABuilder(AccountType.BOT).setToken(getConfig().getString("bot-token")).build();
            return true;
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "Error when logging in!");
            getServer().getPluginManager().disablePlugin(this);
        }

        return false;
    }
}
