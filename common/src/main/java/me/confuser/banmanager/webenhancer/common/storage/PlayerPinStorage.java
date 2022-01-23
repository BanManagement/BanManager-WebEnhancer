package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.webenhancer.common.google.guava.cache.Cache;
import me.confuser.banmanager.webenhancer.common.google.guava.cache.CacheBuilder;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerPinStorage extends BaseDaoImpl<PlayerPinData, Integer> {
  private Cache<UUID, PlayerPinData> pins = CacheBuilder.newBuilder()
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .concurrencyLevel(2)
      .maximumSize(200)
      .build();

  public PlayerPinStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerPinData>) BanManagerPlugin.getInstance().getConfig()
        .getLocalDb()
        .getTable("playerPins"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public PlayerPinData generate(PlayerData player) {
    PlayerPinData pin = null;
    try {
      pin = new PlayerPinData(player);
      if (create(pin) != 1) {
        pin = null;
      } else {
        pins.put(player.getUUID(), pin);
      }
    } catch (NoSuchAlgorithmException | SQLException e) {
      e.printStackTrace();
    }

    return pin;
  }

  public PlayerPinData getValidPin(PlayerData player) {
    PlayerPinData pin = pins.getIfPresent(player.getUUID());

    if (pin == null) {
      pin = generate(player);
    }

    return pin;
  }
}
