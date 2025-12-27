package me.confuser.banmanager.webenhancer.fabric.listeners;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;

public class EventListener {

    private final WebEnhancerPlugin plugin;
    private final CommonPlayerDeniedListener deniedListener;

    public EventListener(WebEnhancerPlugin plugin) {
        this.plugin = plugin;
        this.deniedListener = new CommonPlayerDeniedListener(plugin);

        // Wire BanManager Fabric events
        BanManagerEvents.PLAYER_DENIED_EVENT.register(this::onPlayerDenied);
        BanManagerEvents.PLUGIN_RELOADED_EVENT.register(this::onReload);
    }

    private void onPlayerDenied(PlayerData player, Message message) {
        deniedListener.handlePin(player, message);
    }

    private void onReload(PlayerData actor) {
        plugin.setupConfigs();
    }
}

