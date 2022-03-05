package me.confuser.banmanager.webenhancer.bukkit.listeners;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.bukkit.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerReportedEvent;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.webenhancer.bukkit.BukkitPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Queue;

public class ReportListener implements Listener {
  private final BukkitPlugin plugin;

  public ReportListener(BukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDeny(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.KICK_BANNED) return;

    String msg = event.getKickMessage();

    if (!msg.contains("[pin]")) return;

    PlayerData player;
    try {
      player = BmAPI.getPlayer(event.getUniqueId());
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    PlayerPinData pin = plugin.getPlugin().getPlayerPinStorage().getValidPin(player);

    if (pin != null) {
      msg = msg.replace("[pin]", String.valueOf(pin.getGeneratedPin()));
      event.setKickMessage(msg);
    }


  }
}
