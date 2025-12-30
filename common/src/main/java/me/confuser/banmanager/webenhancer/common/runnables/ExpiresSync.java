package me.confuser.banmanager.webenhancer.common.runnables;

import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.runnables.BmRunnable;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;

import java.sql.SQLException;

public class ExpiresSync extends BmRunnable {
  private PlayerPinStorage pinStorage;

  public ExpiresSync(WebEnhancerPlugin plugin) {
    super(BanManagerPlugin.getInstance(), "pinCheck");

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
