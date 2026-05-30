package me.confuser.banmanager.webenhancer.bukkit.listeners;

import me.confuser.banmanager.api.event.player.PlayerReportDeletedEvent;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.api.event.player.PluginReloadedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.webenhancer.bukkit.BukkitPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Bridges Bukkit's report lifecycle into WebEnhancer storage.
 *
 * <p>Subscribes to the cross-platform {@link me.confuser.banmanager.api.event.EventBus}
 * — the per-platform {@code PlayerDeniedEvent}/{@code PlayerBannedEvent}
 * handlers that used to live here have been consolidated into
 * {@link me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener}.</p>
 */
public class ReportListener {
  private final BukkitPlugin plugin;
  private final BanManagerPlugin banManagerPlugin;

  public ReportListener(BukkitPlugin plugin) {
    this.plugin = plugin;
    this.banManagerPlugin = plugin.getPlugin().getBanManagerPlugin();

    var events = plugin.getPlugin().getBanManagerService().events();
    events.subscribe(PlayerReportedEvent.class, this::notifyOnReport);
    events.subscribe(PlayerReportDeletedEvent.class, this::reportDeleted);
    events.subscribe(PluginReloadedEvent.class, e -> plugin.getPlugin().setupConfigs());
  }

  private void notifyOnReport(PlayerReportedEvent event) {
    List<LogData> logs;
    Queue<LogData> queue = plugin.getAppender().getQueue();
    synchronized (queue) {
      logs = new ArrayList<>(queue);
    }

    final int reportId = event.report().id();

    banManagerPlugin.getScheduler().runAsync(() -> {
      try {
        PlayerReportData report = banManagerPlugin.getPlayerReportStorage().queryForId(reportId);

        if (report == null) return;

        for (LogData log : logs) {
          plugin.getPlugin().getLogStorage().createIfNotExists(log);
          plugin.getPlugin().getReportLogStorage().create(new ReportLogData(report, log));
        }
      } catch (SQLException e) {
        plugin.getPlugin().getLogger().warning("Failed to attach report logs: " + e.getMessage());
      }
    });
  }

  private void reportDeleted(PlayerReportDeletedEvent event) {
    int id = event.report().id();

    DeleteBuilder<ReportLogData, Integer> builder = plugin.getPlugin().getReportLogStorage().deleteBuilder();

    try {
      builder.where().eq("report_id", id);
      builder.delete();
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to delete report associations for " + id);
    }
  }
}
