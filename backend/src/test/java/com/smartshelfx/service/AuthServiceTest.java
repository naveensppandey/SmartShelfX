package com.smartshelfx.service;

import com.smartshelfx.dto.JwtResponse;
import com.smartshelfx.dto.LoginRequest;
import com.smartshelfx.dto.RegisterRequest;
import com.smartshelfx.entity.User;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.repository.UserRepository;
import com.smartshelfx.security.JwtUtils;
import com.smartshelfx.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private UserDetailsImpl sampleUserDetails;

    @BeforeEach
    void setUp() {
        sampleUser = new User("johndoe", "john@example.com", "encodedPass", "John Doe", "WAREHOUSE_MANAGER");
        sampleUser.setId(1L);

        sampleUserDetails = new UserDetailsImpl(
                1L, "johndoe", "john@example.com", "John Doe", "encodedPass",
                "WAREHOUSE_MANAGER", true, Collections.emptyList()
        );
    }

    @Test
    @DisplayName("login should authenticate credentials and return signed JWT")
    void login_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(sampleUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateToken(auth)).thenReturn("mock.jwt.token");

        LoginRequest request = new LoginRequest("johndoe", "Secret@123");
        JwtResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("johndoe", response.getUsername());
        assertEquals("WAREHOUSE_MANAGER", response.getRole());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("login should throw BadCredentialsException when password is invalid")
    void login_InvalidPassword() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest("johndoe", "WrongPass");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("register should create user with hashed password and return token")
    void register_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtUtils.generateTokenFromUsernameAndRole("newuser", "WAREHOUSE_MANAGER", 2L)).thenReturn("new.jwt.token");

        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "Secret@123", "New User", "WAREHOUSE_MANAGER");
        JwtResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("new.jwt.token", response.getToken());
        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when username exists")
    void register_DuplicateUsername() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("existinguser", "any@example.com", "Secret@123", "Existing", "ADMIN");

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        assertTrue(ex.getMessage().contains("is already taken"));
        verify(userRepository, never()).save(any(User.class));
    }
}
