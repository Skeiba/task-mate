package com.salah.taskmate.auth.helper;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class TokenGenerator {
    private static final SecureRandom random = new SecureRandom();

    public String generateToken() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
