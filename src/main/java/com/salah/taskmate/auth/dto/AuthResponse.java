package com.salah.taskmate.auth.dto;

import com.salah.taskmate.user.User;
import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
}
