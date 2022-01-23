package me.confuser.banmanager.webenhancer.sponge.listeners;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.sponge.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerReportedEvent;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.webenhancer.sponge.SpongePlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tristate;

import java.sql.SQLException;
import java.util.Iterator;

public class ReportListener {
  private final SpongePlugin plugin;

  public ReportListener(SpongePlugin plugin) {
    this.plugin = plugin;
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnReport(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Iterator<LogData> iterator = plugin.getAppender().getQueue().iterator();

    // Create many-to-many relationship
    while (iterator.hasNext()) {
      LogData log = iterator.next();

      try {
        plugin.getPlugin().getLogStorage().createIfNotExists(log);
        plugin.getPlugin().getReportLogStorage().create(new ReportLogData(report, log));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void reportDeleted(PlayerReportDeletedEvent event) {
    int id = event.getReport().getId();

    DeleteBuilder<ReportLogData, Integer> builder = plugin.getPlugin().getReportLogStorage().deleteBuilder();

    try {
      builder.where().eq("report_id", id);
      builder.delete();
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to delete report associations for " + id);
    }
  }

  @IsCancelled(Tristate.TRUE)
  @Listener(order = Order.BEFORE_POST)
  public void onDeny(final ClientConnectionEvent.Auth event) {
    String msg = TextSerializers.FORMATTING_CODE.serialize(event.getMessage());

    if (!msg.contains("[pin]")) return;

    PlayerData player;
    try {
      player = BmAPI.getPlayer(event.getProfile().getUniqueId());
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    PlayerPinData pin = plugin.getPlugin().getPlayerPinStorage().getValidPin(player);

    if (pin != null) {
      msg = msg.replace("[pin]", String.valueOf(pin.getGeneratedPin()));
      event.setMessage(TextSerializers.FORMATTING_CODE.deserialize(msg));
    }
  }
}
