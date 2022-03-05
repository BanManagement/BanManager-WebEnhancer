package me.confuser.banmanager.webenhancer.bungee;

import lombok.Getter;
import me.confuser.banmanager.bungee.BungeeScheduler;
import me.confuser.banmanager.bungee.PluginLogger;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class BungeePlugin extends Plugin {
  @Getter
  private WebEnhancerPlugin plugin;

  private String[] configs = new String[]{
      "config.yml",
      "messages.yml"
  };
  private Metrics metrics;

  @Override
  public void onEnable() {
    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    metrics = new Metrics(this, 14539);
    plugin = new WebEnhancerPlugin(pluginInfo, new PluginLogger(getLogger()), getDataFolder(), new BungeeScheduler(this), new BungeeMetrics(metrics));

    try {
      plugin.enable();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    setupListeners();
    setupCommands();
    setupRunnables();
  }

  @Override
  public void onDisable() {
    getProxy().getScheduler().cancel(this);
  }

  private PluginInfo setupConfigs() throws IOException {
    if (!getDataFolder().exists()) getDataFolder().mkdir();

    for (String name : configs) {
      File file = new File(getDataFolder(), name);

      if (!file.exists()) {
        try (InputStream in = getResourceAsStream(name)) {
          Files.copy(in, file.toPath());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        Reader defConfigStream = new InputStreamReader(getResourceAsStream(file.getName()), "UTF8");

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        conf.setDefaults(defConfig);
        conf.options().copyDefaults(true);
        conf.save(file);
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = new InputStreamReader(getResourceAsStream("plugin.yml"), "UTF8");
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

    for (String command : commands.getKeys(false)) {
      ConfigurationSection cmd = commands.getConfigurationSection(command);

      pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
    }

    return pluginInfo;
  }

  private void setupCommands() {
    for (CommonCommand cmd : plugin.getCommands()) {
      new BungeeCommand(cmd, this);
    }

    getLogger().info("Registered commands");
  }

  public void setupRunnables() {
//    Runner syncRunner = new Runner(new ExpiresSync(plugin));

//    getServer().getScheduler().runTaskTimerAsynchronously(this, syncRunner, 10L, 10L);
  }

  public void setupListeners() {
  }

  private void registerEvent(Listener listener) {
    getProxy().getPluginManager().registerListener(this, listener);
  }
}
