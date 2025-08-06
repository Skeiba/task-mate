package com.salah.taskmate.auth.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
