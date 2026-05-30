package me.confuser.banmanager.webenhancer.common.listeners;

import me.confuser.banmanager.api.event.player.PlayerDeniedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.webenhancer.common.WebEnhancerPlugin;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.common.storage.PlayerPinStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the WebEnhancer listener mutates the API event's
 * {@link PlayerDeniedEvent#placeholders()} map rather than touching
 * BanManager's internal {@code Message} class — BanManager itself applies
 * those placeholders to the kick template before rendering.
 */
@ExtendWith(MockitoExtension.class)
public class CommonPlayerDeniedListenerTest {

    @Mock
    private WebEnhancerPlugin plugin;

    @Mock
    private BanManagerPlugin banManagerPlugin;

    @Mock
    private PlayerStorage playerStorage;

    @Mock
    private PlayerPinStorage playerPinStorage;

    @Mock
    private PlayerData player;

    @Mock
    private PlayerPinData pinData;

    private CommonPlayerDeniedListener listener;

    private final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private void wireMocks() throws Exception {
        lenient().when(plugin.getBanManagerPlugin()).thenReturn(banManagerPlugin);
        lenient().when(plugin.getPlayerPinStorage()).thenReturn(playerPinStorage);
        lenient().when(banManagerPlugin.getPlayerStorage()).thenReturn(playerStorage);
        lenient().when(playerStorage.queryForId(any(byte[].class))).thenReturn(player);
        listener = new CommonPlayerDeniedListener(plugin);
    }

    /** Reflection: the {@code onDenied} method is package-private. */
    private void invokeOnDenied(PlayerDeniedEvent event) throws Exception {
        Method m = CommonPlayerDeniedListener.class
            .getDeclaredMethod("onDenied", PlayerDeniedEvent.class);
        m.setAccessible(true);
        m.invoke(listener, event);
    }

    @Test
    public void onDenied_addsPinPlaceholder() throws Exception {
        wireMocks();
        when(playerPinStorage.getValidPin(player)).thenReturn(pinData);
        when(pinData.getGeneratedPin()).thenReturn(123456);

        Map<String, String> placeholders = new HashMap<>();
        PlayerDeniedEvent event = mock(PlayerDeniedEvent.class);
        when(event.uuid()).thenReturn(Optional.of(uuid));
        when(event.placeholders()).thenReturn(placeholders);

        invokeOnDenied(event);

        assertEquals("123456", placeholders.get("pin"));
    }

    @Test
    public void onDenied_skipsWhenNoUuid() throws Exception {
        wireMocks();

        Map<String, String> placeholders = new HashMap<>();
        PlayerDeniedEvent event = mock(PlayerDeniedEvent.class);
        when(event.uuid()).thenReturn(Optional.empty());

        invokeOnDenied(event);

        verify(playerPinStorage, never()).getValidPin(any());
        assertTrue(placeholders.isEmpty());
    }

    @Test
    public void onDenied_skipsWhenNoPin() throws Exception {
        wireMocks();
        when(playerPinStorage.getValidPin(player)).thenReturn(null);

        Map<String, String> placeholders = new HashMap<>();
        PlayerDeniedEvent event = mock(PlayerDeniedEvent.class);
        when(event.uuid()).thenReturn(Optional.of(uuid));
        when(event.placeholders()).thenReturn(placeholders);

        invokeOnDenied(event);

        assertTrue(placeholders.isEmpty());
    }
}
