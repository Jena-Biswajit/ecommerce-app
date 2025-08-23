package com.example.customer.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil();
    }

    @Test
    void testHashAndMatches_Success() {
        String raw = "mySecret123";
        String hashed = passwordUtil.hash(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed); // hashed must differ
        assertTrue(passwordUtil.matches(raw, hashed));
    }

    @Test
    void testMatches_Failure() {
        String raw = "mySecret123";
        String hashed = passwordUtil.hash(raw);

        assertFalse(passwordUtil.matches("wrongPass", hashed));
    }
}
