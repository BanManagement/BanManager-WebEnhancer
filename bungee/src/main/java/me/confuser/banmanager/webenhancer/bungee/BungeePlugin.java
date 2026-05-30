package me.confuser.banmanager.webenhancer.bungee;

import lombok.Getter;
import me.confuser.banmanager.bungee.BMBungeePlugin;
import me.confuser.banmanager.bungee.BungeeScheduler;
import me.confuser.banmanager.bungee.PluginLogger;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;

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
    BMBungeePlugin banManager = (BMBungeePlugin) getProxy().getPluginManager().getPlugin("BanManager");
    if (banManager == null || banManager.getPlugin() == null) {
      getLogger().severe("BanManager is not enabled — cannot start BanManager-WebEnhancer.");
      return;
    }

    PluginInfo pluginInfo;
    try {
      pluginInfo = setupConfigs();
    } catch (IOException e) {
      getLogger().log(java.util.logging.Level.WARNING, "Failed to set up plugin configuration", e);
      return;
    }

    metrics = new Metrics(this, 14539);
    plugin = new WebEnhancerPlugin(pluginInfo, new PluginLogger(getLogger()), getDataFolder(),
        new BungeeScheduler(this), new BungeeMetrics(metrics), banManager.getPlugin());

    try {
      plugin.enable();
    } catch (Exception e) {
      getLogger().log(java.util.logging.Level.WARNING, "Failed to enable BanManager-WebEnhancer", e);
      return;
    }

    setupCommands();
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
}
