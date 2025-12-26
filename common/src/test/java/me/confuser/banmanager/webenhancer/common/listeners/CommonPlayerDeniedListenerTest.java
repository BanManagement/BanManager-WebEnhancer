package me.confuser.banmanager.webenhancer.common.listeners;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommonPlayerDeniedListenerTest {

    @Mock
    private WebEnhancerPlugin plugin;

    @Mock
    private PlayerPinStorage playerPinStorage;

    @Mock
    private PlayerData player;

    @Mock
    private PlayerPinData pinData;

    private CommonPlayerDeniedListener listener;

    @Before
    public void setUp() {
        when(plugin.getPlayerPinStorage()).thenReturn(playerPinStorage);
        listener = new CommonPlayerDeniedListener(plugin);
    }

    @Test
    public void handlePin_replacesPlaceholderWithPin() {
        // Setup
        when(playerPinStorage.getValidPin(player)).thenReturn(pinData);
        when(pinData.getGeneratedPin()).thenReturn(123456);

        // Create a mocked message with [pin] placeholder
        Message message = mock(Message.class);
        when(message.toString()).thenReturn("Your login pin is: [pin]");

        // Execute
        listener.handlePin(player, message);

        // Verify - message.set() should be called with the pin
        verify(message).set("pin", "123456");
    }

    @Test
    public void handlePin_ignoresMessagesWithoutPlaceholder() {
        // Create a mocked message without [pin] placeholder
        Message message = mock(Message.class);
        when(message.toString()).thenReturn("You have been banned!");

        // Execute
        listener.handlePin(player, message);

        // Verify - getValidPin should NOT be called since message doesn't contain [pin]
        verify(playerPinStorage, never()).getValidPin(any());

        // Verify set was never called
        verify(message, never()).set(anyString(), anyString());
    }

    @Test
    public void handlePin_handlesNullPinGracefully() {
        // Setup - getValidPin returns null
        when(playerPinStorage.getValidPin(player)).thenReturn(null);

        // Create a mocked message with [pin] placeholder
        Message message = mock(Message.class);
        when(message.toString()).thenReturn("Your login pin is: [pin]");

        // Execute - should not throw
        listener.handlePin(player, message);

        // Verify - set should NOT be called when pin is null
        verify(message, never()).set(anyString(), anyString());
    }
}
