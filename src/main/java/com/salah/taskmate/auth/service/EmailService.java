package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.dto.ForgotPasswordRequest;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendResetPasswordEmail(String to, String token) {
        // JavaMailSender afterwords now ill just use a hardcoded link
        System.out.println("Password reset link: http://localhost:8080/api/auth/reset-password?token=" + token);
    }
}

