package com.smartshelfx.controller;

import com.smartshelfx.dto.JwtResponse;
import com.smartshelfx.dto.LoginRequest;
import com.smartshelfx.dto.RegisterRequest;
import com.smartshelfx.dto.UserResponse;
import com.smartshelfx.security.UserDetailsImpl;
import com.smartshelfx.service.AuthService;
import com.smartshelfx.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 * 
 * Public REST API endpoints for user authentication and session verification.
 * Base Path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * POST /api/auth/login
     * Validates credentials and returns a Bearer JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/register
     * Creates a new user with BCrypt hashed password and returns a JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/auth/me
     * Returns the currently authenticated user's profile details.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }
}
