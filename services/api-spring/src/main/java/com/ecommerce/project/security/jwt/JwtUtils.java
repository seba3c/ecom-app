package com.ecommerce.project.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    @Autowired
    private JwtParser jwtParser;

    @Autowired
    private JwtValidator jwtValidator;

    @Autowired
    private JwtGenerator jwtGenerator;

    public String getJwtFromHeader(HttpServletRequest request) {
        return jwtParser.getJwtFromHeader(request);
    }

    public String getUserNameFromJwtToken(String token) {
        return jwtParser.getUserNameFromJwtToken(token);
    }

    public boolean validateJwtToken(String authToken) {
        return jwtValidator.validateJwtToken(authToken);
    }

    public String generateTokenFromUsername(String username) {
        return jwtGenerator.generateTokenFromUsername(username);
    }

    public String getJwtFromCookie(HttpServletRequest request) {
        return jwtParser.getJwtFromCookie(request);
    }

    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        return jwtGenerator.generateJwtCookie(userDetails);
    }

    public ResponseCookie generateJwtCleanCookie() {
        return jwtGenerator.generateJwtCleanCookie();
    }
}
