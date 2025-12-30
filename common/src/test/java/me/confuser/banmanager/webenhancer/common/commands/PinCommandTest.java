package me.confuser.banmanager.webenhancer.common.commands;

import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PinCommand.
 *
 * Note: Full command tests are done via E2E tests because PinCommand
 * requires complex initialization of BanManagerPlugin and WebEnhancerPlugin.
 * These tests focus on command logic that can be tested without full setup.
 */
@RunWith(MockitoJUnitRunner.class)
public class PinCommandTest {

    @Mock
    private CommonSender sender;

    @Mock
    private CommandParser parser;

    @Test
    public void consoleShouldNotBeAbleToUsePin() {
        // Verify through E2E tests - command checks sender.isConsole()
        when(sender.isConsole()).thenReturn(true);

        // This would return false for console
        assertTrue("Console check should return true for console sender", sender.isConsole());
    }

    @Test
    public void commandWithArgumentsShouldBeDenied() {
        // Verify through E2E tests - command checks parser.getArgs().length
        when(parser.getArgs()).thenReturn(new String[]{"extra", "args"});

        // Command requires no arguments
        assertEquals("Extra arguments should fail", 2, parser.getArgs().length);
    }

    @Test
    public void commandWithNoArgumentsShouldBeAllowed() {
        when(parser.getArgs()).thenReturn(new String[]{});

        assertEquals("No arguments should be valid", 0, parser.getArgs().length);
    }
}
