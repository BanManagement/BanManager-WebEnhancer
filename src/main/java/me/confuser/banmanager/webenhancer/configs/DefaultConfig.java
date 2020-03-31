package me.confuser.banmanager.webenhancer.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;

import com.j256.ormlite.table.DatabaseTableConfig;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.webenhancer.data.LogData;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

@Getter
public class DefaultConfig extends Config {
    /** Default java yaml plugin configuration file is config.yml */
    private static final String DEFAULT_CONFIG_FILENAME = "config.yml";

    private static HashMap<String, Class<?>> tables = new HashMap<String, Class<?>>() {
        private static final long serialVersionUID = -3410082528548603183L;
        {
            put("logs", LogData.class);
            put("reportLogs", ReportLogData.class);
            put("playerPins", PlayerPinData.class);
        }
    };

    private ArrayList<Pattern> patterns;
    private List<String> contains;
    private int amount;

    /**
     * Creates new default plugin configuraton representation and
     *      saves default {@link #DEFAULT_CONFIG_FILENAME} file if
     *      it doesn't exists.
     *
     * @param   pluginOwner     The plugin that owns this defaut
     *      configuration file.
     */
    public DefaultConfig(final JavaPlugin pluginOwner) {
        super(
            pluginOwner.getDataFolder(),
            DEFAULT_CONFIG_FILENAME,
            BanManagerPlugin.getInstance().getLogger()
        );

        if (!super.file.exists())
            pluginOwner.saveDefaultConfig();
    }

    @Override
    public void afterLoad() {
        for (Map.Entry<String, Class<?>> entry : tables.entrySet()) {
            DatabaseTableConfig<?> tableConfig = new DatabaseTableConfig<>(
                entry.getValue(),
                conf.getString("tables." + entry.getKey()),
                null
            );
            BanManagerPlugin.getInstance().getConfig().getLocalDb().addTable(entry.getKey(), tableConfig);
        }

        contains = new ArrayList<>();
        patterns = new ArrayList<>();
        amount = conf.getInt("lines", 30);

        if (conf.getStringList("ignoreContains") != null)
            contains = conf.getStringList("ignoreContains");

        List<String> ignorePatterns = conf.getStringList("ignorePatterns");

        if (ignorePatterns == null)
            return;

        for (String pattern : ignorePatterns) {
            patterns.add(Pattern.compile(pattern));
        }
    }

    @Override
    public void onSave() {
        // empty implementation, nothing to do
    }
}
