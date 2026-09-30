package com.ecommerce.project.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private JwtParser jwtParser;

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private JwtGenerator jwtGenerator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtils jwtUtils;

    @Test
    void getJwtFromHeader_delegatesToJwtParser() {
        when(jwtParser.getJwtFromHeader(request)).thenReturn("sometoken");

        String result = jwtUtils.getJwtFromHeader(request);

        assertEquals("sometoken", result);
        verify(jwtParser).getJwtFromHeader(request);
    }

    @Test
    void getUserNameFromJwtToken_delegatesToJwtParser() {
        when(jwtParser.getUserNameFromJwtToken("token")).thenReturn("alice");

        String result = jwtUtils.getUserNameFromJwtToken("token");

        assertEquals("alice", result);
        verify(jwtParser).getUserNameFromJwtToken("token");
    }

    @Test
    void validateJwtToken_delegatesToJwtValidator() {
        when(jwtValidator.validateJwtToken("token")).thenReturn(true);

        boolean result = jwtUtils.validateJwtToken("token");

        assertTrue(result);
        verify(jwtValidator).validateJwtToken("token");
    }

    @Test
    void generateTokenFromUsername_delegatesToJwtGenerator() {
        when(jwtGenerator.generateTokenFromUsername("bob")).thenReturn("generated-token");

        String result = jwtUtils.generateTokenFromUsername("bob");

        assertEquals("generated-token", result);
        verify(jwtGenerator).generateTokenFromUsername("bob");
    }

    @Test
    void getJwtFromCookie_delegatesToJwtParser() {
        when(jwtParser.getJwtFromCookie(request)).thenReturn("cookie-token");

        String result = jwtUtils.getJwtFromCookie(request);

        assertEquals("cookie-token", result);
        verify(jwtParser).getJwtFromCookie(request);
    }

    @Test
    void generateJwtCookie_delegatesToJwtGenerator() {
        UserDetails userDetails = new User("bob", "pass", Collections.emptyList());
        ResponseCookie cookie =
                ResponseCookie.from("ecommerce-app", "jwt-value").build();
        when(jwtGenerator.generateJwtCookie(userDetails)).thenReturn(cookie);

        ResponseCookie result = jwtUtils.generateJwtCookie(userDetails);

        assertEquals(cookie, result);
        verify(jwtGenerator).generateJwtCookie(userDetails);
    }

    @Test
    void generateJwtCleanCookie_delegatesToJwtGenerator() {
        ResponseCookie cleanCookie =
                ResponseCookie.from("ecommerce-app", null).path("/api").build();
        when(jwtGenerator.generateJwtCleanCookie()).thenReturn(cleanCookie);

        ResponseCookie result = jwtUtils.generateJwtCleanCookie();

        assertEquals(cleanCookie, result);
        verify(jwtGenerator).generateJwtCleanCookie();
    }
}
