package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test void hash_isDeterministic() {
        assertEquals(PasswordHasher.hash("pw"), PasswordHasher.hash("pw"));
    }
    @Test void hash_isDifferentForDifferentInputs() {
        assertNotEquals(PasswordHasher.hash("pw1"), PasswordHasher.hash("pw2"));
    }
    @Test void hash_producesHex64Chars() {
        assertEquals(64, PasswordHasher.hash("anything").length());
    }
    @Test void verify_acceptsCorrectPassword() {
        String h = PasswordHasher.hash("secret");
        assertTrue(PasswordHasher.verify("secret", h));
    }
    @Test void verify_rejectsWrongPassword() {
        String h = PasswordHasher.hash("secret");
        assertFalse(PasswordHasher.verify("wrong", h));
    }
    @Test void hash_matchesSeedAdminHash() {
        // Must match the hash stored in db/schema.sql for 'admin123'
        assertEquals("240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                     PasswordHasher.hash("admin123"));
    }
    @Test void hash_matchesSeedPassHash() {
        // Must match the hash stored in db/schema.sql for 'pass123'
        assertEquals("9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c",
                     PasswordHasher.hash("pass123"));
    }
}
