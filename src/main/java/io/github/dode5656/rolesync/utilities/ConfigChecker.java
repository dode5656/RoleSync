package io.github.dode5656.rolesync.utilities;

import io.github.dode5656.rolesync.RoleSync;

public final class ConfigChecker {

    private final RoleSync plugin;

    public ConfigChecker(RoleSync plugin) {
        this.plugin = plugin;
    }

    public void checkDefaults() {
        if (plugin.getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {

            plugin.getLogger().severe(plugin.getMessageManager().defaultError("Bot Token"));
            plugin.disablePlugin();

        } else if (plugin.getConfig().getString("server-id").equals("REPLACESERVERID")) {

            plugin.getLogger().severe(plugin.getMessageManager().defaultError("Server ID"));
            plugin.disablePlugin();

        } else if (plugin.getConfig().getConfigurationSection("roles").getValues(false).containsValue("REPLACEROLEID")) {
            plugin.getLogger().severe(plugin.getMessageManager().defaultError("a Role ID"));
            plugin.disablePlugin();
        }
    }

}
