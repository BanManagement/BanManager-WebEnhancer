package me.confuser.banmanager.webenhancer.common;

import lombok.Getter;
import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.BanManagerService;
import me.confuser.banmanager.api.database.DatabaseKind;
import me.confuser.banmanager.api.database.MigrationService;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.webenhancer.common.commands.PinCommand;
import me.confuser.banmanager.webenhancer.common.configs.DefaultConfig;
import me.confuser.banmanager.webenhancer.common.configs.MessagesConfig;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;
import me.confuser.banmanager.webenhancer.common.runnables.ExpiresSync;
import me.confuser.banmanager.webenhancer.common.storage.LogStorage;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;
import me.confuser.banmanager.webenhancer.common.storage.ReportLogStorage;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;

/**
 * Common WebEnhancer entry point. Each platform's bootstrap looks up the
 * already-enabled BanManager plugin instance and passes it here, alongside
 * platform glue (logger, scheduler, metrics).
 *
 * <p>The {@link BanManagerPlugin} reference is required because WebEnhancer
 * shares BanManager's shaded ORMLite + entity layer (e.g.
 * {@link me.confuser.banmanager.common.data.PlayerData}); running them as
 * fully-isolated plugins would force WebEnhancer to maintain duplicate
 * entities. WebEnhancer uses {@link BanManager#get()} for everything that
 * <em>is</em> covered by the public API (events, migrations).</p>
 */
public class WebEnhancerPlugin {
  @Getter
  private final PluginInfo pluginInfo;
  @Getter
  private final CommonLogger logger;
  @Getter
  private final CommonMetrics metrics;
  @Getter
  private final BanManagerPlugin banManagerPlugin;
  @Getter
  private final BanManagerService banManagerService;

  @Getter
  private final File dataFolder;
  @Getter
  private DefaultConfig config;
  @Getter
  private final CommonScheduler scheduler;

  @Getter
  private LogStorage logStorage;
  @Getter
  private ReportLogStorage reportLogStorage;
  @Getter
  private PlayerPinStorage playerPinStorage;

  public WebEnhancerPlugin(PluginInfo pluginInfo,
                           CommonLogger logger,
                           File dataFolder,
                           CommonScheduler scheduler,
                           CommonMetrics metrics,
                           BanManagerPlugin banManagerPlugin) {
    this.pluginInfo = Objects.requireNonNull(pluginInfo, "pluginInfo");
    this.logger = Objects.requireNonNull(logger, "logger");
    this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.metrics = Objects.requireNonNull(metrics, "metrics");
    this.banManagerPlugin = Objects.requireNonNull(banManagerPlugin, "banManagerPlugin");
    this.banManagerService = BanManager.get();
  }

  public final void enable() throws Exception {
    setupConfigs();

    banManagerService.migrations().run(new MigrationService.MigrationConfig(
        DatabaseKind.LOCAL,
        "webenhancer",
        "db/webenhancer",
        WebEnhancerPlugin.class.getClassLoader(),
        "logs"));

    try {
      setupStorage();
    } catch (SQLException e) {
      throw new Exception("An error occurred attempting to make a database connection", e);
    }

    new CommonPlayerDeniedListener(this).register();

    scheduler.runAsyncRepeating(new ExpiresSync(this), Duration.ofSeconds(60), Duration.ofSeconds(60));
  }

  public void setupConfigs() {
    loadConfigCompat(new MessagesConfig(dataFolder, logger));

    config = new DefaultConfig(dataFolder, logger, banManagerPlugin);
    loadConfigCompat(config);
  }

  /**
   * Supports BanManager config API changes where Config#load() can vary by return type.
   */
  private void loadConfigCompat(Config config) {
    try {
      config.getClass().getMethod("load").invoke(config);
    } catch (ReflectiveOperationException e) {
      logger.severe("Failed to load config " + config.getClass().getSimpleName() + ": " + e.getMessage());
      throw new IllegalStateException("Failed to load config " + config.getClass().getSimpleName(), e);
    }
  }

  public void setupStorage() throws SQLException {
    logStorage = new LogStorage(banManagerPlugin);
    reportLogStorage = new ReportLogStorage(banManagerPlugin);
    playerPinStorage = new PlayerPinStorage(banManagerPlugin);
  }

  public CommonCommand[] getCommands() {
    return new CommonCommand[]{
        new PinCommand(banManagerPlugin, this)
    };
  }
}
