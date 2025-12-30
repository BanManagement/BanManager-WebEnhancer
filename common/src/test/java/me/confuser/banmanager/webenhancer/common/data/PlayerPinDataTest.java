package me.confuser.banmanager.webenhancer.common.data;

import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.Assert.*;
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
        assertTrue("Pin should be at least 100000", pin >= 100000);
        assertTrue("Pin should be at most 999999", pin <= 999999);
    }

    @Test
    public void shouldHashPinWithArgon2() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);
        String hashedPin = pinData.getPin();

        assertNotNull("Hashed pin should not be null", hashedPin);
        assertTrue("Pin should be hashed with Argon2i", hashedPin.startsWith("$argon2i$"));
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
        assertTrue("Expiry should be ~300 seconds from creation",
            expires >= beforeCreate + 299 && expires <= afterCreate + 301);
    }

    @Test
    public void shouldStoreGeneratedPinForDisplay() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        int generatedPin = pinData.getGeneratedPin();
        String hashedPin = pinData.getPin();

        // Generated pin is plaintext for display
        assertNotEquals("Generated pin should not be 0", 0, generatedPin);

        // Hashed pin is different from plaintext
        assertFalse("Hashed pin should not equal plaintext",
            hashedPin.equals(String.valueOf(generatedPin)));
    }

    @Test
    public void shouldStorePlayerReference() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        assertSame("Player reference should be stored", player, pinData.getPlayer());
    }

    @Test
    public void shouldHaveZeroIdBeforePersistence() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);

        assertEquals("ID should be 0 before persistence", 0, pinData.getId());
    }

    @Test
    public void shouldGenerateRandomPins() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pin1 = new PlayerPinData(player);
        PlayerPinData pin2 = new PlayerPinData(player);
        PlayerPinData pin3 = new PlayerPinData(player);

        // While there's a small chance of collision, it's unlikely with 900,000 possibilities
        // At least 2 of 3 should be different
        boolean atLeastTwoDifferent =
            pin1.getGeneratedPin() != pin2.getGeneratedPin() ||
            pin1.getGeneratedPin() != pin3.getGeneratedPin() ||
            pin2.getGeneratedPin() != pin3.getGeneratedPin();

        assertTrue("At least two of three pins should differ", atLeastTwoDifferent);
    }

    @Test
    public void shouldGenerateUniqueHashesForSamePin() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pin1 = new PlayerPinData(player);
        PlayerPinData pin2 = new PlayerPinData(player);

        // Even if by chance the generated pins are the same, the hashes should differ
        // due to unique random salts in Argon2
        assertNotEquals("Hashes should differ due to random salts",
            pin1.getPin(), pin2.getPin());
    }

    @Test
    public void generatedPinShouldBeSettable() throws NoSuchAlgorithmException {
        PlayerData player = mock(PlayerData.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        PlayerPinData pinData = new PlayerPinData(player);
        pinData.setGeneratedPin(999999);

        assertEquals("Generated pin should be settable", 999999, pinData.getGeneratedPin());
    }
}
