package com.example.customer.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-seconds}") long ttlSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be provided and at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = ttlSeconds * 1000L;
    }

    // customerId first, email second — unambiguous
    public String generateToken(Long customerId, String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)        // email as subject
                .claim("cid", customerId) // numeric id claim named "cid"
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Long extractCustomerId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object cid = claims.get("cid");
        if (cid == null) return null;
        if (cid instanceof Number) return ((Number) cid).longValue();
        try {
            return Long.valueOf(cid.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
