package me.confuser.banmanager.webenhancer.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class PlayerPinStorage extends BaseDaoImpl<PlayerPinData, Integer> {

  public PlayerPinStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerPinData>) BanManagerPlugin.getInstance().getConfig()
        .getLocalDb()
        .getTable("playerPins"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      try {
        String update = "ALTER TABLE " + tableConfig
            .getTableName() + " ADD KEY `" + tableConfig.getTableName() + "_player_pin_idx` (`player_id`, `pin`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public PlayerPinData generate(PlayerData player) {
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

  public PlayerPinData getValidPin(PlayerData player) {
    PlayerPinData pin = null;

    try {
      pin = queryBuilder()
          .where().eq("player_id", player.getId()).and().gt("expires", System.currentTimeMillis() / 1000L)
          .queryForFirst();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (pin == null) pin = generate(player);

    return pin;
  }
}