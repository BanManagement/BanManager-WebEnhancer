package me.confuser.banmanager.webenhancer.commands;

import java.sql.SQLException;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.NonNull;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.storage.PlayerPinStorage;

public class PinCommand extends BukkitCommand {
    @NonNull private final JavaPlugin plugin;
    @NonNull private final PlayerPinStorage pinStorage;

    public PinCommand(final JavaPlugin plugin, final PlayerPinStorage pinStorage) {
        super("bmpin");
        this.plugin = plugin;
        this.pinStorage = pinStorage;
    }

    @Override public boolean execute(
            final CommandSender sender, final String commandLabel,
            final String[] args
    ) {
        // Disallow console pins
        if (!(sender instanceof Player))
            return false;
        if (args.length != 0)
            return false;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData player = null;
            try {
                player = BanManagerPlugin.getInstance().getPlayerStorage()
                        .queryForId(UUIDUtils.toBytes(((Player) sender).getUniqueId()));
            } catch (SQLException e) {
                sender.sendMessage(Message.get("sender.error.exception").toString());
                e.printStackTrace();
            }

            final PlayerPinData pin = pinStorage.getValidPin(player);

            if (pin == null) {
                sender.sendMessage(Message.get("sender.error.exception").toString());
                return;
            }

            final String notifyMessage = Message.get("pin.notify").set("pin", pin.getGeneratedPin())
                    .set("expires", DateUtils.getDifferenceFormat(pin.getExpires())).toString();
            sender.sendMessage(notifyMessage);

            final String pinMessage = Message.get("pin.pin").set("pin", pin.getGeneratedPin()).toString();
            sender.sendMessage(pinMessage);
        });

        return true;
    }
}
