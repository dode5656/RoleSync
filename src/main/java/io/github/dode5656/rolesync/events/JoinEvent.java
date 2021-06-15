package io.github.dode5656.rolesync.events;

import io.github.dode5656.rolesync.RoleSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public final class JoinEvent implements Listener {
    private final RoleSync plugin;

    public JoinEvent(RoleSync plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getUtil().joinEvent(e.getPlayer());
    }

}
