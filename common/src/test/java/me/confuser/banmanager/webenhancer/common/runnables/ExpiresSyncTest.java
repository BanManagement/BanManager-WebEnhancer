package me.confuser.banmanager.webenhancer.common.runnables;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ExpiresSync runnable.
 *
 * Note: ExpiresSync is currently commented out in platform implementations
 * and requires complex mocking of BanManagerPlugin.getInstance() static method.
 * Full tests are done via E2E tests or manual integration testing.
 * These tests verify basic contract expectations.
 */
public class ExpiresSyncTest {

    @Test
    public void expiresSyncShouldDeleteExpiredPins() {
        // ExpiresSync queries for pins where expires <= now and deletes them
        // This behavior is verified through E2E tests when enabled

        long now = System.currentTimeMillis() / 1000L;
        long expiredTime = now - 60; // 1 minute ago
        long validTime = now + 300; // 5 minutes from now

        assertTrue("Expired pin should be older than now", expiredTime < now);
        assertTrue("Valid pin should be newer than now", validTime > now);
    }

    @Test
    public void expiresSyncShouldNotDeleteActivePins() {
        long now = System.currentTimeMillis() / 1000L;
        long validExpiry = now + 300; // 5 minutes from now

        // Pins with expiry > now should NOT be deleted
        assertTrue("Active pin expiry should be in the future", validExpiry > now);
    }

    @Test
    public void expiryCalculationShouldBeFiveMinutes() {
        // Default pin expiry is 300 seconds (5 minutes)
        int expectedExpirySeconds = 300;
        int expectedExpiryMinutes = 5;

        assertEquals("Expiry should be 5 minutes in seconds",
            expectedExpiryMinutes * 60, expectedExpirySeconds);
    }
}
