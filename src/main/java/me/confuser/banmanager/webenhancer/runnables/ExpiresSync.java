package me.confuser.banmanager.webenhancer.runnables;

import me.confuser.banmanager.internal.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.runnables.BmRunnable;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.webenhancer.WebEnhancer;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.storage.PlayerPinStorage;

import java.sql.SQLException;

public class ExpiresSync extends BmRunnable {
  private PlayerPinStorage pinStorage = WebEnhancer.getPlugin().getPlayerPinStorage();

  public ExpiresSync() {
    super("pinCheck");
  }

  @Override
  public void run() {
    long now = (System.currentTimeMillis() / 1000L) + DateUtils.getTimeDiff();

    CloseableIterator<PlayerPinData> pins = null;
    try {
      pins = pinStorage.queryBuilder().where().le("expires", now).iterator();

      while (pins.hasNext()) {
        PlayerPinData pin = pins.next();

        pinStorage.delete(pin);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (pins != null) pins.closeQuietly();
    }
  }
}
