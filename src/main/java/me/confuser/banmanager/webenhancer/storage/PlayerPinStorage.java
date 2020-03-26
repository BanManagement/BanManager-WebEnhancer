package me.confuser.banmanager.webenhancer.storage;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;

public class PlayerPinStorage extends BaseDaoImpl<PlayerPinData, Integer> {

    @SuppressWarnings("unchecked")
    public PlayerPinStorage(final ConnectionSource connection) throws SQLException {
        super(
            connection,
            (DatabaseTableConfig<PlayerPinData>) BanManagerPlugin.getInstance()
                .getConfig().getLocalDb().getTable("playerPins")
        );

        if (!isTableExists()) {
            TableUtils.createTable(connection, tableConfig);
        } else {
            try {
                executeRawNoArgs(
                    "ALTER TABLE " + tableConfig.getTableName()
                    + " ADD KEY `" + tableConfig.getTableName() + "_player_pin_idx`"
                    + "(`player_id`, `pin`)"
                );
            } catch (SQLException ignore) {}
        }
    }

    public PlayerPinData generate(final PlayerData player) {
        PlayerPinData pin = null;
        try {
            pin = new PlayerPinData(player);
            if (create(pin) != 1) {
                pin = null;
            }
        } catch (NoSuchAlgorithmException | SQLException e) {
            e.printStackTrace();
        }

        return pin;
    }

    public PlayerPinData getValidPin(final PlayerData player) {
        PlayerPinData pin = null;

        try {
            pin = queryBuilder()
                .where().eq("player_id", player.getId())
                .and().gt("expires", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
                .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (pin == null) {
            pin = generate(player);
        }

        return pin;
    }
}