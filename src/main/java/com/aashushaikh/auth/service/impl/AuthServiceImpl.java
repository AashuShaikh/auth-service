package com.aashushaikh.auth.service.impl;

import com.aashushaikh.auth.client.UserServiceClient;
import com.aashushaikh.auth.dto.AuthResponse;
import com.aashushaikh.auth.dto.CreateUserProfileRequest;
import com.aashushaikh.auth.dto.LoginRequest;
import com.aashushaikh.auth.dto.RefreshTokenRequest;
import com.aashushaikh.auth.dto.RegisterRequest;
import com.aashushaikh.auth.exception.DuplicateResourceException;
import com.aashushaikh.auth.model.Role;
import com.aashushaikh.auth.model.User;
import com.aashushaikh.auth.repository.UserRepository;
import com.aashushaikh.auth.service.AuthService;
import com.aashushaikh.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserServiceClient userServiceClient;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // T1: commit credentials before calling the user service
        userRepository.save(user);

        // T2: create profile; if this fails run compensating transaction C1
        try {
            userServiceClient.createUserProfile(
                    new CreateUserProfileRequest(user.getId(), user.getUsername(), user.getEmail())
            );
        } catch (Exception e) {
            // C1: roll back T1 — remove credentials so no orphaned profile exists without auth
            userRepository.deleteById(user.getId());
            throw new RuntimeException("Registration failed. Please try again.");
        }

        return generateTokens(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return generateTokens(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String userId;
        try {
            userId = jwtService.extractUserIdFromRefreshToken(request.getRefreshToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return generateTokens(user);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        // stateless — client discards tokens on logout
    }

    @Override
    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    private AuthResponse generateTokens(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}
