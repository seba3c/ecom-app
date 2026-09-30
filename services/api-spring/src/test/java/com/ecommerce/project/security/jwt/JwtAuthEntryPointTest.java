package com.ecommerce.project.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JwtAuthEntryPointTest {

    @InjectMocks
    private JwtAuthEntryPoint entryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Test
    @SuppressWarnings("unchecked")
    void commence_setsUnauthorizedStatusAndJsonBody() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(request.getServletPath()).thenReturn("/api/products");
        when(authException.getMessage()).thenReturn("Full authentication is required");
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        });

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        Map<String, Object> body = new ObjectMapper().readValue(outputStream.toByteArray(), Map.class);
        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Full authentication is required", body.get("message"));
        assertEquals("/api/products", body.get("path"));
    }
}
