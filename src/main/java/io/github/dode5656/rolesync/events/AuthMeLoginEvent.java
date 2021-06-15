package io.github.dode5656.rolesync.events;

import fr.xephi.authme.events.LoginEvent;
import io.github.dode5656.rolesync.RoleSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class AuthMeLoginEvent implements Listener {

    private final RoleSync plugin;

    public AuthMeLoginEvent(RoleSync plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        plugin.getUtil().joinEvent(e.getPlayer());
    }

}
