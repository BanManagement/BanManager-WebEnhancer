package me.confuser.banmanager.webenhancer.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Argon2PasswordEncoder.
 */
public class Argon2PasswordEncoderTest {

    private Argon2PasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        encoder = new Argon2PasswordEncoder();
    }

    @Test
    public void shouldEncodePassword() {
        String password = "123456";

        String encoded = encoder.encode(password);

        assertNotNull(encoded, "Encoded password should not be null");
        assertNotEquals(password, encoded, "Encoded password should differ from original");
    }

    @Test
    public void shouldProduceArgon2iFormat() {
        String password = "testpassword";

        String encoded = encoder.encode(password);

        // Argon2i format starts with $argon2i$
        assertTrue(encoded.startsWith("$argon2i$"), "Hash should use Argon2i format");
    }

    @Test
    public void shouldGenerateUniqueSaltsForSameInput() {
        String password = "samepassword";

        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        assertNotEquals(encoded1, encoded2, "Hashes should differ due to unique salts");
    }

    @Test
    public void shouldProduceCorrectHashLength() {
        String password = "test";

        String encoded = encoder.encode(password);

        assertTrue(encoded.length() > 50, "Hash string should be substantial");
    }

    @Test
    public void shouldHandleEmptyPassword() {
        String password = "";

        String encoded = encoder.encode(password);

        assertNotNull(encoded, "Should handle empty password");
        assertTrue(encoded.startsWith("$argon2i$"), "Should still produce Argon2i format");
    }

    @Test
    public void shouldHandleLongPassword() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("a");
        }

        String encoded = encoder.encode(longPassword.toString());

        assertNotNull(encoded, "Should handle long password");
        assertTrue(encoded.startsWith("$argon2i$"), "Should still produce Argon2i format");
    }

    @Test
    public void shouldHandleSpecialCharacters() {
        String password = "p@$$w0rd!#$%^&*()日本語";

        String encoded = encoder.encode(password);

        assertNotNull(encoded, "Should handle special characters");
        assertTrue(encoded.startsWith("$argon2i$"), "Should still produce Argon2i format");
    }

    @Test
    public void shouldHandleNumericPin() {
        // This is the most common use case - 6 digit PINs
        String pin = "123456";

        String encoded = encoder.encode(pin);

        assertNotNull(encoded, "Should handle numeric PIN");
        assertTrue(encoded.startsWith("$argon2i$"), "Should produce Argon2i format");

        // Verify format contains expected parameters
        assertTrue(encoded.contains("v="), "Should contain version");
        assertTrue(encoded.contains("m="), "Should contain memory parameter");
        assertTrue(encoded.contains("t="), "Should contain time parameter");
        assertTrue(encoded.contains("p="), "Should contain parallelism parameter");
    }

    @Test
    public void shouldProduceConsistentFormatAcrossMultipleCalls() {
        Argon2PasswordEncoder encoder2 = new Argon2PasswordEncoder();

        String encoded1 = encoder.encode("test");
        String encoded2 = encoder2.encode("test");

        String format1 = encoded1.substring(0, encoded1.indexOf("$", 9));

        String format2 = encoded2.substring(0, encoded2.indexOf("$", 9));

        assertEquals(format1, format2, "Both instances should produce same format");
    }
}
