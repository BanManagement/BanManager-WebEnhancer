package me.confuser.banmanager.webenhancer.listeners;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.collect.EvictingQueue;

import lombok.Getter;
import me.confuser.banmanager.webenhancer.WebEnhancer;
import me.confuser.banmanager.webenhancer.data.LogData;

public class LogServerAppender extends AbstractAppender {

    private WebEnhancer plugin;
    @Getter
    private EvictingQueue<LogData> queue;

    @SuppressWarnings("deprecation")
    public LogServerAppender(final WebEnhancer plugin) {
        super("Log4JAppender", null, PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg").build(),
                false);

        this.plugin = plugin;
        queue = EvictingQueue.create(plugin.getConfiguration().getAmount());
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void append(final LogEvent log) {
        String message = log.getMessage().getFormattedMessage();

        if (message == null)
            return;

        boolean ignore = false;

        for (String check : plugin.getConfiguration().getContains()) {
            if (message.contains(check)) {
                ignore = true;
                break;
            }
        }

        if (!ignore) {
            for (Pattern pattern : plugin.getConfiguration().getPatterns()) {
                if (pattern.matcher(message).matches()) {
                    ignore = true;
                    break;
                }
            }
        }

        if (!ignore) {
            // Not thread safe, sync this if causes issues
            queue.add(new LogData(message, TimeUnit.MILLISECONDS.toSeconds(log.getTimeMillis())));
        }
    }
}
