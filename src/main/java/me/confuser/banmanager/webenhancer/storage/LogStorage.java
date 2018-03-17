package me.confuser.banmanager.webenhancer.storage;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.internal.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.internal.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.internal.ormlite.support.ConnectionSource;
import me.confuser.banmanager.internal.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.internal.ormlite.table.TableUtils;
import me.confuser.banmanager.webenhancer.data.LogData;

import java.sql.SQLException;
import java.util.List;

public class LogStorage extends BaseDaoImpl<LogData, Integer> {

  public LogStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<LogData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                               .getTable("logs"));

    if (!isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public LogData createIfNotExists(LogData data) throws SQLException {
    QueryBuilder<LogData, Integer> query = queryBuilder();
    //TODO Will require full table scan because of message, add index
    query.where().eq("message", data.getMessage()).and().eq("created", data.getCreated());

    List<LogData> results = query.query();

    if (results.size() == 0) {
      create(data);
    } else {
      data.setId(results.get(0).getId());
    }

    return data;
  }
}
