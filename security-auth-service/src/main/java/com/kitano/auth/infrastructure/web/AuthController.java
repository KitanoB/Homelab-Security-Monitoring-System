package com.kitano.auth.infrastructure.web;

import com.kitano.auth.application.AuthService;
import com.kitano.core.model.HomeLabUser;
import com.kitano.core.model.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService eventService;

    public AuthController(AuthService eventService) {
        this.eventService = eventService;
    }

    /**
     * Logs a new security event.
     *
     * @param user The security event to log.
     * @return The logged event with HTTP 201 Created.
     */
    @PostMapping("/login")
    public ResponseEntity<HomeLabUser> login(@RequestBody HomeLabUser user) {
        LOGGER.info("Received request to log event: {}", user);

        try {
            HomeLabUser savedUser = eventService.authenticate(user);
            return ResponseEntity.status(201).body(savedUser);
        } catch (SystemException e) {
            LOGGER.error("Error logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<HomeLabUser> signin(@RequestBody HomeLabUser user) {
        LOGGER.info("Received request to log event: {}", user);

        try {
            HomeLabUser savedUser = eventService.authenticate(user);
            return ResponseEntity.status(201).body(savedUser);
        } catch (SystemException e) {
            LOGGER.error("Error logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<HomeLabUser> signout(@RequestBody HomeLabUser user) {
        LOGGER.info("Received request to log event: {}", user);

        try {
            HomeLabUser savedUser = eventService.authenticate(user);
            return ResponseEntity.status(201).body(savedUser);
        } catch (SystemException e) {
            LOGGER.error("Error logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ban")
    public ResponseEntity<HomeLabUser> ban(@RequestBody HomeLabUser user) {
        LOGGER.info("Received request to log event: {}", user);

        try {
            HomeLabUser savedUser = eventService.authenticate(user);
            return ResponseEntity.status(201).body(savedUser);
        } catch (SystemException e) {
            LOGGER.error("Error logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
