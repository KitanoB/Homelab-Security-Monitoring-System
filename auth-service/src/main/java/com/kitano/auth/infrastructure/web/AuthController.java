package com.kitano.auth.infrastructure.web;

import com.kitano.auth.application.AuthService;
import com.kitano.auth.model.HomelabUserCreateDTO;
import com.kitano.auth.model.HomelabUserDTO;
import com.kitano.auth.model.UserLoginDTO;
import com.kitano.core.model.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}