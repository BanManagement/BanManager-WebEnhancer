package me.confuser.banmanager.webenhancer.sponge.listeners;

import lombok.Getter;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

public class LogServerAppender extends AbstractAppender {

    private WebEnhancerPlugin plugin;
    @Getter
    private Queue<LogData> queue;
    private int maxSize;

    public LogServerAppender(WebEnhancerPlugin plugin) {
        super("Log4JAppender", null,
            PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg").build(), false, null);

        this.plugin = plugin;
        this.maxSize = plugin.getConfig().getAmount();
        this.queue = new LinkedList<>();
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
            synchronized (queue) {
                if (queue.size() >= maxSize) {
                    queue.poll();
                }
                queue.add(new LogData(message, log.getTimeMillis() / 1000L));
            }
        }
    }
}
