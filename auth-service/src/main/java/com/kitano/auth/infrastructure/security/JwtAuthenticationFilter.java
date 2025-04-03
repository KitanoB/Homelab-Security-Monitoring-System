package com.kitano.auth.infrastructure.security;

import com.kitano.auth.infrastructure.repository.AuthUserJpaRepository;
import com.kitano.core.model.HomeLabUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * This class is a filter that checks for JWT tokens in incoming requests.
 * It validates the token and sets the authentication in the security context.
 * It extends OncePerRequestFilter to ensure it is executed once per request.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AuthUserJpaRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, AuthUserJpaRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    /**
     * This method is called for every request to check if the user is authenticated.
     * It extracts the JWT token from the request and validates it.
     *
     * @param request     The HttpServletRequest object.
     * @param response    The HttpServletResponse object.
     * @param filterChain The FilterChain object.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("JwtAuthenticationFilter triggered for: " + request.getRequestURI());
        String token = extractToken(request);

        // Validate the token and set the authentication in the context
        if (token != null) {
            if (jwtUtils.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token has been invalidated. \"}");
                return;
            }

            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                HomeLabUser user = userRepository.findByUsername(username);
                if (user != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.emptyList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * <p>
     * This method checks if the header is present and starts with "Bearer ".
     * If so, it extracts the token by removing the "Bearer " prefix.
     *
     * @param request The HttpServletRequest object.
     * @return The JWT token if present, null otherwise.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}