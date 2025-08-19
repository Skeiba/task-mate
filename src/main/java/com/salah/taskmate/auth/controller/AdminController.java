package com.salah.taskmate.auth.controller;

import com.salah.taskmate.auth.dto.RegisterRequest;
import com.salah.taskmate.auth.service.AuthService;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "Admin created successfully")
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.registerAdmin(request, response));
    }
}

