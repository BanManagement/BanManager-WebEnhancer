package me.confuser.banmanager.webenhancer.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;

import java.sql.SQLException;

public class PinCommand extends CommonCommand {
  private WebEnhancerPlugin plugin;

  public PinCommand(BanManagerPlugin plugin, WebEnhancerPlugin webEnhancerPlugin) {
    super(plugin, "bmpin", false, webEnhancerPlugin.getPluginInfo());

    this.plugin = webEnhancerPlugin;
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    // Disallow console pins
    if (sender.isConsole()) return false;
    if (parser.getArgs().length != 0) return false;

    getPlugin().getScheduler().runAsync(() -> {
      PlayerData player = null;

      try {
        player = getPlugin().getPlayerStorage().queryForId(sender.getData().getId());
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
      }

      PlayerPinData pin = plugin.getPlayerPinStorage().getValidPin(player);

      if (pin == null) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        return;
      }

      Message.get("pin.notify")
          .set("pin", String.valueOf(pin.getGeneratedPin()))
          .set("expires", DateUtils.getDifferenceFormat(pin.getExpires()))
          .sendTo(sender);

      Message.get("pin.pin")
          .set("pin", String.valueOf(pin.getGeneratedPin()))
          .sendTo(sender);
    });

    return true;
  }
}
