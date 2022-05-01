package me.confuser.banmanager.webenhancer.sponge.listeners;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.sponge.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerReportedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.sponge.api.events.PluginReloadedEvent;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.webenhancer.sponge.SpongePlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Queue;

public class ReportListener {
  private final SpongePlugin plugin;
  private CommonPlayerDeniedListener listener;

  public ReportListener(SpongePlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonPlayerDeniedListener(plugin.getPlugin());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnReport(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();
    Queue<LogData> queue = plugin.getAppender().getQueue();

    synchronized (queue) {
      Iterator<LogData> iterator = queue.iterator();

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

  @Listener(order = Order.BEFORE_POST)
  public void onDeny(final PlayerDeniedEvent event) {
    listener.handlePin(event.getPlayer(), event.getMessage());
  }

  @Listener
  public void onReload(PluginReloadedEvent event) {
    plugin.getPlugin().setupConfigs();
  }
}
