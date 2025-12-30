package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.webenhancer.common.google.guava.cache.Cache;
import me.confuser.banmanager.webenhancer.common.google.guava.cache.CacheBuilder;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
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
  private static final int RATE_LIMIT_SECONDS = 30;

  private Cache<UUID, Long> rateLimitCache = CacheBuilder.newBuilder()
      .expireAfterWrite(RATE_LIMIT_SECONDS, TimeUnit.SECONDS)
      .maximumSize(200)
      .build();

  public PlayerPinStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerPinData>) BanManagerPlugin.getInstance().getConfig()
        .getLocalDb()
        .getTable("playerPins"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `expires` `expires` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  /**
   * Checks if a player is rate limited from generating a new pin.
   *
   * @param playerId the player's UUID
   * @return true if the player must wait before generating a new pin
   */
  public boolean isRateLimited(UUID playerId) {
    return rateLimitCache.getIfPresent(playerId) != null;
  }

  /**
   * Gets the number of seconds remaining until rate limit expires.
   *
   * @param playerId the player's UUID
   * @return seconds remaining, or 0 if not rate limited
   */
  public long getRateLimitRemaining(UUID playerId) {
    Long lastGenerated = rateLimitCache.getIfPresent(playerId);
    if (lastGenerated == null) {
      return 0;
    }
    long elapsed = (System.currentTimeMillis() - lastGenerated) / 1000;
    return Math.max(0, RATE_LIMIT_SECONDS - elapsed);
  }

  private PlayerPinData generate(PlayerData player) {
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

  /**
   * Gets a valid pin for the player, always generating a fresh one.
   * Any existing pins for this player are deleted first to prevent stale pins.
   * Rate limiting should be checked via isRateLimited() before calling this method.
   *
   * @param player the player to generate a pin for
   * @return the newly generated pin, or null if generation failed
   */
  public PlayerPinData getValidPin(PlayerData player) {
    try {
      DeleteBuilder<PlayerPinData, Integer> deleteBuilder = deleteBuilder();
      deleteBuilder.where().eq("player_id", player.getId());
      deleteBuilder.delete();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // Generate fresh pin
    PlayerPinData pin = generate(player);

    // Update rate limit cache on successful generation
    if (pin != null) {
      rateLimitCache.put(player.getUUID(), System.currentTimeMillis());
    }

    return pin;
  }
}
