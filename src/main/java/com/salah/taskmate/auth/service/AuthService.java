package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.dto.AuthResponse;
import com.salah.taskmate.auth.dto.LoginRequest;
import com.salah.taskmate.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerAdmin(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
