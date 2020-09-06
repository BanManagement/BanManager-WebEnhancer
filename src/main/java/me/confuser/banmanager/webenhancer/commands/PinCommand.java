package me.confuser.banmanager.webenhancer.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;

import java.sql.SQLException;

public class PinCommand extends CommonCommand {

  public PinCommand() {
    super(BanManagerPlugin.getInstance(), "bmpin", false);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    // Disallow console pins
    if (sender.isConsole()) return false;
    if (parser.getArgs().length != 0) return false;

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
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
            .set("pin", pin.getGeneratedPin())
            .set("expires", DateUtils.getDifferenceFormat(pin.getExpires()))
            .sendTo(sender);

        Message.get("pin.pin")
            .set("pin", pin.getGeneratedPin())
            .sendTo(sender);
      }

    });

    return true;
  }
}
