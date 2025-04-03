package com.kitano.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class handles unauthorized access attempts by sending an error response.
 * It implements the AuthenticationEntryPoint interface from Spring Security.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * This method is called when an authentication exception occurs.
     * It sends a 401 Unauthorized response to the client.
     *
     * @param request       The HttpServletRequest object.
     * @param response      The HttpServletResponse object.
     * @param authException The AuthenticationException that occurred.
     * @throws IOException If an input or output error occurs.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        LOGGER.error("Unauthorized error: {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}
