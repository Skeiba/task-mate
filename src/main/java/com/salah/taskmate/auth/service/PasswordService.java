package com.salah.taskmate.auth.service;

import com.salah.taskmate.auth.dto.ForgotPasswordRequest;
import com.salah.taskmate.auth.dto.ResetPasswordRequest;

public interface PasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
