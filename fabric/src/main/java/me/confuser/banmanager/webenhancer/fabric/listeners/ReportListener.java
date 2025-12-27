package me.confuser.banmanager.webenhancer.fabric.listeners;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Queue;

import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.fabric.BanManagerEvents.SilentValue;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;
import me.confuser.banmanager.webenhancer.fabric.FabricPlugin;

public class ReportListener {

    private final FabricPlugin fabricPlugin;

    public ReportListener(FabricPlugin fabricPlugin) {
        this.fabricPlugin = fabricPlugin;

        BanManagerEvents.PLAYER_REPORTED_EVENT.register(this::notifyOnReport);
        BanManagerEvents.PLAYER_REPORT_DELETED_EVENT.register(this::reportDeleted);
    }

    private void notifyOnReport(PlayerReportData report, boolean silent) {
        Queue<LogData> queue = fabricPlugin.getAppender().getQueue();

        synchronized (queue) {
            Iterator<LogData> iterator = queue.iterator();

            // Create many-to-many relationship
            while (iterator.hasNext()) {
                LogData log = iterator.next();

                try {
                    fabricPlugin.getPlugin().getLogStorage().createIfNotExists(log);
                    fabricPlugin.getPlugin().getReportLogStorage().create(new ReportLogData(report, log));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reportDeleted(PlayerReportData report) {
        int id = report.getId();

        DeleteBuilder<ReportLogData, Integer> builder = fabricPlugin.getPlugin().getReportLogStorage().deleteBuilder();

        try {
            builder.where().eq("report_id", id);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            fabricPlugin.getPlugin().getLogger().warning("Failed to delete report associations for " + id);
        }
    }
}

