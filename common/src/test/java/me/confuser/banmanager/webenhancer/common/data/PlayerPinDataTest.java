package me.confuser.banmanager.webenhancer.common.data;

import me.confuser.banmanager.common.data.PlayerData;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerPinData.
 */
public class PlayerPinDataTest {

    @Test
    public void shouldGenerateSixDigitPin() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);
        int pin = pinData.getGeneratedPin();

        // 6 digit pin: 100000-999999
        assertTrue(pin >= 100000, "Pin should be at least 100000");
        assertTrue(pin <= 999999, "Pin should be at most 999999");
    }

    @Test
    public void shouldHashPinWithArgon2() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);
        String hashedPin = pinData.getPin();

        assertNotNull(hashedPin, "Hashed pin should not be null");
        assertTrue(hashedPin.startsWith("$argon2i$"), "Pin should be hashed with Argon2i");
    }

    @Test
    public void shouldSetExpiryFiveMinutesFromNow() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        long beforeCreate = System.currentTimeMillis() / 1000L;
        PlayerPinData pinData = new PlayerPinData(player);
        long afterCreate = System.currentTimeMillis() / 1000L;

        // Expiry should be 300 seconds (5 minutes) from creation
        long expires = pinData.getExpires();

        // Allow 1 second tolerance for test execution
        assertTrue(expires >= beforeCreate + 299 && expires <= afterCreate + 301, "Expiry should be ~300 seconds from creation");
    }

    @Test
    public void shouldStoreGeneratedPinForDisplay() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        int generatedPin = pinData.getGeneratedPin();
        String hashedPin = pinData.getPin();

        assertNotEquals(0, generatedPin, "Generated pin should not be 0");
        assertFalse(hashedPin.equals(String.valueOf(generatedPin)), "Hashed pin should not equal plaintext");
    }

    @Test
    public void shouldStorePlayerReference() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        assertSame(player, pinData.getPlayer(), "Player reference should be stored");
    }

    @Test
    public void shouldHaveZeroIdBeforePersistence() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        assertEquals(0, pinData.getId(), "ID should be 0 before persistence");
    }

    @Test
    public void shouldGenerateRandomPins() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pin1 = new PlayerPinData(player);
        PlayerPinData pin2 = new PlayerPinData(player);
        PlayerPinData pin3 = new PlayerPinData(player);

        boolean atLeastTwoDifferent =
            pin1.getGeneratedPin() != pin2.getGeneratedPin() ||
            pin1.getGeneratedPin() != pin3.getGeneratedPin() ||
            pin2.getGeneratedPin() != pin3.getGeneratedPin();

        assertTrue(atLeastTwoDifferent, "At least two of three pins should differ");
    }

    @Test
    public void shouldGenerateUniqueHashesForSamePin() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pin1 = new PlayerPinData(player);
        PlayerPinData pin2 = new PlayerPinData(player);

        // Even if by chance the generated pins are the same, the hashes should differ
        // due to unique random salts in Argon2
        assertNotEquals(pin1.getPin(), pin2.getPin(), "Hashes should differ due to random salts");
    }

    @Test
    public void generatedPinShouldBeSettable() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);
        pinData.setGeneratedPin(999999);

        assertEquals(999999, pinData.getGeneratedPin(), "Generated pin should be settable");
    }
}
