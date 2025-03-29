package com.kitano.auth.application;

import com.kitano.auth.infrastructure.messaging.AuthProducer;
import com.kitano.auth.infrastructure.repository.AuthEventJpaRepository;
import com.kitano.auth.infrastructure.repository.AuthUserJpaRepository;
import com.kitano.auth.infrastructure.security.JwtUtils;
import com.kitano.auth.infrastructure.security.PasswordService;
import com.kitano.auth.model.HomelabUserCreateDTO;
import com.kitano.auth.model.UserLoginDTO;
import com.kitano.core.model.HomeLabUser;
import com.kitano.core.model.SystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final String username = "alex";
    private final String rawPassword = "pass123";
    private final String hashedPassword = "$2a$10$mocked-hash";
    private final String fakeJwtToken = "mocked.jwt.token";
    @Mock
    private AuthUserJpaRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthProducer producer;
    @Mock
    private PasswordService passwordService;
    @Mock
    private AuthEventJpaRepository authEventJpaRepository;
    @InjectMocks
    private AuthService authService;
    private HomeLabUser user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new HomeLabUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setCreated(LocalDateTime.now());
    }

    @Test
    void login_shouldReturnJwtToken_onSuccess() throws SystemException {
        UserLoginDTO loginDTO = new UserLoginDTO(username, rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtils.generateToken(any())).thenReturn(fakeJwtToken);
        when(passwordService.matches(rawPassword, hashedPassword)).thenReturn(true);

        String token = authService.login(loginDTO);

        assertNotNull(token);
        assertEquals(fakeJwtToken, token);
        verify(producer).sendEvent(any()); // Logged event
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        UserLoginDTO loginDTO = new UserLoginDTO(username, rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        assertThrows(SystemException.class, () -> authService.login(loginDTO));
        verify(producer).sendEvent(any()); // Still logs failure
    }

    @Test
    void register_shouldCreateNewUser() throws SystemException {
        HomelabUserCreateDTO dto = new HomelabUserCreateDTO();
        dto.setUsername(username);
        dto.setPassword(rawPassword);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any())).thenReturn(user);

        var result = authService.register(dto);

        assertNotNull(result);
        assertEquals(username, result.username());
        verify(producer).sendEvent(any());
    }

    @Test
    void register_shouldThrow_whenUsernameTaken() {
        HomelabUserCreateDTO dto = new HomelabUserCreateDTO();
        dto.setUsername(username);
        dto.setPassword(rawPassword);

        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(SystemException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }
}