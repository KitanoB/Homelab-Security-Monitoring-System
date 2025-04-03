package com.kitano.auth.application;

import com.kitano.auth.infrastructure.messaging.AuthProducer;
import com.kitano.auth.infrastructure.repository.AuthEventJpaRepository;
import com.kitano.auth.infrastructure.repository.AuthUserJpaRepository;
import com.kitano.auth.infrastructure.security.JwtUtils;
import com.kitano.auth.infrastructure.security.PasswordService;
import com.kitano.auth.model.HomelabUserCreateDTO;
import com.kitano.auth.model.HomelabUserDTO;
import com.kitano.auth.model.UserLoginDTO;
import com.kitano.auth.model.UserMapper;
import com.kitano.core.model.HomeLabUser;
import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import com.kitano.iface.model.KtxRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for handling authentication-related operations.
 * <p>
 * This class provides methods for user login, registration, and logout.
 * It also handles event logging for authentication actions.
 */
@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AuthUserJpaRepository authUserJpaRepository;
    private final AuthEventJpaRepository authEventJpaRepository;
    private final PasswordService passwordService;
    private final JwtUtils jwtUtils;
    private final AuthProducer producer;

    public AuthService(AuthUserJpaRepository authUserJpaRepository,
                       AuthEventJpaRepository authEventJpaRepository,
                       PasswordService passwordService,
                       JwtUtils jwtUtils,
                       AuthProducer producer) {
        this.authUserJpaRepository = authUserJpaRepository;
        this.authEventJpaRepository = authEventJpaRepository;
        this.passwordService = passwordService;
        this.jwtUtils = jwtUtils;
        this.producer = producer;
    }

    /**
     * Logs in a user by validating their credentials and generating a JWT token.
     *
     * @param loginRequest the login request containing username and password
     * @return a JWT token if login is successful
     * @throws SystemException if the user is not found or credentials are invalid
     */

    @Transactional
    public String login(UserLoginDTO loginRequest) throws SystemException {
        LOGGER.info("Login attempt for user {}", loginRequest.getUsername());

        HomeLabUser user = authUserJpaRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            LOGGER.error("User not found: {}", loginRequest.getUsername());
            logEvent(null, KtxEvent.EventType.AUTHENTICATION_FAILURE, "User not found", loginRequest.getIpAddress());
            throw new SystemException("User not found");
        }

        if (!passwordService.matches(loginRequest.getPassword(), user.getPassword())) {
            LOGGER.error("Invalid credentials for user {}", loginRequest.getUsername());
            logEvent(user, KtxEvent.EventType.AUTHENTICATION_FAILURE, "Bad credentials", loginRequest.getIpAddress());
            throw new SystemException("Invalid credentials");
        } else if (user.isBan()) {
            LOGGER.error("User {} is banned", loginRequest.getUsername());
            logEvent(user, KtxEvent.EventType.AUTHENTICATION_FAILURE, "User is banned", loginRequest.getIpAddress());
            throw new SystemException("User is banned");
        }

        logEvent(user, KtxEvent.EventType.AUTHENTICATION_SUCCESS, "Login successful", loginRequest.getIpAddress());

        LOGGER.info("User {} logged in", loginRequest.getUsername());
        return jwtUtils.generateToken(UserMapper.toDto(user));
    }

    /**
     * Registers a new user by saving their credentials and generating an event.
     *
     * @param dto the user creation DTO containing username and password
     * @return the registered user DTO
     * @throws SystemException if the username is already in use
     */
    @Transactional
    public HomelabUserDTO register(HomelabUserCreateDTO dto) throws SystemException {

        LOGGER.info("Registering user {}", dto.getUsername());
        boolean exists = authUserJpaRepository.existsByUsername(dto.getUsername());
        if (exists) {
            LOGGER.error("Username already in use: {}", dto.getUsername());
            throw new SystemException("Username already in use");
        }

        // Check if the IP address is already in use
        // and if the user is banned
        if (authUserJpaRepository.existByIpAddress(dto.getIpAddress())) {
            HomeLabUser user = authUserJpaRepository.findByIpAddress(dto.getIpAddress());
            if (user != null && user.isBan()) {
                LOGGER.error("User with IP {} is banned", dto.getIpAddress());
                throw new SystemException("User with this IP is banned");
            } else if (user != null) {
                LOGGER.error("User with IP {} already exists", dto.getIpAddress());
                throw new SystemException("User with this IP already exists");
            }
        }


        HomeLabUser user = new HomeLabUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordService.hash(dto.getPassword()));
        user.setRole(KtxRole.USER);
        user.setCreated(LocalDateTime.now());

        HomeLabUser saved = authUserJpaRepository.save(user);

        logEvent(saved, KtxEvent.EventType.USER_ACTION, "User created", dto.getIpAddress());

        LOGGER.info("User {} registered", dto.getUsername());
        return UserMapper.toDto(saved);
    }

    /**
     * Logs out the user by invalidating the JWT token and clearing the authentication context.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @throws SystemException if the user is not authenticated
     */
    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws SystemException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                throw new SystemException("Invalid token format");
            }
            String username = jwtUtils.getUsernameFromToken(token);
            HomeLabUser user = authUserJpaRepository.findByUsername(username);
            jwtUtils.blacklist(token);
            if (user != null) {
                logEvent(user, KtxEvent.EventType.USER_ACTION, "User logged out", request.getRemoteAddr());
            } else {
                LOGGER.warn("User not found during logout: {}", username);
            }
            // Invalidate the token (if applicable)
            // This could involve adding the token to a blacklist or similar mechanism
            // For now, we just log the event
            LOGGER.info("User {} logged out", username);
            // Clear the authentication context
            SecurityContextHolder.clearContext();
            // Optionally, you can also invalidate the session
            request.getSession().invalidate();


            // Returns a successful response indicating that the logout process was completed
            return ResponseEntity.ok("Logout successful");
        } else {
            // Throws an exception if the user was not authenticated in the first place
            throw new SystemException("User not authenticated");
        }
    }

    /**
     * Logs an event related to authentication actions.
     *
     * @param user    the user associated with the event
     * @param type    the type of event
     * @param message the message describing the event
     * @param ip      the IP address of the user
     */

    private void logEvent(HomeLabUser user, KtxEvent.EventType type, String message, String ip) {
        SystemEvent event = SystemEvent.builder()
                .userId(user != null ? user.getId() : "unknown user")
                .eventType(type)
                .message(message)
                .criticality(KtxEvent.Criticality.REGULAR)
                .level(KtxEvent.Level.INFO)
                .ipAddress(ip)
                .source("auth-service")
                .timestamp(LocalDateTime.now())
                .version(0L)
                .build();
        LOGGER.debug("Sending event: {}", event);
        producer.sendEvent(event);
        authEventJpaRepository.save(event);
    }

    /**
     * Bans a user by setting their status to banned.
     *
     * @param userId the ID of the user to ban
     * @throws SystemException if the user is not found
     */
    @Transactional
    public void banUser(String userId) throws SystemException {
        LOGGER.info("Banning user {}", userId);
        HomeLabUser user = authUserJpaRepository.findById(userId)
                .orElseThrow(() -> new SystemException("User not found"));
        user.setBan(true);
        authUserJpaRepository.save(user);
        logEvent(user, KtxEvent.EventType.USER_ACTION, "User banned", user.getUsername());
        LOGGER.info("User {} banned", userId);
    }

    /**
     * Unbans a user by removing their ban status.
     *
     * @param userId the ID of the user to unban
     * @throws SystemException if the user is not found
     */
    @Transactional
    public void unbanUser(String userId) throws SystemException {
        LOGGER.info("Unbanning user {}", userId);
        HomeLabUser user = authUserJpaRepository.findById(userId)
                .orElseThrow(() -> new SystemException("User not found"));
        user.setBan(false);
        authUserJpaRepository.save(user);
        logEvent(user, KtxEvent.EventType.USER_ACTION, "User unbanned", user.getUsername());
        LOGGER.info("User {} unbanned", userId);
    }
}