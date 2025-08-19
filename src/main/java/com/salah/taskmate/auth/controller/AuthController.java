package com.salah.taskmate.auth.controller;

import com.salah.taskmate.auth.dto.*;
import com.salah.taskmate.auth.service.AuthService;
import com.salah.taskmate.auth.service.PasswordService;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordService  passwordService;

    @PostMapping("/register")
    @StandardApiResponse(message = "User registered successfully")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    @PostMapping("/login")
    @StandardApiResponse(message = "Logged in successfully")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    @StandardApiResponse(message = "Logged out successfully")
    public ResponseEntity<Void> logout(HttpServletResponse response){
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @StandardApiResponse(message = "Reset link sent")
    public Object forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
        return null;
    }

    @PostMapping("/reset-password")
    @StandardApiResponse(message = "Password updated successfully")
    public Object resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return null;
    }
}
