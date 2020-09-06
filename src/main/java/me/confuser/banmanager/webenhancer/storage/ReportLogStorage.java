package me.confuser.banmanager.webenhancer.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

import java.sql.SQLException;

public class ReportLogStorage extends BaseDaoImpl<ReportLogData, Integer> {

  public ReportLogStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ReportLogData>) BanManagerPlugin.getInstance().getConfig().getLocalDb()
        .getTable("reportLogs"));

    if (!isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

}
