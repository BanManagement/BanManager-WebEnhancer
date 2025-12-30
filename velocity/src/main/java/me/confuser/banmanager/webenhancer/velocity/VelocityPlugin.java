package me.confuser.banmanager.webenhancer.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.SneakyThrows;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.PluginLogger;
import me.confuser.banmanager.velocity.VelocityScheduler;
import me.confuser.banmanager.webenhancer.VelocityCommand;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.velocity.listener.BanListener;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "banmanagerwebenhancer",
        name = "BanManagerWebEnhancer",
        version = "@projectVersion@",
        url = "https://banmanagement.com",
        description = "A suite of moderation plugins & apps for Minecraft servers",dependencies = {
        @Dependency(id = "banmanager")},
        authors = {
                "confuser",
                "Lorias-Jak"
        })
public class VelocityPlugin {

        @Getter
        private WebEnhancerPlugin plugin;
        private final String[] configs = new String[]{
                "config.yml",
                "messages.yml"
        };
        private Metrics.Factory metrics;

        public ProxyServer server;
        private final Logger logger;
        private final File dataDirectory;
        private final VelocityPlugin instance;

        @Inject
        public VelocityPlugin(ProxyServer server, Logger logger, @DataDirectory final Path directory, Metrics.Factory metricsFactory) {
                this.server = server;
                this.logger = logger;
                instance = this;
                this.dataDirectory = directory.toFile();
                this.metrics = metricsFactory;
        }

        @Subscribe
        public void onProxyInitialisation(ProxyInitializeEvent event) {
                PluginInfo pluginInfo;
                try {
                        pluginInfo = setupConfigs();
                } catch (IOException e) {
                        e.printStackTrace();
                        return;
                }

                plugin = new WebEnhancerPlugin(pluginInfo, new PluginLogger(logger), dataDirectory, new VelocityScheduler(this, server), new VelocityMetrics(metrics.make(this, 14539)));

                try {
                        plugin.enable();
                } catch (Exception e) {
                        e.printStackTrace();
                        return;
                }

                setupListeners();
                setupCommands();

        }

        private PluginInfo setupConfigs() throws IOException {
                if (!dataDirectory.exists()) {
                        dataDirectory.mkdir();
                }

                for (String name : configs) {
                        File file = new File(dataDirectory, name);

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

        @SneakyThrows
        private InputStream getResourceAsStream(String resource) {
                Class<?> cls = getClass();
                ClassLoader cLoader = cls.getClassLoader();
                return cLoader.getResourceAsStream(resource);
        }

        private void setupCommands() {
                for (CommonCommand cmd : plugin.getCommands()) {
                        new VelocityCommand(cmd, this);
                }

                logger.info("Registered commands");
        }

        public void setupListeners() {
                registerEvent(new BanListener(this));
        }

        private void registerEvent(Listener listener) {
                server.getEventManager().register(this, listener);
        }
}
