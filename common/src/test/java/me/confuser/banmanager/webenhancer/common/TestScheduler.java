package me.confuser.banmanager.webenhancer.common;

import me.confuser.banmanager.common.CommonScheduler;

import java.time.Duration;

/**
 * A test scheduler that runs tasks synchronously/inline for deterministic unit tests.
 * This allows testing async code without actual threading.
 */
public class TestScheduler implements CommonScheduler {

    @Override
    public void runAsync(Runnable task) {
        // Run inline for deterministic testing
        task.run();
    }

    @Override
    public void runAsyncLater(Runnable task, Duration delay) {
        // Ignore delay, run immediately for testing
        task.run();
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runSyncLater(Runnable task, Duration delay) {
        // Ignore delay, run immediately for testing
        task.run();
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
        // Just run once for testing
        task.run();
    }
}
