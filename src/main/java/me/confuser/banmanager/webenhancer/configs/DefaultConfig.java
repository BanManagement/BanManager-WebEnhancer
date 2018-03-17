package me.confuser.banmanager.webenhancer.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.bukkitutil.configs.Config;
import me.confuser.banmanager.internal.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.webenhancer.WebEnhancer;
import me.confuser.banmanager.webenhancer.data.LogData;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.data.ReportLogData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultConfig extends Config<WebEnhancer> {

  private static HashMap<String, Class> tables = new HashMap<String, Class>() {{
    put("logs", LogData.class);
    put("reportLogs", ReportLogData.class);
    put("playerPins", PlayerPinData.class);
  }};

  @Getter
  private ArrayList<Pattern> patterns;
  @Getter
  private List<String> contains;
  @Getter
  private int amount;

  public DefaultConfig() {
    super("config.yml");
  }

  @Override
  public void afterLoad() {
    for (Map.Entry<String, Class> entry : tables.entrySet()) {
      BanManager.getPlugin().getConfiguration().getLocalDb()
                .addTable(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), conf.getString("tables." + entry.getKey()), null));
    }

    contains = new ArrayList<>();
    patterns = new ArrayList<>();
    amount = conf.getInt("lines", 30);

    if (conf.getStringList("ignoreContains") != null) contains = conf.getStringList("ignoreContains");

    List<String> ignorePatterns = conf.getStringList("ignorePatterns");

    if (ignorePatterns == null) return;

    for (String pattern : ignorePatterns) {
      patterns.add(Pattern.compile(pattern));
    }
  }

  @Override
  public void onSave() {

  }
}
