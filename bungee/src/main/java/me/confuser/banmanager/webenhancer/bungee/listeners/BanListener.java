package me.confuser.banmanager.webenhancer.bungee.listeners;

import me.confuser.banmanager.bungee.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.webenhancer.bungee.BungeePlugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;

public class BanListener implements Listener {
  private final BungeePlugin plugin;
  private final CommonPlayerDeniedListener listener;

  public BanListener(BungeePlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonPlayerDeniedListener(plugin.getPlugin());
  }

  @EventHandler
  public void onDeny(PlayerDeniedEvent event) {
    listener.handlePin(event.getPlayer(), event.getMessage());
  }
}
