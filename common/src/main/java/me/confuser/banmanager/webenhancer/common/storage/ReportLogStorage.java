package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.webenhancer.common.data.ReportLogData;

import java.sql.SQLException;

public class ReportLogStorage extends BaseDaoImpl<ReportLogData, Integer> {

  @SuppressWarnings("unchecked")
  public ReportLogStorage(BanManagerPlugin banManagerPlugin) throws SQLException {
    super(banManagerPlugin.getLocalConn(),
        (DatabaseTableConfig<ReportLogData>) banManagerPlugin.getConfig().getLocalDb().getTable("reportLogs"));

    if (!isTableExists()) {
      TableUtils.createTable(banManagerPlugin.getLocalConn(), tableConfig);
    }
  }

}
