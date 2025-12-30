package me.confuser.banmanager.webenhancer.common.security;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Argon2PasswordEncoder.
 */
public class Argon2PasswordEncoderTest {

    private Argon2PasswordEncoder encoder;

    @Before
    public void setUp() {
        encoder = new Argon2PasswordEncoder();
    }

    @Test
    public void shouldEncodePassword() {
        String password = "123456";

        String encoded = encoder.encode(password);

        assertNotNull("Encoded password should not be null", encoded);
        assertNotEquals("Encoded password should differ from original", password, encoded);
    }

    @Test
    public void shouldProduceArgon2iFormat() {
        String password = "testpassword";

        String encoded = encoder.encode(password);

        // Argon2i format starts with $argon2i$
        assertTrue("Hash should use Argon2i format", encoded.startsWith("$argon2i$"));
    }

    @Test
    public void shouldGenerateUniqueSaltsForSameInput() {
        String password = "samepassword";

        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        assertNotEquals("Hashes should differ due to unique salts", encoded1, encoded2);
    }

    @Test
    public void shouldProduceCorrectHashLength() {
        String password = "test";

        String encoded = encoder.encode(password);

        assertTrue("Hash string should be substantial", encoded.length() > 50);
    }

    @Test
    public void shouldHandleEmptyPassword() {
        String password = "";

        String encoded = encoder.encode(password);

        assertNotNull("Should handle empty password", encoded);
        assertTrue("Should still produce Argon2i format", encoded.startsWith("$argon2i$"));
    }

    @Test
    public void shouldHandleLongPassword() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("a");
        }

        String encoded = encoder.encode(longPassword.toString());

        assertNotNull("Should handle long password", encoded);
        assertTrue("Should still produce Argon2i format", encoded.startsWith("$argon2i$"));
    }

    @Test
    public void shouldHandleSpecialCharacters() {
        String password = "p@$$w0rd!#$%^&*()日本語";

        String encoded = encoder.encode(password);

        assertNotNull("Should handle special characters", encoded);
        assertTrue("Should still produce Argon2i format", encoded.startsWith("$argon2i$"));
    }

    @Test
    public void shouldHandleNumericPin() {
        // This is the most common use case - 6 digit PINs
        String pin = "123456";

        String encoded = encoder.encode(pin);

        assertNotNull("Should handle numeric PIN", encoded);
        assertTrue("Should produce Argon2i format", encoded.startsWith("$argon2i$"));

        // Verify format contains expected parameters
        assertTrue("Should contain version", encoded.contains("v="));
        assertTrue("Should contain memory parameter", encoded.contains("m="));
        assertTrue("Should contain time parameter", encoded.contains("t="));
        assertTrue("Should contain parallelism parameter", encoded.contains("p="));
    }

    @Test
    public void shouldProduceConsistentFormatAcrossMultipleCalls() {
        Argon2PasswordEncoder encoder2 = new Argon2PasswordEncoder();

        String encoded1 = encoder.encode("test");
        String encoded2 = encoder2.encode("test");

        String format1 = encoded1.substring(0, encoded1.indexOf("$", 9));

        String format2 = encoded2.substring(0, encoded2.indexOf("$", 9));

        assertEquals("Both instances should produce same format", format1, format2);
    }
}
