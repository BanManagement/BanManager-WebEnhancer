package me.confuser.banmanager.webenhancer.sponge;

import lombok.Getter;
import com.google.inject.Inject;
import me.confuser.banmanager.sponge.SpongeScheduler;
import me.confuser.banmanager.sponge.PluginLogger;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;

import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.webenhancer.sponge.listeners.ReportListener;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.sponge.listeners.LogServerAppender;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.Dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

@Plugin(
    id = "banmanager-webenhancer",
    name = "BanManager-WebEnhancer",
    version = "@projectVersion@",
    authors = "confuser",
    description = "An addon required by the BanManager WebUI",
    url = "https://banmanagement.com",
    dependencies = {
        @Dependency(id = "banmanager"),
    }
)
public class SpongePlugin {
  private CommonLogger logger;
  @Getter
  private WebEnhancerPlugin plugin;

  @Inject
  @ConfigDir(sharedRoot = false)
  private Path dataFolder;

  @Inject
  private PluginContainer pluginContainer;

  private String[] configs = new String[]{
      "config.yml",
      "messages.yml"
  };
  private Metrics metrics;
  @Getter
  private LogServerAppender appender;

  @Inject
  public SpongePlugin(Logger logger, Metrics.Factory metrics) {
    this.logger = new PluginLogger(logger);
    this.metrics = metrics.make(14039);
  }

  @Listener
  public void onDisable(GameStoppingServerEvent event) {
    // @TODO Disable scheduled tasks somehow

    if (appender == null) return;

    ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).removeAppender(appender);
  }

  @Listener
  public void onEnable(GameInitializationEvent event) {
    PluginInfo pluginInfo;

    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    this.plugin = new WebEnhancerPlugin(pluginInfo, this.logger, dataFolder.toFile(), new SpongeScheduler(this), new SpongeMetrics(metrics));

    try {
      plugin.enable();
    } catch (Exception e) {
      logger.severe("Unable to start BanManager-WebEnhancer");
      e.printStackTrace();
      return;
    }


    setupListeners();
    setupCommands();
    setupRunnables();
  }

  public CommonLogger getLogger() {
    return logger;
  }

  private PluginInfo setupConfigs() throws IOException {
    for (String name : configs) {
      File file = new File(dataFolder.toFile(), name);
      if (file.exists()) {
        // YAMLConfigurationLoader messes with format and makes it unreadable
        Reader defConfigStream = new InputStreamReader(pluginContainer.getAsset(name).get().getUrl().openStream());

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      } else {
        pluginContainer.getAsset(name).get().copyToDirectory(dataFolder);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(pluginContainer.getAsset("plugin.yml").get().getUrl().openStream());
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

    for (String command : commands.getKeys(false)) {
      ConfigurationSection cmd = commands.getConfigurationSection(command);

      pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
    }

    return pluginInfo;
  }

  public void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      new SpongeCommand(this, cmd);
    }
  }

  public void setupRunnables() {
  }

  public void setupListeners() {
    appender = new LogServerAppender(plugin);
    ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);

    registerEvent(new ReportListener(this));
  }

  private void registerEvent(Object listener) {
    Sponge.getEventManager().registerListeners(this, listener);
  }
}
