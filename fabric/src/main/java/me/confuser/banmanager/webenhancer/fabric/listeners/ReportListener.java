package me.confuser.banmanager.webenhancer.fabric.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import me.confuser.banmanager.common.BanManagerPlugin;

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
        List<LogData> logs;
        Queue<LogData> queue = fabricPlugin.getAppender().getQueue();
        synchronized (queue) {
            logs = new ArrayList<>(queue);
        }

        final int reportId = report.getId();

        BanManagerPlugin.getInstance().getScheduler().runAsync(() -> {
            try {
                PlayerReportData reportReloaded = BanManagerPlugin.getInstance()
                    .getPlayerReportStorage().queryForId(reportId);

                if (reportReloaded == null) return;

                for (LogData log : logs) {
                    fabricPlugin.getPlugin().getLogStorage().createIfNotExists(log);
                    fabricPlugin.getPlugin().getReportLogStorage().create(new ReportLogData(reportReloaded, log));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
