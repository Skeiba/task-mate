package com.salah.taskmate.user;

import com.salah.taskmate.user.dto.ChangePasswordRequest;
import com.salah.taskmate.user.dto.UpdateUserRequest;
import com.salah.taskmate.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    User findUserById(UUID userId);

    UserResponse getUserById(UUID userId);
    Page<UserResponse> getAllUsers(int page, int size);
    void deactivateUser(UUID userId);
    void activateUser(UUID userId);
    public Page<UserResponse> getUsersByStatus(boolean enabled, Pageable pageable);


    UserResponse getProfile(UUID userId);
    UserResponse updateProfile(UUID userId, UpdateUserRequest updateUserRequest);
    void deleteAccount(UUID userId);
    void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest);
}
