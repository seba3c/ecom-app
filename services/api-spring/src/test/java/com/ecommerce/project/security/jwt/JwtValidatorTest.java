package com.ecommerce.project.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtValidatorTest {

    private static final String SECRET = "6d5f7d8e9a2b3c4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9";

    @InjectMocks
    private JwtValidator jwtValidator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtValidator, "jwtSecret", SECRET);
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    private String buildValidToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey())
                .compact();
    }

    @Test
    void validateJwtToken_validToken_returnsTrue() {
        assertTrue(jwtValidator.validateJwtToken(buildValidToken("alice")));
    }

    @Test
    void validateJwtToken_malformedToken_returnsFalse() {
        assertFalse(jwtValidator.validateJwtToken("this.is.not.a.jwt"));
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        String expiredToken = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey())
                .compact();

        assertFalse(jwtValidator.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_emptyString_returnsFalse() {
        assertFalse(jwtValidator.validateJwtToken(""));
    }
}
