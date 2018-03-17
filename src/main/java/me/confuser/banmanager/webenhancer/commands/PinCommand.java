package me.confuser.banmanager.webenhancer.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.bukkitutil.Message;
import me.confuser.banmanager.bukkitutil.commands.BukkitCommand;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.banmanager.webenhancer.WebEnhancer;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PinCommand extends BukkitCommand<WebEnhancer> {

  public PinCommand() {
    super("bmpin");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
    // Disallow console pins
    if (!(sender instanceof Player)) return false;
    if (args.length != 0) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerData player = null;

        try {
          player = BanManager.getPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
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
