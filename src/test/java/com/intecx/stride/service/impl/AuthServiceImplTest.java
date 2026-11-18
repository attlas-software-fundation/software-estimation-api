package com.intecx.stride.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intecx.stride.dto.request.auth.LoginRequest;
import com.intecx.stride.dto.request.auth.LogoutRequest;
import com.intecx.stride.dto.response.AuthResponse;
import com.intecx.stride.dto.response.UserResponse;
import com.intecx.stride.entity.Role;
import com.intecx.stride.entity.User;
import com.intecx.stride.exception.UnauthorizedException;
import com.intecx.stride.mapper.UserMapper;
import com.intecx.stride.repository.PasswordResetTokenRepository;
import com.intecx.stride.repository.UserRepository;
import com.intecx.stride.security.JwtTokenBlacklistService;
import com.intecx.stride.security.JwtTokenProvider;
import com.intecx.stride.service.EmailService;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private JwtTokenBlacklistService blacklistService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(UUID.randomUUID())
                .code("DEV")
                .name("Developer")
                .isSystem(false)
                .isActive(true)
                .permissions(Collections.emptyList())
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Neil")
                .lastName("Armstrong")
                .email("neil.armstrong@intecx.pe")
                .passwordHash("encoded_password")
                .role(role)
                .isActive(true)
                .build();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("neil.armstrong@intecx.pe", "Secret123!");

        when(userRepository.findActiveByEmail("neil.armstrong@intecx.pe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "encoded_password")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("mocked-jwt-token");
        when(tokenProvider.generateRefreshToken(user)).thenReturn("mocked-refresh-token");
        when(tokenProvider.getExpirationInSeconds()).thenReturn(86400L);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("mocked-refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        verify(userRepository).findActiveByEmail("neil.armstrong@intecx.pe");
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest("neil.armstrong@intecx.pe", "WrongPassword");

        when(userRepository.findActiveByEmail("neil.armstrong@intecx.pe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encoded_password")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_UserNotFound_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest("nonexistent@intecx.pe", "AnyPassword");

        when(userRepository.findActiveByEmail("nonexistent@intecx.pe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void logout_Success() {
        LogoutRequest request = new LogoutRequest();
        String bearerToken = "Bearer test-token-123";
        java.util.Date expirationDate = new java.util.Date();

        when(tokenProvider.validateToken("test-token-123")).thenReturn(true);
        when(tokenProvider.getExpirationDateFromToken("test-token-123")).thenReturn(expirationDate);

        authService.logout(bearerToken, request);

        verify(blacklistService).blacklistToken("test-token-123", expirationDate);
    }
}
