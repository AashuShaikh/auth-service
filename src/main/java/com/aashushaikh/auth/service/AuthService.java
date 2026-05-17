package com.aashushaikh.auth.service;

import com.aashushaikh.auth.dto.AuthResponse;
import com.aashushaikh.auth.dto.LoginRequest;
import com.aashushaikh.auth.dto.RefreshTokenRequest;
import com.aashushaikh.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
