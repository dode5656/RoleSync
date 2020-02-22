package io.github.dode5656.donorrole;

import io.github.dode5656.donorrole.storage.FileStorage;
import io.github.dode5656.donorrole.utilities.MessageManager;
import io.github.dode5656.donorrole.commands.ReloadCommand;
import io.github.dode5656.donorrole.commands.DonorCommand;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class DonorRole extends JavaPlugin
{

    private FileStorage playerCache;
    private FileStorage messages;
    private MessageManager messageManager;
    
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

        }
        else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {

            getLogger().severe(messageManager.defaultError("Server ID"));
            getServer().getPluginManager().disablePlugin(this);

        }
        else if (getConfig().getString("role-id").equals("REPLACEROLEID")) {

            getLogger().severe(messageManager.defaultError("Role ID"));
            getServer().getPluginManager().disablePlugin(this);

        }

        getCommand("donor").setExecutor(new DonorCommand(this));
        getCommand("donorreload").setExecutor(new ReloadCommand(this));

    }

    public final FileStorage getMessages() { return messages; }

    public final FileStorage getPlayerCache() { return playerCache; }

    public final MessageManager getMessageManager() { return messageManager; }
}
