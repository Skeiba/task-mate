package com.salah.taskmate.auth;

import com.salah.taskmate.auth.dto.*;
import com.salah.taskmate.auth.service.AuthService;
import com.salah.taskmate.auth.service.PasswordService;
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
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        Map<String, Object> body = new HashMap<>();
        body.put("register message", "User registered successfully");
        body.put("token", response.getToken());

        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        Map<String, Object> body = new HashMap<>();
        body.put("login message", "Login successful");
        body.put("token", response.getToken());

        return ResponseEntity.ok(body);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("fp message", "Reset link sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok(Map.of("rp message", "Password updated successfully"));
    }
}
