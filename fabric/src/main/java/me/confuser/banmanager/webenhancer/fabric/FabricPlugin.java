package me.confuser.banmanager.webenhancer.fabric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.fabric.listeners.EventListener;
import me.confuser.banmanager.webenhancer.fabric.listeners.LogServerAppender;
import me.confuser.banmanager.webenhancer.fabric.listeners.ReportListener;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class FabricPlugin implements DedicatedServerModInitializer {

    @Getter
    private WebEnhancerPlugin plugin;
    private String[] configs = new String[]{
        "config.yml",
        "messages.yml"
    };
    private PluginInfo pluginInfo;
    @Getter
    private LogServerAppender appender;

    @Override
    public void onInitializeServer() {
        try {
            pluginInfo = setupConfigs();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Use BanManager's scheduler - no need to create our own
        plugin = new WebEnhancerPlugin(
            pluginInfo,
            new PluginLogger(LogManager.getLogger("BanManager-WebEnhancer")),
            getDataFolder(),
            BanManagerPlugin.getInstance().getScheduler(),
            new FabricMetrics()
        );

        try {
            plugin.enable();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        setupCommands();
        setupListeners();

        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStopping(MinecraftServer server) {
        // Remove log appender to prevent leaks
        if (appender != null) {
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).removeAppender(appender);
        }
    }

    private void setupCommands() {
        for (CommonCommand cmd : plugin.getCommands()) {
            new FabricCommand(cmd).register();
        }
    }

    private File getDataFolder() {
        File dataDirectory = FabricLoader.getInstance().getConfigDir().resolve("banmanager-webenhancer").toFile();

        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }

        return dataDirectory;
    }

    private PluginInfo setupConfigs() throws IOException {
        for (String name : configs) {
            File file = new File(getDataFolder(), name);

            if (!file.exists()) {
                try (InputStream in = getResourceAsStream(name)) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (InputStream resourceStream = getResourceAsStream(file.getName())) {
                    if (resourceStream != null) {
                        try (Reader defConfigStream = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
                            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
                            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                            conf.setDefaults(defConfig);
                            conf.options().copyDefaults(true);
                            conf.save(file);
                        }
                    }
                }
            }
        }

        // Load plugin.yml from this mod's container to avoid classloader conflicts with BanManager
        PluginInfo pluginInfo = new PluginInfo();
        try (InputStream pluginYmlStream = getResourceAsStream("plugin.yml")) {
            if (pluginYmlStream == null) {
                throw new IOException("plugin.yml not found in JAR");
            }
            try (Reader defConfigStream = new InputStreamReader(pluginYmlStream, StandardCharsets.UTF_8)) {
                YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
                ConfigurationSection commands = conf.getConfigurationSection("commands");

                if (commands != null) {
                    for (String command : commands.getKeys(false)) {
                        ConfigurationSection cmd = commands.getConfigurationSection(command);
                        if (cmd != null) {
                            pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"),
                                cmd.getStringList("aliases")));
                        }
                    }
                }
            }
        }

        return pluginInfo;
    }

    public void setupListeners() {
        appender = new LogServerAppender(plugin);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);

        new ReportListener(this);
        new EventListener(plugin);
    }

    private InputStream getResourceAsStream(String resource) {
        // Use FabricLoader to get resources from this specific mod's container
        // This avoids classloader conflicts where BanManager's resources might be loaded instead
        return FabricLoader.getInstance()
            .getModContainer("banmanager-webenhancer")
            .flatMap(container -> container.findPath(resource))
            .map(path -> {
                try {
                    return Files.newInputStream(path);
                } catch (IOException e) {
                    return null;
                }
            })
            .orElse(null);
    }
}
