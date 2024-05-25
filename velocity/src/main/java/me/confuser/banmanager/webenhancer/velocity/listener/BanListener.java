package me.confuser.banmanager.webenhancer.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;
import me.confuser.banmanager.webenhancer.velocity.VelocityPlugin;

public class BanListener extends Listener {
    private final VelocityPlugin velocityPlugin;
    private final CommonPlayerDeniedListener listener;

    public BanListener(VelocityPlugin velocityPlugin) {
        this.velocityPlugin = velocityPlugin;
        this.listener = new CommonPlayerDeniedListener(velocityPlugin.getPlugin());
    }

    @Subscribe
    public void onDeny(PlayerDeniedEvent event) {
        listener.handlePin(event.getPlayer(), event.getMessage());
    }

    @Subscribe
    public void onReload(ProxyReloadEvent event) {
        velocityPlugin.getPlugin().setupConfigs();
    }

}
