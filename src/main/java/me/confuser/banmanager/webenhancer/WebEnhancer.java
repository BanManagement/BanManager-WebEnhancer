package me.confuser.banmanager.webenhancer;

import lombok.Getter;
import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.bukkitutil.BukkitPlugin;
import me.confuser.banmanager.runnables.Runner;
import me.confuser.banmanager.webenhancer.commands.PinCommand;
import me.confuser.banmanager.webenhancer.configs.DefaultConfig;
import me.confuser.banmanager.webenhancer.listeners.LogServerAppender;
import me.confuser.banmanager.webenhancer.listeners.ReportListener;
import me.confuser.banmanager.webenhancer.runnables.ExpiresSync;
import me.confuser.banmanager.webenhancer.storage.LogStorage;
import me.confuser.banmanager.webenhancer.storage.PlayerPinStorage;
import me.confuser.banmanager.webenhancer.storage.ReportLogStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class WebEnhancer extends BukkitPlugin {

  @Getter
  private static WebEnhancer plugin;

  @Getter
  private DefaultConfig configuration;
  @Getter
  private LogServerAppender appender;

  @Getter
  private LogStorage logStorage;
  @Getter
  private ReportLogStorage reportLogStorage;
  @Getter
  private PlayerPinStorage playerPinStorage;

  @Getter
  private Runner syncRunner;

  public void onEnable() {
    plugin = this;

    setupConfigs();
    try {
      setupStorage();
    } catch (SQLException e) {
      getLogger().warning("An error occurred attempting to enable the plugin");
      plugin.getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    setupListeners();
    setupCommands();
//    setupRunnables();
  }

  public void onDisable() {
    getServer().getScheduler().cancelTasks(plugin);

    if (appender == null) return;

    Logger log = (Logger) LogManager.getRootLogger();
    log.removeAppender(appender);
  }

  @Override
  public String getPluginFriendlyName() {
    return "BanManager-WebEnhancer";
  }

  @Override
  public String getPermissionBase() {
    return "bm";
  }

  @Override
  public void setupConfigs() {
    configuration = new DefaultConfig();
    configuration.load();
  }

  public void setupStorage() throws SQLException {
    logStorage = new LogStorage(BmAPI.getLocalConnection());
    reportLogStorage = new ReportLogStorage(BmAPI.getLocalConnection());
    playerPinStorage = new PlayerPinStorage(BmAPI.getLocalConnection());
  }

  @Override
  public void setupCommands() {
    new PinCommand().register();
  }

  @Override
  public void setupListeners() {
    appender = new LogServerAppender(plugin);

    Logger log = (Logger) LogManager.getRootLogger();
    log.addAppender(appender);

    new ReportListener().register();
  }

  @Override
  public void setupRunnables() {
    syncRunner = new Runner(new ExpiresSync());

    setupAsyncRunnable(10L, syncRunner);
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, length, length);
  }
}
