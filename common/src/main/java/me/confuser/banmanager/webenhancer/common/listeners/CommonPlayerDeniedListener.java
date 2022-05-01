package me.confuser.banmanager.webenhancer.common.listeners;

import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public class CommonPlayerDeniedListener {
  private WebEnhancerPlugin plugin;

  public CommonPlayerDeniedListener(WebEnhancerPlugin plugin) {
    this.plugin = plugin;
  }

  public void handlePin(PlayerData player, Message message) {
    if (!message.toString().contains("[pin]")) return;

    PlayerPinData pin = plugin.getPlayerPinStorage().getValidPin(player);


    if (pin != null) {
      message.set("pin", String.valueOf(pin.getGeneratedPin()));
    }
  }
}
