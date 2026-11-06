package com.smartshelfx.service;

import com.smartshelfx.dto.JwtResponse;
import com.smartshelfx.dto.LoginRequest;
import com.smartshelfx.dto.RegisterRequest;
import com.smartshelfx.entity.User;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.repository.UserRepository;
import com.smartshelfx.security.JwtUtils;
import com.smartshelfx.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService
 * 
 * Handles user authentication, token issuance, and registration.
 */
@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticates credentials and returns a signed JWT.
     */
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                userDetails.getRole()
        );
    }

    /**
     * Registers a new user, hashes password with BCrypt, and returns a JWT session.
     */
    public JwtResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username '" + username + "' is already taken.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email '" + email + "' is already registered.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Never plain text!
        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole() != null ? request.getRole().trim().toUpperCase() : "WAREHOUSE_MANAGER");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Generate immediate JWT token for the newly registered user
        String jwt = jwtUtils.generateTokenFromUsernameAndRole(
                savedUser.getUsername(),
                savedUser.getRole(),
                savedUser.getId()
        );

        return new JwtResponse(
                jwt,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
    }
}
