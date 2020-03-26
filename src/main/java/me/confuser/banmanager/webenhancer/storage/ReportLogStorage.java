package me.confuser.banmanager.webenhancer.storage;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

public class ReportLogStorage extends BaseDaoImpl<ReportLogData, Integer> {

    @SuppressWarnings("unchecked")
    public ReportLogStorage(final ConnectionSource connection) throws SQLException {
        super(
            connection,
            (DatabaseTableConfig<ReportLogData>) BanManagerPlugin.getInstance()
                    .getConfig().getLocalDb().getTable("reportLogs")
        );

        if (!isTableExists())
            TableUtils.createTable(connection, tableConfig);
    }
}
