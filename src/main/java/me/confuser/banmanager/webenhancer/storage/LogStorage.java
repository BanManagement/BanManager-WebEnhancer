package me.confuser.banmanager.webenhancer.storage;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.webenhancer.data.LogData;

public class LogStorage extends BaseDaoImpl<LogData, Integer> {

    @SuppressWarnings("unchecked")
    public LogStorage(final ConnectionSource connection) throws SQLException {
        super(
            connection,
            (DatabaseTableConfig<LogData>) BanManagerPlugin.getInstance()
                    .getConfig().getLocalDb().getTable("logs")
        );

        if (!isTableExists()) {
            TableUtils.createTable(connection, tableConfig);
        }
    }

    @Override
    public LogData createIfNotExists(final LogData data) throws SQLException {
        QueryBuilder<LogData, Integer> query = queryBuilder();
        // the TODO message below requires depth explanation or issue,
        //      I'm personally don't unserstand the message

        // TODO Will require full table scan because of message, add index
        query.where().eq("message", data.getMessage())
            .and().eq("created", data.getCreated());

        List<LogData> results = query.query();

        if (results.isEmpty())
            create(data);
        else
            data.setId(results.get(0).getId());

        return data;
    }
}
