package me.confuser.banmanager.webenhancer.configs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.j256.ormlite.table.DatabaseTableConfig;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.webenhancer.data.LogData;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

@Getter
public class DefaultConfig extends Config {

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

    public DefaultConfig() {
        super(new File("config.yml"), "config.yml", BanManagerPlugin.getInstance().getLogger());
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
