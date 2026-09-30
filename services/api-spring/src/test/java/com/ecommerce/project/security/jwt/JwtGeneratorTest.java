package com.ecommerce.project.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Collections;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtGeneratorTest {

    private static final String SECRET = "6d5f7d8e9a2b3c4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9";
    private static final int EXPIRATION_MS = 86400000;
    private static final String COOKIE_NAME = "ecommerce-app";
    private static final long COOKIE_EXPIRATION_SEC = 86400L;

    @InjectMocks
    private JwtGenerator jwtGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtGenerator, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtGenerator, "jwtExpirationMs", EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtGenerator, "jwtCookie", COOKIE_NAME);
        ReflectionTestUtils.setField(jwtGenerator, "jwtCookieExpiration", COOKIE_EXPIRATION_SEC);
    }

    @Test
    void generateTokenFromUsername_returnsNonEmptyToken() {
        String token = jwtGenerator.generateTokenFromUsername("bob");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateTokenFromUsername_tokenContainsCorrectUsername() {
        String token = jwtGenerator.generateTokenFromUsername("bob");

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Claims claims =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        assertEquals("bob", claims.getSubject());
    }

    @Test
    void generateJwtCookie_returnsCorrectCookieAttributes() {
        UserDetails userDetails = new User("bob", "password", Collections.emptyList());

        ResponseCookie cookie = jwtGenerator.generateJwtCookie(userDetails);

        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals("/api", cookie.getPath());
        assertEquals(COOKIE_EXPIRATION_SEC, cookie.getMaxAge().getSeconds());
        assertFalse(cookie.getValue().isBlank());
    }

    @Test
    void generateJwtCleanCookie_returnsNullValueCookie() {
        ResponseCookie cookie = jwtGenerator.generateJwtCleanCookie();

        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals("/api", cookie.getPath());
    }

    @Test
    void generateJwtCleanCookie_hasCorrectPath() {
        ResponseCookie cookie = jwtGenerator.generateJwtCleanCookie();

        assertEquals("/api", cookie.getPath());
    }
}
