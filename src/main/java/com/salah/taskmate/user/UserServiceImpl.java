package com.salah.taskmate.user;

import com.salah.taskmate.shared.exception.UserNotFoundException;
import com.salah.taskmate.user.dto.ChangePasswordRequest;
import com.salah.taskmate.user.dto.UpdateUserRequest;
import com.salah.taskmate.user.dto.UserResponse;
import com.salah.taskmate.user.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    private User getCurrentUserOrThrow(UUID currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (!password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain uppercase, lowercase, and a digit");
        }
    }

    @Override
    public User findUserById(UUID userId) {
        return getUserOrThrow(userId);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = getUserOrThrow(userId);
        log.info("Admin retrieved user: {}", userId);
        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        log.info("Admin retrieved {} users (page: {}, size: {})", users.getTotalElements(), page, size);
        return users.map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(boolean enabled, Pageable pageable) {
        Page<User> users = userRepository.findByEnabled(enabled, pageable);
        log.info("Admin retrieved {} users with enabled status: {}", users.getTotalElements(), enabled);
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse updateUserById(UUID userId, UpdateUserRequest request) {
        User user = getUserOrThrow(userId);
        updateUserFields(user, request);
        User savedUser = userRepository.save(user);
        log.info("Admin updated user: {}", userId);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void deleteUserById(UUID userId) {
        User user = getUserOrThrow(userId);
        userRepository.delete(user);
        log.info("Admin deleted user: {}", userId);
    }

    @Override
    public void deactivateUser(UUID userId) {
        User user = getUserOrThrow(userId);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Admin deactivated user: {}", userId);
    }

    @Override
    public void activateUser(UUID userId) {
        User user = getUserOrThrow(userId);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Admin activated user: {}", userId);
    }

    @Override
    public void changeUserPasswordById(UUID userId, ChangePasswordRequest request) {
        User user = getUserOrThrow(userId);
        validatePassword(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Admin changed password for user: {}", userId);
    }

    @Override
    public UserResponse updateUserRole(UUID userId, Role role) {
        User user = getUserOrThrow(userId);
        Role oldRole = user.getRole();
        user.setRole(role);
        User savedUser = userRepository.save(user);
        log.info("Admin changed user {} role from {} to {}", userId, oldRole, role);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getMyProfile(UUID currentUserId) {
        User user = getCurrentUserOrThrow(currentUserId);
        log.debug("User {} retrieved their own profile", currentUserId);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(UUID currentUserId, UpdateUserRequest request) {
        User user = getCurrentUserOrThrow(currentUserId);
        updateUserFields(user, request);
        User savedUser = userRepository.save(user);
        log.info("User {} updated their own profile", currentUserId);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void deleteMyAccount(UUID currentUserId) {
        User user = getCurrentUserOrThrow(currentUserId);
        userRepository.delete(user);
        log.info("User {} deleted their own account", currentUserId);
    }

    @Override
    public void changeMyPassword(UUID currentUserId, ChangePasswordRequest request) {
        User user = getCurrentUserOrThrow(currentUserId);

        if (request.getOldPassword() == null ||
                !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("User {} attempted password change with incorrect current password", currentUserId);
            throw new IllegalArgumentException("Current password is required and must be correct");
        }

        validatePassword(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed their own password", currentUserId);
    }

    @Override
    public void deactivateMyAccount(UUID currentUserId) {
        User user = getCurrentUserOrThrow(currentUserId);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User {} deactivated their own account", currentUserId);
    }


    private void updateUserFields(User user, UpdateUserRequest request) {
        if (request.getUsername() != null) {
            String username = request.getUsername().trim();
            user.setUsername(username.isEmpty() ? null : username);
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase();
            user.setEmail(email.isEmpty() ? null : email);
        }
    }
}

