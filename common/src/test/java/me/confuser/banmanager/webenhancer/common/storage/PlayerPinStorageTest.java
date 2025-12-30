package me.confuser.banmanager.webenhancer.common.storage;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.webenhancer.common.data.PlayerPinData;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerPinStorage.
 *
 * Note: Full integration tests requiring database are in E2E tests.
 * These tests focus on the storage behavior that can be tested with mocks.
 */
public class PlayerPinStorageTest {

    @Test
    public void shouldGenerateSixDigitPin() throws Exception {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        // Pin should be 6 digits (100000-999999)
        int pin = pinData.getGeneratedPin();
        assertTrue("Pin should be at least 100000", pin >= 100000);
        assertTrue("Pin should be at most 999999", pin <= 999999);
    }

    @Test
    public void shouldHashPinWithArgon2() throws Exception {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        // The stored pin should be an Argon2 hash
        String hashedPin = pinData.getPin();
        assertNotNull("Hashed pin should not be null", hashedPin);
        assertTrue("Pin should be hashed with Argon2", hashedPin.startsWith("$argon2"));
    }

    @Test
    public void shouldSetExpiryFiveMinutesFromNow() throws Exception {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        long beforeCreate = System.currentTimeMillis() / 1000L;
        PlayerPinData pinData = new PlayerPinData(player);
        long afterCreate = System.currentTimeMillis() / 1000L;

        // Expiry should be 300 seconds (5 minutes) from creation
        long expires = pinData.getExpires();
        assertTrue("Expiry should be at least 300 seconds from before", expires >= beforeCreate + 300);
        assertTrue("Expiry should be at most 300 seconds from after", expires <= afterCreate + 300);
    }

    @Test
    public void shouldStoreGeneratedPinForDisplay() throws Exception {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        // generatedPin should be set for display to user
        int generatedPin = pinData.getGeneratedPin();
        assertNotEquals("Generated pin should not be 0", 0, generatedPin);

        // The hashed pin and generated pin should be related (generated is plaintext, pin is hash)
        assertFalse("Hashed pin should not equal generated pin string",
            pinData.getPin().equals(String.valueOf(generatedPin)));
    }

    @Test
    public void shouldGenerateUniquePinsForDifferentPlayers() throws Exception {
        PlayerData player1 = mock(PlayerData.class);
        PlayerData player2 = mock(PlayerData.class);
        when(player1.getUUID()).thenReturn(UUID.randomUUID());
        when(player2.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pin1 = new PlayerPinData(player1);
        PlayerPinData pin2 = new PlayerPinData(player2);

        // While not guaranteed to be different (random), hashes should differ
        // due to random salt in Argon2
        assertNotEquals("Hashes should differ due to unique salts",
            pin1.getPin(), pin2.getPin());
    }
}
