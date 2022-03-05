package me.confuser.banmanager.webenhancer.bungee.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.webenhancer.bungee.BungeePlugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.bungee.BungeeServer;
import java.sql.SQLException;

public class BanListener implements Listener {
  private final CommonJoinListener listener;
  private final BungeePlugin plugin;

  public BanListener(BungeePlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(BanManagerPlugin.getInstance());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDeny(LoginEvent event) {
    event.registerIntent(plugin);

    // Work around until Bungee triggers async events in priority order :(
    // https://github.com/SpigotMC/BungeeCord/issues/1491
    // https://github.com/SpigotMC/BungeeCord/issues/1858
    plugin.getPlugin().getScheduler().runAsync(() -> {
      listener.banCheck(event.getConnection().getUniqueId(), event.getConnection().getName(), IPUtils.toIPAddress(event.getConnection().getAddress().getAddress()), new BanJoinHandler(event));

      event.completeIntent(plugin);
    });
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final LoginEvent event;

    @Override
    public void handleDeny(Message message) {
      if (message.toString().contains("[pin]")) {
        PlayerData player;

        try {
          player = BmAPI.getPlayer(event.getConnection().getUniqueId());
        } catch (SQLException e) {
          e.printStackTrace();
          event.completeIntent(plugin);
          return;
        }

        PlayerPinData pin = plugin.getPlugin().getPlayerPinStorage().getValidPin(player);

        if (pin != null) {
          message.set("pin", String.valueOf(pin.getGeneratedPin()));
        }
      }

      event.setCancelled(true);
      event.setCancelReason(BungeeServer.formatMessage(message.toString()));
    }
  }
}
