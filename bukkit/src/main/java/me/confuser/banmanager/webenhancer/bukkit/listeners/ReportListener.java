package me.confuser.banmanager.webenhancer.bukkit.listeners;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.bukkit.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerReportedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.bukkit.api.events.PluginReloadedEvent;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.webenhancer.bukkit.BukkitPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import me.confuser.banmanager.common.BanManagerPlugin;

public class ReportListener implements Listener {
  private final BukkitPlugin plugin;
  private CommonPlayerDeniedListener listener;

  public ReportListener(BukkitPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonPlayerDeniedListener(plugin.getPlugin());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnReport(PlayerReportedEvent event) {
    List<LogData> logs;
    Queue<LogData> queue = plugin.getAppender().getQueue();
    synchronized (queue) {
      logs = new ArrayList<>(queue);
    }

    final int reportId = event.getReport().getId();

    BanManagerPlugin.getInstance().getScheduler().runAsync(() -> {
      try {
        PlayerReportData report = BanManagerPlugin.getInstance()
            .getPlayerReportStorage().queryForId(reportId);

        if (report == null) return;

        for (LogData log : logs) {
          plugin.getPlugin().getLogStorage().createIfNotExists(log);
          plugin.getPlugin().getReportLogStorage().create(new ReportLogData(report, log));
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
  }

  @EventHandler
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

  @EventHandler
  public void onDeny(PlayerDeniedEvent event) {
    listener.handlePin(event.getPlayer(), event.getMessage());
  }

  @EventHandler
  public void onReload(PluginReloadedEvent event) {
    plugin.getPlugin().setupConfigs();
  }
}
