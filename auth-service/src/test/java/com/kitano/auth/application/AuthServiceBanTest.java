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
import com.kitano.iface.model.KtxRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceBanTest {

    @Mock private AuthUserJpaRepository userRepository;
    @Mock private PasswordService passwordService;
    @Mock private AuthProducer producer;
    @Mock private AuthEventJpaRepository eventJpaRepository;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldBanUser() throws SystemException {
        // Given
        HomeLabUser user = new HomeLabUser();
        user.setUsername("testuser");
        user.setEnabled(true);
        user.setId(UUID.randomUUID().toString());
        user.setBan(false);
        user.setRole(KtxRole.USER);
        user.setCreated(LocalDateTime.now());
        user.setPassword("password");
        user.setVersion(0L);

        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        lenient().when(producer.sendEvent(any())).thenReturn(true);
        lenient().when(eventJpaRepository.save(any())).thenReturn(null);

        // When
        authService.banUser("testuser");

        // Then
        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotAllowBanUserToLogin() {
        // Given
        HomeLabUser user = new HomeLabUser();
        user.setUsername("testuser");
        user.setEnabled(false);
        user.setId(UUID.randomUUID().toString());
        user.setBan(true);
        user.setRole(KtxRole.USER);
        user.setCreated(LocalDateTime.now());
        user.setPassword("password");
        user.setVersion(0L);

        when(userRepository.findByUsername(any())).thenReturn(user);
        when(passwordService.matches(any(), any())).thenReturn(true);

        // When & Then
        assertThrows(SystemException.class, () ->
                authService.login(new UserLoginDTO(user.getUsername(), user.getPassword(), "ipAddress"))
        );
    }

    @Test
    void shouldNotAllowBanUserToRegister() {
        // Given
        HomelabUserCreateDTO createDTO = new HomelabUserCreateDTO();
        createDTO.setUsername("testuser");
        createDTO.setPassword("password");
        createDTO.setIpAddress("ipAddress");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThrows(SystemException.class, () -> authService.register(createDTO));
    }

    @Test
    void afterUnbanShouldAllowLogin() throws SystemException {
        // Given
        HomeLabUser user = new HomeLabUser();
        user.setUsername("testuser");
        user.setEnabled(false);
        user.setId(UUID.randomUUID().toString());
        user.setBan(true);
        user.setRole(KtxRole.USER);
        user.setCreated(LocalDateTime.now());
        user.setPassword("password");
        user.setVersion(0L);

        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordService.matches(any(), any())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtUtils.generateToken(any())).thenReturn("token");
        lenient().when(producer.sendEvent(any())).thenReturn(true);
        lenient().when(eventJpaRepository.save(any())).thenReturn(null);

        // Unban the user
        authService.unbanUser(user.getId());

        // Simulate enabled user after unban
        user.setEnabled(true);
        user.setBan(false);

        // When
        authService.login(new UserLoginDTO(user.getUsername(), user.getPassword(), "ip"));

        // Then -> pas d’exception donc login autorisé
    }
}