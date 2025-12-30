package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;

import java.sql.SQLException;
import java.util.List;

public class LogStorage extends BaseDaoImpl<LogData, Integer> {

  public LogStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<LogData>) BanManagerPlugin.getInstance().getConfig().getLocalDb()
        .getTable("logs"));

    if (!isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }

      boolean columnAdded = false;
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() +
            " ADD COLUMN messageHash CHAR(64)");
        columnAdded = true;
      } catch (SQLException e) {
      }

      try {
        executeRawNoArgs("CREATE INDEX idx_" + tableConfig.getTableName() +
            "_created_hash ON " + tableConfig.getTableName() + " (created, messageHash)");
      } catch (SQLException e) {
      }

      if (columnAdded) {
        try {
          executeRawNoArgs(
              "UPDATE " + tableConfig.getTableName() +
              " SET messageHash = SHA2(message, 256)" +
              " WHERE messageHash IS NULL"
          );
          BanManagerPlugin.getInstance().getLogger().info("[WebEnhancer] Log hash migration completed");
        } catch (SQLException e) {
          BanManagerPlugin.getInstance().getLogger().info("[WebEnhancer] SQL hash migration skipped (SHA2 not available) - new logs will be indexed");
        }
      }
    }
  }

  public LogData createIfNotExists(LogData data) throws SQLException {
    QueryBuilder<LogData, Integer> query = queryBuilder();

    if (data.getMessageHash() != null) {
      query.where()
          .eq("created", data.getCreated())
          .and()
          .eq("messageHash", data.getMessageHash());
    } else {
      query.where()
          .eq("message", data.getMessage())
          .and()
          .eq("created", data.getCreated());
    }

    List<LogData> results = query.query();

    if (results.size() == 0) {
      create(data);
    } else {
      data.setId(results.get(0).getId());
    }

    return data;
  }
}
