package com.kitano.auth.infrastructure.web;

import com.kitano.auth.application.AuthService;
import com.kitano.auth.infrastructure.proxy.SecurityServiceClient;
import com.kitano.auth.infrastructure.repository.AuthUserJpaRepository;
import com.kitano.auth.infrastructure.security.JwtUtils;
import com.kitano.auth.model.HomelabUserCreateDTO;
import com.kitano.auth.model.HomelabUserDTO;
import com.kitano.auth.model.UserLoginDTO;
import com.kitano.core.model.HomeLabUser;
import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final SecurityServiceClient securityClient;
    private final JwtUtils jwtUtils;
    private final AuthUserJpaRepository authUserJpaRepository;

    public AuthController(AuthService authService, SecurityServiceClient securityClient, JwtUtils jwtUtils, AuthUserJpaRepository authUserJpaRepository) {
        this.authService = authService;
        this.securityClient = securityClient;
        this.jwtUtils = jwtUtils;
        this.authUserJpaRepository = authUserJpaRepository;
    }

    /**
     * Handle login request and return a JWT if credentials are valid.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        loginRequest.setIpAddress(ipAddress);
        try {
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(token);
        } catch (SystemException e) {
            LOGGER.warn("Login failed for user {} from IP {}: {}", loginRequest.getUsername(), ipAddress, e.getMessage());
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody HomelabUserCreateDTO createDTO, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        createDTO.setIpAddress(ipAddress);
        try {
            HomelabUserDTO registeredUser = authService.register(createDTO);
            return ResponseEntity.status(201).body(registeredUser);
        } catch (SystemException e) {
            LOGGER.warn("Registration failed from IP {}: {}", ipAddress, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Log the user out by invalidating the token.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("Logout request from IP: {}", request.getRemoteAddr());
        try {
            ResponseEntity<?> responseEntity = authService.logout(request, response);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Logout successful");
            } else {
                return ResponseEntity.status(responseEntity.getStatusCode()).body("Logout failed");
            }
        } catch (Exception e) {
            LOGGER.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Logout failed");
        }
    }

    /**
     * Fetch all events from security-service.
     */
    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents(HttpServletRequest request) {
        LOGGER.info("Fetching all events from security-service");
        if (!isAdmin(request)) {
            return ResponseEntity.badRequest().body("Role ADMIN required");
        }
        try {
            List<SystemEvent> events = securityClient.getAllEvents();
            return events.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(events);
        } catch (Exception e) {
            LOGGER.error("Error while fetching events: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Check if the user is allowed to access the events.
     * To be allowed, the user must be authenticated and have the role "ADMIN".
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        boolean isAdmin = false;
        if (authentication != null && authentication.isAuthenticated()) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                username = jwtUtils.getUsernameFromToken(token);
                HomeLabUser user = authUserJpaRepository.findByUsername(username);
                isAdmin = user != null && KtxRole.ADMIN.equals(user.getRole());
            }
        }
        return username != null && isAdmin;
    }
}