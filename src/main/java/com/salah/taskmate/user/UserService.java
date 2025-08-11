package com.salah.taskmate.user;

import com.salah.taskmate.user.dto.ChangePasswordRequest;
import com.salah.taskmate.user.dto.UpdateUserRequest;
import com.salah.taskmate.user.dto.UserResponse;
import com.salah.taskmate.user.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    User findUserById(UUID userId);

    UserResponse getUserById(UUID userId);
    Page<UserResponse> getAllUsers(int page, int size);
    Page<UserResponse> getUsersByStatus(boolean enabled, Pageable pageable);
    UserResponse updateUserById(UUID userId, UpdateUserRequest request);
    void deleteUserById(UUID userId);
    void deactivateUser(UUID userId);
    void activateUser(UUID userId);
    void changeUserPasswordById(UUID userId, ChangePasswordRequest request);
    UserResponse updateUserRole(UUID userId, Role role);

    UserResponse getMyProfile(UUID currentUserId);
    UserResponse updateMyProfile(UUID currentUserId, UpdateUserRequest request);
    void deleteMyAccount(UUID currentUserId);
    void changeMyPassword(UUID currentUserId, ChangePasswordRequest request);
    void deactivateMyAccount(UUID currentUserId);
}
