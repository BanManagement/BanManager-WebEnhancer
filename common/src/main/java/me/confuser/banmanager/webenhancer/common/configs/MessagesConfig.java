package me.confuser.banmanager.webenhancer.common.configs;


import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configs.Config;
import me.confuser.banmanager.common.util.Message;

import java.io.File;

public class MessagesConfig extends Config {

  public MessagesConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "messages.yml", logger);
  }

  public void afterLoad() {
    for (String key : conf.getConfigurationSection("messages").getKeys(true)) {
      new Message(key, conf.getString("messages." + key).replace("\\n", "\n"));
    }
  }

  public void onSave() {

  }

}
