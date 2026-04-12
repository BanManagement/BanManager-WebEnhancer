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
        when(playerPinStorage.getValidPin(player)).thenReturn(pinData);
        when(pinData.getGeneratedPin()).thenReturn(123456);

        Message message = mock(Message.class);
        when(message.getRawTemplate()).thenReturn("Your login pin is: <pin>");

        listener.handlePin(player, message);

        verify(message).set("pin", "123456");
    }

    @Test
    public void handlePin_ignoresMessagesWithoutPlaceholder() {
        Message message = mock(Message.class);
        when(message.getRawTemplate()).thenReturn("You have been banned!");

        listener.handlePin(player, message);

        verify(playerPinStorage, never()).getValidPin(any());
        verify(message, never()).set(anyString(), anyString());
    }

    @Test
    public void handlePin_handlesNullPinGracefully() {
        when(playerPinStorage.getValidPin(player)).thenReturn(null);

        Message message = mock(Message.class);
        when(message.getRawTemplate()).thenReturn("Your login pin is: <pin>");

        listener.handlePin(player, message);

        verify(message, never()).set(anyString(), anyString());
    }

    @Test
    public void handlePin_handlesNullTemplateGracefully() {
        Message message = mock(Message.class);
        when(message.getRawTemplate()).thenReturn(null);

        listener.handlePin(player, message);

        verify(playerPinStorage, never()).getValidPin(any());
        verify(message, never()).set(anyString(), anyString());
    }
}
