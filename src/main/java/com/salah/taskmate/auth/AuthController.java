package com.salah.taskmate.auth;

import com.salah.taskmate.auth.dto.*;
import com.salah.taskmate.auth.service.AuthService;
import com.salah.taskmate.auth.service.PasswordService;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordService  passwordService;

    @PostMapping("/register")
    @StandardApiResponse(message = "User registered successfully")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @StandardApiResponse(message = "Logged in successfully")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
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
