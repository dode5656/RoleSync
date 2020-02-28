package io.github.dode5656.donorrole;

import io.github.dode5656.donorrole.events.JoinEvent;
import io.github.dode5656.donorrole.storage.FileStorage;
import io.github.dode5656.donorrole.utilities.MessageManager;
import io.github.dode5656.donorrole.commands.ReloadCommand;
import io.github.dode5656.donorrole.commands.DonorCommand;

import java.io.File;
import java.util.logging.Level;

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

        messageManager = new MessageManager(this);

        playerCache = new FileStorage("kits.yml", new File(getDataFolder().getPath()));
        playerCache.saveDefaults(this);

        messages = new FileStorage("messages.yml", new File(getDataFolder().getPath()));
        messages.saveDefaults(this);

        saveDefaultConfig();

        if (getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {

            getLogger().severe(messageManager.defaultError("Bot Token"));
            getServer().getPluginManager().disablePlugin(this);

        } else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {

            getLogger().severe(messageManager.defaultError("Server ID"));
            getServer().getPluginManager().disablePlugin(this);

        } else if (getConfig().getConfigurationSection("roles").getValues(false).containsValue("REPLACEROLEID")) {

            getLogger().severe(messageManager.defaultError("a Role ID"));
            getServer().getPluginManager().disablePlugin(this);

        }

        startBot();

        getCommand("donor").setExecutor(new DonorCommand(this));
        getCommand("donorreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);

    }

    @Override
    public void onDisable() {
        jda.shutdown();
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

    private void startBot() {
        try {
            this.jda = new JDABuilder(AccountType.BOT).setToken(getConfig().getString("bot-token")).build();
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "Error when logging in!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
