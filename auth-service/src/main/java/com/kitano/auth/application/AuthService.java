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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public String login(UserLoginDTO loginRequest) throws SystemException {
        LOGGER.info("Login attempt for user {}", loginRequest.username());

        HomeLabUser user = authUserJpaRepository.findByUsername(loginRequest.username());

        if (user == null) {
            LOGGER.error("User not found: {}", loginRequest.username());
            logEvent(null, KtxEvent.EventType.AUTHENTICATION_FAILURE, "User not found");
            throw new SystemException("User not found");
        }

        if (!passwordService.matches(loginRequest.password(), user.getPassword())) {
            LOGGER.error("Invalid credentials for user {}", loginRequest.username());
            logEvent(user, KtxEvent.EventType.AUTHENTICATION_FAILURE, "Bad credentials");
            throw new SystemException("Invalid credentials");
        }

        logEvent(user, KtxEvent.EventType.AUTHENTICATION_SUCCESS, "Login successful");

        LOGGER.info("User {} logged in", loginRequest.username());
        return jwtUtils.generateToken(UserMapper.toDto(user));
    }

    public HomelabUserDTO register(HomelabUserCreateDTO dto) throws SystemException {

        LOGGER.info("Registering user {}", dto.getUsername());
        boolean exists = authUserJpaRepository.existsByUsername(dto.getUsername());
        if (exists) {
            LOGGER.error("Username already in use: {}", dto.getUsername());
            throw new SystemException("Username already in use");
        }

        HomeLabUser user = new HomeLabUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordService.hash(dto.getPassword()));
        user.setRole("USER");
        user.setCreated(LocalDateTime.now());

        HomeLabUser saved = authUserJpaRepository.save(user);

        logEvent(saved, KtxEvent.EventType.USER_ACTION, "User created");

        LOGGER.info("User {} registered", dto.getUsername());
        return UserMapper.toDto(saved);
    }

    private void logEvent(HomeLabUser user, KtxEvent.EventType type, String message) {
        SystemEvent event = SystemEvent.builder()
                .userId(user != null ? user.getId() : "unknown user")
                .eventType(type)
                .message(message)
                .criticality(KtxEvent.Criticality.REGULAR)
                .level(KtxEvent.Level.INFO)
                .ipAddress("127.0.0.1")
                .source("auth-service")
                .timestamp(LocalDateTime.now())
                .version(0L)
                .build();
        LOGGER.debug("Sending event: {}", event);
        producer.sendEvent(event);
        authEventJpaRepository.save(event);
    }
}