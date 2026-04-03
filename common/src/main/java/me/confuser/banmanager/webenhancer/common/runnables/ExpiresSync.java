package me.confuser.banmanager.webenhancer.common.runnables;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;

import java.sql.SQLException;

public class ExpiresSync implements Runnable {
  private PlayerPinStorage pinStorage;

  public ExpiresSync(WebEnhancerPlugin plugin) {
    pinStorage = plugin.getPlayerPinStorage();
  }

  @Override
  public void run() {
    long now = (System.currentTimeMillis() / 1000L) + DateUtils.getTimeDiff();

    try {
      DeleteBuilder<PlayerPinData, Integer> deleteBuilder = pinStorage.deleteBuilder();
      deleteBuilder.where().le("expires", now);
      deleteBuilder.delete();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
