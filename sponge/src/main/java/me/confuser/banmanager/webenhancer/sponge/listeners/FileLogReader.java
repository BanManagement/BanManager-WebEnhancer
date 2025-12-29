package me.confuser.banmanager.webenhancer.sponge.listeners;

import lombok.Getter;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLogReader {

    private final WebEnhancerPlugin plugin;
    private final File logFile;
    @Getter
    private final Queue<LogData> queue;
    private final int maxSize;
    private long lastPosition = 0;
    private long lastFileSize = 0;

    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
        "^\\[(\\d{2}:\\d{2}:\\d{2})\\].*$"
    );

    public FileLogReader(WebEnhancerPlugin plugin, File serverDir, String logFilePath) {
        this.plugin = plugin;
        this.logFile = detectLogFile(serverDir, logFilePath);
        this.maxSize = plugin.getConfig().getAmount();
        this.queue = new LinkedList<>();

        if (logFile.exists()) {
            this.lastPosition = logFile.length();
            this.lastFileSize = logFile.length();
        }
    }

    private File detectLogFile(File serverDir, String configOverride) {
        if (configOverride != null && !configOverride.isEmpty()) {
            File overrideFile = new File(configOverride);
            if (overrideFile.isAbsolute()) {
                return overrideFile;
            }
            return new File(serverDir, configOverride);
        }

        String log4jConfigProp = System.getProperty("log4j.configurationFile");
        if (log4jConfigProp != null && !log4jConfigProp.isEmpty()) {
            String[] configPaths = log4jConfigProp.split(",");
            for (String configPath : configPaths) {
                File configFile = new File(configPath.trim());
                if (!configFile.isAbsolute()) {
                    configFile = new File(serverDir, configPath.trim());
                }
                if (configFile.exists()) {
                    String detected = parseLog4jConfig(configFile, serverDir);
                    if (detected != null) {
                        return new File(detected).isAbsolute()
                            ? new File(detected)
                            : new File(serverDir, detected);
                    }
                }
            }
        }

        File[] defaultConfigs = {
            new File(serverDir, "log4j2.xml"),
            new File(serverDir, "config/log4j2.xml")
        };
        for (File configFile : defaultConfigs) {
            if (configFile.exists()) {
                String detected = parseLog4jConfig(configFile, serverDir);
                if (detected != null) {
                    return new File(detected).isAbsolute()
                        ? new File(detected)
                        : new File(serverDir, detected);
                }
            }
        }

        File latestLog = new File(serverDir, "logs/latest.log");
        if (latestLog.exists()) {
            return latestLog;
        }

        File debugLog = new File(serverDir, "logs/debug.log");
        if (debugLog.exists()) {
            return debugLog;
        }

        return latestLog;
    }

    private String parseLog4jConfig(File configFile, File serverDir) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            String[] appenderTypes = {"RollingRandomAccessFile", "RandomAccessFile", "File", "RollingFile"};
            for (String appenderType : appenderTypes) {
                NodeList appenders = doc.getElementsByTagName(appenderType);
                for (int i = 0; i < appenders.getLength(); i++) {
                    Element appender = (Element) appenders.item(i);
                    String fileName = appender.getAttribute("fileName");
                    if (fileName != null && !fileName.isEmpty() && !fileName.contains("debug")) {
                        return fileName;
                    }
                }
            }

            for (String appenderType : appenderTypes) {
                NodeList appenders = doc.getElementsByTagName(appenderType);
                for (int i = 0; i < appenders.getLength(); i++) {
                    Element appender = (Element) appenders.item(i);
                    String fileName = appender.getAttribute("fileName");
                    if (fileName != null && !fileName.isEmpty()) {
                        return fileName;
                    }
                }
            }
        } catch (Exception e) {
            // Parsing failed, return null to use defaults
        }
        return null;
    }

    public File getLogFile() {
        return logFile;
    }

    public void readNewEntries() {
        if (!logFile.exists()) {
            return;
        }

        try {
            long currentSize = logFile.length();

            if (currentSize < lastFileSize) {
                lastPosition = 0;
            }
            lastFileSize = currentSize;

            if (currentSize <= lastPosition) {
                return;
            }

            try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
                raf.seek(lastPosition);

                String line;
                while ((line = raf.readLine()) != null) {
                    processLine(line);
                }

                lastPosition = raf.getFilePointer();
            }
        } catch (IOException e) {
            // Silently ignore - log file may be locked or unavailable
        }
    }

    private void processLine(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }

        Matcher matcher = LOG_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return;
        }

        boolean ignore = false;

        for (String check : plugin.getConfig().getContains()) {
            if (line.contains(check)) {
                ignore = true;
                break;
            }
        }

        if (!ignore) {
            for (Pattern pattern : plugin.getConfig().getPatterns()) {
                if (pattern.matcher(line).matches()) {
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
                queue.add(new LogData(line, System.currentTimeMillis() / 1000L));
            }
        }
    }
}
