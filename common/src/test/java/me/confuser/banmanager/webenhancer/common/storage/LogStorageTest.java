package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.webenhancer.common.data.LogData;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LogStorage and LogData.
 *
 * Note: Full integration tests requiring database are in E2E tests.
 * These tests focus on LogData behavior that can be tested without database.
 */
public class LogStorageTest {

    @Test
    public void shouldCreateLogWithMessage() {
        String message = "Test log message";
        long created = System.currentTimeMillis() / 1000L;

        LogData log = new LogData(message, created);

        assertEquals("Message should be set", message, log.getMessage());
        assertEquals("Created timestamp should be set", created, log.getCreated());
    }

    @Test
    public void shouldHaveZeroIdBeforePersistence() {
        LogData log = new LogData("Test", System.currentTimeMillis() / 1000L);

        assertEquals("ID should be 0 before database persistence", 0, log.getId());
    }

    @Test
    public void shouldAllowSettingId() {
        LogData log = new LogData("Test", System.currentTimeMillis() / 1000L);
        log.setId(42);

        assertEquals("ID should be settable", 42, log.getId());
    }

    @Test
    public void shouldHandleLongMessages() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a long message part ").append(i).append(". ");
        }

        long created = System.currentTimeMillis() / 1000L;
        LogData log = new LogData(longMessage.toString(), created);

        assertEquals("Long message should be preserved", longMessage.toString(), log.getMessage());
    }

    @Test
    public void shouldHandleSpecialCharactersInMessage() {
        String messageWithSpecialChars = "Test with 日本語, émojis 🎉, and <html> tags & 'quotes'";
        long created = System.currentTimeMillis() / 1000L;

        LogData log = new LogData(messageWithSpecialChars, created);

        assertEquals("Special characters should be preserved", messageWithSpecialChars, log.getMessage());
    }

    @Test
    public void sameMessageDifferentTimeShouldBeDifferent() {
        String message = "Same message";
        long time1 = 1000L;
        long time2 = 2000L;

        LogData log1 = new LogData(message, time1);
        LogData log2 = new LogData(message, time2);

        // Same message but different timestamps - these should be treated as different entries
        assertEquals("Messages should be same", log1.getMessage(), log2.getMessage());
        assertNotEquals("Timestamps should differ", log1.getCreated(), log2.getCreated());
    }
}
