package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.EmailContentBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailContentBuilder contentBuilder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    //use this when you use Google SMTP for production
//    @Value("${email.from}")
//    private String from;

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
//            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendResetPasswordEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String content = contentBuilder.build("reset-password", Map.of(
                "resetLink", resetLink,
                "year", Year.now().getValue()
        ));

        sendHtmlEmail(to, "🔐 Reset Your TaskMate Password", content);
    }

    public void sendWelcomeEmail(String to, String username) {
        String content = contentBuilder.build("welcome", Map.of(
                "username", username,
                "appUrl", frontendUrl,
                "year", Year.now().getValue()
        ));

        sendHtmlEmail(to, "Welcome to TaskMate", content);
    }
}
