package com.example.customer.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 32+ char secret for HS256
        String secret = "12345678901234567890123456789012";
        jwtUtil = new JwtUtil(secret, 3600); // 1 hour TTL
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(100L, "test@example.com");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractEmailAndCustomerId() {
        String token = jwtUtil.generateToken(200L, "user@domain.com");

        assertEquals("user@domain.com", jwtUtil.extractEmail(token));
        assertEquals(200L, jwtUtil.extractCustomerId(token));
    }

    @Test
    void testInvalidTokenValidation() {
        assertFalse(jwtUtil.validateToken("this.is.fake.token"));
    }

//    @Test
//    void testExtractCustomerId_NullCid() {
//        // Create token without cid claim
//        String token = new io.jsonwebtoken.JwtBuilder() {
//            // Just a dummy builder, but we can do simpler:
//        };
//
//        String tokenWithoutCid = io.jsonwebtoken.Jwts.builder()
//                .setSubject("no-cid@example.com")
//                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
//                                "12345678901234567890123456789012".getBytes()),
//                        io.jsonwebtoken.SignatureAlgorithm.HS256)
//                .compact();
//
//        assertNull(jwtUtil.extractCustomerId(tokenWithoutCid));
//    }

    @Test
    void testExtractCustomerId_InvalidCidType() {
        // put string instead of number
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("string-cid@example.com")
                .claim("cid", "not-a-number")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                "12345678901234567890123456789012".getBytes()),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        assertNull(jwtUtil.extractCustomerId(token));
    }

    @Test
    void testConstructorRejectsInvalidSecret() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtUtil("shortSecret", 3600)
        );
        assertTrue(ex.getMessage().contains("JWT secret must be provided"));
    }
}
