package me.confuser.banmanager.webenhancer.storage;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.internal.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.internal.ormlite.support.ConnectionSource;
import me.confuser.banmanager.internal.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.internal.ormlite.table.TableUtils;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

import java.sql.SQLException;

public class ReportLogStorage extends BaseDaoImpl<ReportLogData, Integer> {

  public ReportLogStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ReportLogData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                     .getTable("reportLogs"));

    if (!isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

}
