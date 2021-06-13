package io.github.dode5656.rolesync;

import io.github.dode5656.rolesync.commands.ReloadCommand;
import io.github.dode5656.rolesync.commands.SyncCommand;
import io.github.dode5656.rolesync.commands.UnSyncCommand;
import io.github.dode5656.rolesync.events.AuthMeLoginEvent;
import io.github.dode5656.rolesync.events.JoinEvent;
import io.github.dode5656.rolesync.events.ReadyListener;
import io.github.dode5656.rolesync.storage.FileStorage;
import io.github.dode5656.rolesync.utilities.ConfigChecker;
import io.github.dode5656.rolesync.utilities.MessageManager;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class RoleSync extends JavaPlugin {

    private FileStorage playerCache;
    private FileStorage messages;
    private MessageManager messageManager;
    private JDA jda;
    private PluginStatus pluginStatus;
    private ConfigChecker configChecker;
    private final List<Integer> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        pluginStatus = PluginStatus.ENABLED;

        saveDefaultConfig();

        playerCache = new FileStorage("playerCache.yml", new File(getDataFolder().getPath(), "cache"),this);

        messages = new FileStorage("messages.yml", new File(getDataFolder().getPath()),this);
        messages.saveDefaults(this);
        messageManager = new MessageManager(this);

        configChecker = new ConfigChecker(this);
        configChecker.checkDefaults();

        startBot();

        getCommand("sync").setExecutor(new SyncCommand(this));
        getCommand("syncreload").setExecutor(new ReloadCommand(this));
        getCommand("unsync").setExecutor(new UnSyncCommand(this));

        if (getServer().getPluginManager().getPlugin("AuthMe") != null) {

            getServer().getPluginManager().registerEvents(new AuthMeLoginEvent(this),this);

        } else {

            getServer().getPluginManager().registerEvents(new JoinEvent(this), this);

        }

        if (!getConfig().getBoolean("opt-out-bstats", false)) {
            int pluginId = 6790;
            Metrics metrics = new Metrics(this, pluginId);
        }

    }

    @Override
    public void onDisable() {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) jda.shutdown();
        if (!tasks.isEmpty()) {
            for (int taskId : tasks) {
                getServer().getScheduler().cancelTask(taskId);
            }
        }
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

    public ConfigChecker getConfigChecker() {
        return configChecker;
    }

    public void startBot() {
        try {
            this.jda = JDABuilder.createDefault(getConfig().getString("bot-token")).enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).addEventListeners(new ReadyListener(this)).build();
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "Error when logging in!");
            disablePlugin();
        }
    }

    public void disablePlugin() {
        pluginStatus = PluginStatus.DISABLED;
    }

    @Override
    public void saveDefaultConfig() {
        File file = new File(getDataFolder().getPath(),"config.yml");

        if (file.exists()) {

            FileConfiguration tempConfig = new YamlConfiguration();
            try {
                tempConfig.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                getLogger().log(Level.SEVERE, "Couldn't load config.yml", e);
            }

            if (tempConfig.getString("version") != null &&
                    tempConfig.getString("version").equals(getDescription().getVersion())) {
                reloadConfig();
                return;
            }

            File oldDir = new File(getDataFolder().getPath(),"old");

            if (!oldDir.exists()) {
                oldDir.mkdirs();
            }


            file.renameTo(new File(getDataFolder().getPath()+File.separator+"old",
                    "old_"+file.getName()));
        }

        super.saveDefaultConfig();

    }

    public void addTask(int taskId) {
        tasks.add(taskId);
    }
}
