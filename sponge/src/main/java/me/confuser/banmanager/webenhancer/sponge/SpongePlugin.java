package me.confuser.banmanager.webenhancer.sponge;

import com.google.inject.Inject;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.sponge.PluginLogger;
import me.confuser.banmanager.sponge.SpongeScheduler;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.sponge.listeners.LogServerAppender;
import me.confuser.banmanager.webenhancer.sponge.listeners.ReportListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin("banmanager-webenhancer")
public class SpongePlugin {

    private CommonLogger logger;
    @Getter
    private WebEnhancerPlugin plugin;
    private SpongeScheduler scheduler;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path dataFolder;

    @Inject
    private PluginContainer pluginContainer;

    private String[] configs = new String[]{
        "config.yml",
        "messages.yml"
    };

    @Getter
    private LogServerAppender appender;

    @Inject
    public SpongePlugin(Logger logger) {
        this.logger = new PluginLogger(logger);
    }

    @Listener(order = Order.LATE)
    public void onServerStarted(StartedEngineEvent<Server> event) {
        this.scheduler = new SpongeScheduler(pluginContainer);

        PluginInfo pluginInfo;
        try {
            pluginInfo = setupConfigs();
        } catch (IOException e) {
            this.logger.severe("Failed to setup configs: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        this.plugin = new WebEnhancerPlugin(pluginInfo, this.logger, dataFolder.toFile(), scheduler, new SpongeMetrics());

        try {
            plugin.enable();
        } catch (Exception e) {
            logger.severe("Unable to start BanManager-WebEnhancer");
            e.printStackTrace();
            return;
        }

        setupListeners();
        setupCommands();
    }

    @Listener
    public void onServerStopping(StoppingEngineEvent<Server> event) {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (appender != null) {
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).removeAppender(appender);
        }
    }

    public CommonLogger getLogger() {
        return logger;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    private PluginInfo setupConfigs() throws IOException {
        File dataDir = dataFolder.toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        for (String name : configs) {
            File file = new File(dataDir, name);

            if (!file.exists()) {
                try (InputStream in = getResourceAsStream(name)) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (InputStream in = getResourceAsStream(name);
                     Reader defConfigStream = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
                    if (defConfigStream != null) {
                        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
                        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                        conf.setDefaults(defConfig);
                        conf.options().copyDefaults(true);
                        conf.save(file);
                    }
                }
            }
        }

        PluginInfo pluginInfo = new PluginInfo();
        try (InputStream in = getResourceAsStream("plugin.yml");
             Reader defConfigStream = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
            if (defConfigStream == null) {
                throw new IOException("plugin.yml not found in resources");
            }
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
            ConfigurationSection commands = conf.getConfigurationSection("commands");

            if (commands != null) {
                for (String command : commands.getKeys(false)) {
                    ConfigurationSection cmd = commands.getConfigurationSection(command);
                    pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
                }
            }
        }

        return pluginInfo;
    }

    public void setupCommands() {
        for (CommonCommand cmd : plugin.getCommands()) {
            SpongeCommand spongeCmd = new SpongeCommand(plugin, cmd, pluginContainer);
            spongeCmd.register();
        }
    }

    public void setupListeners() {
        appender = new LogServerAppender(plugin);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);

        Sponge.eventManager().registerListeners(pluginContainer, new ReportListener(this));
    }

    private InputStream getResourceAsStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream("assets/banmanager-webenhancer/" + resource);
    }
}
