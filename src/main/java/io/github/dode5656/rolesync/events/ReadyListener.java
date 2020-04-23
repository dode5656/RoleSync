package io.github.dode5656.rolesync.events;

import io.github.dode5656.rolesync.RoleSync;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

public class ReadyListener implements EventListener {

    private final RoleSync plugin;

    public ReadyListener(RoleSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (!(event instanceof ReadyEvent)) { return; }
        if (!plugin.getConfig().getBoolean("enable-bot-status")) { return; }
        event.getJDA().getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf(plugin.getConfig().getString("bot-status")),plugin.getConfig().getString("custom-status")));
    }
}
