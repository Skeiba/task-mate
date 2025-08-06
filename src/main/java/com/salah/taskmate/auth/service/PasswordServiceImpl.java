package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.PasswordResetToken;
import com.salah.taskmate.auth.PasswordResetTokenRepository;
import com.salah.taskmate.auth.dto.ForgotPasswordRequest;
import com.salah.taskmate.auth.dto.ResetPasswordRequest;
import com.salah.taskmate.auth.helper.TokenGenerator;
import com.salah.taskmate.user.User;
import com.salah.taskmate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = tokenGenerator.generateToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepository.save(resetToken);

        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        tokenRepository.delete(token);
    }
}
