package me.confuser.banmanager.webenhancer.bukkit;

import lombok.Getter;
import me.confuser.banmanager.bukkit.BukkitCommand;
import me.confuser.banmanager.bukkit.BukkitScheduler;
import me.confuser.banmanager.bukkit.PluginLogger;
import org.bstats.bukkit.Metrics;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.webenhancer.bukkit.listeners.ReportListener;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.bukkit.listeners.LogServerAppender;
import org.apache.logging.log4j.LogManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class BukkitPlugin extends JavaPlugin {
  @Getter
  private WebEnhancerPlugin plugin;

  private String[] configs = new String[]{
      "config.yml",
      "messages.yml"
  };
  private Metrics metrics;
  @Getter
  private LogServerAppender appender;

  @Override
  public void onEnable() {
    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    metrics = new Metrics(this, 8838);
    plugin = new WebEnhancerPlugin(pluginInfo, new PluginLogger(getLogger()), getDataFolder(), new BukkitScheduler(this), new BukkitMetrics(metrics));

    try {
      plugin.enable();
    } catch (Exception e) {
      getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    setupListeners();
    setupCommands();
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);

    if (appender == null) return;

    ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).removeAppender(appender);
  }

  private PluginInfo setupConfigs() throws IOException {
    for (String name : configs) {
      if (!new File(getDataFolder(), name).exists()) {
        this.saveResource(name, false);
      } else {
        File file = new File(getDataFolder(), name);
        Reader defConfigStream = new InputStreamReader(getResource(file.getName()), "UTF8");

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(getResource("plugin.yml"), "UTF8");
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
      try {
        getCommand(cmd.getCommandName()).setExecutor(new BukkitCommand(cmd));
      } catch (NullPointerException e) {
        plugin.getLogger().severe("Failed to register /" + cmd.getCommandName() + " command");
      }
    }
  }

  public void setupListeners() {
    appender = new LogServerAppender(plugin);
    ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);

    registerEvent(new ReportListener(this));
  }

  private void registerEvent(Listener listener) {
    getServer().getPluginManager().registerEvents(listener, this);
  }
}
