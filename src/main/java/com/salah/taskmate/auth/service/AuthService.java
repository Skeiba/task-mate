package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.dto.LoginRequest;
import com.salah.taskmate.auth.dto.RegisterRequest;
import com.salah.taskmate.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request, HttpServletResponse response);
    UserResponse registerAdmin(RegisterRequest request, HttpServletResponse response);
    UserResponse login(LoginRequest request, HttpServletResponse response);
    void logout(HttpServletResponse response);
}
