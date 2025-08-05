package com.salah.taskmate.user;

import com.salah.taskmate.user.dto.ChangePasswordRequest;
import com.salah.taskmate.user.dto.UpdateUserRequest;
import com.salah.taskmate.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User not found"));
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return userMapper.toResponse(findUserById(userId));
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable  pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public void deactivateUser(UUID userId) {
        User user = findUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(UUID userId) {
        User user = findUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(boolean enabled, Pageable pageable) {
        return userRepository.findByEnabled(enabled, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public UserResponse getProfile(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(UUID userId, UpdateUserRequest updateUserRequest) {
        User user = findUserById(userId);

        user.setUsername(updateUserRequest.getUsername());
        user.setEmail(updateUserRequest.getEmail());

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public void deleteAccount(UUID userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old Password Doesn't Match");
        }
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }
}
