package me.confuser.banmanager.webenhancer.common;

import lombok.Getter;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.webenhancer.common.commands.PinCommand;
import me.confuser.banmanager.webenhancer.common.configs.DefaultConfig;
import me.confuser.banmanager.webenhancer.common.configs.MessagesConfig;
import me.confuser.banmanager.webenhancer.common.storage.LogStorage;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;
import me.confuser.banmanager.webenhancer.common.storage.ReportLogStorage;

import java.io.File;
import java.sql.SQLException;

public class WebEnhancerPlugin {
  private static WebEnhancerPlugin self;
  @Getter
  private PluginInfo pluginInfo;
  @Getter
  private final CommonLogger logger;
  @Getter
  private final CommonMetrics metrics;

  // Configs
  @Getter
  private File dataFolder;
  @Getter
  private DefaultConfig config;
  @Getter
  private CommonScheduler scheduler;

  // Storage
  @Getter
  private LogStorage logStorage;
  @Getter
  private ReportLogStorage reportLogStorage;
  @Getter
  private PlayerPinStorage playerPinStorage;

  public WebEnhancerPlugin(PluginInfo pluginInfo, CommonLogger logger, File dataFolder, CommonScheduler scheduler, CommonMetrics metrics) {
    this.pluginInfo = pluginInfo;
    this.logger = logger;
    this.dataFolder = dataFolder;
    this.scheduler = scheduler;
    this.metrics = metrics;
    self = this;
  }

  public final void enable() throws Exception {
    setupConfigs();

    try {
      setupStorage();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new Exception("An error occurred attempting to make a database connection, please see stack trace below");
    }
  }

  public void setupConfigs() {
    new MessagesConfig(dataFolder, logger).load();

    config = new DefaultConfig(dataFolder, logger);
    config.load();
  }

  public void setupStorage() throws SQLException {
    logStorage = new LogStorage(BmAPI.getLocalConnection());
    reportLogStorage = new ReportLogStorage(BmAPI.getLocalConnection());
    playerPinStorage = new PlayerPinStorage(BmAPI.getLocalConnection());
  }

  public CommonCommand[] getCommands() {
    return new CommonCommand[]{
        new PinCommand(BanManagerPlugin.getInstance(), this)
    };
  }
}
