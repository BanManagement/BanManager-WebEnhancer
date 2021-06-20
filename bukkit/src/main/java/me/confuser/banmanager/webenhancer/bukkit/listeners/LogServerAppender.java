package me.confuser.banmanager.webenhancer.bukkit.listeners;

import com.google.common.collect.EvictingQueue;
import lombok.Getter;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.regex.Pattern;

public class LogServerAppender extends AbstractAppender {

  private WebEnhancerPlugin plugin;
  @Getter
  private EvictingQueue<LogData> queue;

  public LogServerAppender(WebEnhancerPlugin plugin) {
    super("Log4JAppender", null,
            PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg").build(), false);

    this.plugin = plugin;
    queue = EvictingQueue.create(plugin.getConfig().getAmount());
  }

  @Override
  public boolean isStarted() {
    return true;
  }

  @Override
  public void append(LogEvent log) {
    String message = log.getMessage().getFormattedMessage();

    if (message == null) return;

    boolean ignore = false;

    for (String check : plugin.getConfig().getContains()) {
      if (message.contains(check)) {
        ignore = true;
        break;
      }
    }

    if (!ignore) {
      for (Pattern pattern : plugin.getConfig().getPatterns()) {
        if (pattern.matcher(message).matches()) {
          ignore = true;
          break;
        }
      }
    }

    if (!ignore) {
      // Not thread safe, sync this if causes issues
      queue.add(new LogData(message, log.getTimeMillis() / 1000L));
    }
  }
}
