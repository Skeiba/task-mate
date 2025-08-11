package com.salah.taskmate.user;

import com.salah.taskmate.security.CustomUserDetails;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.user.dto.ChangePasswordRequest;
import com.salah.taskmate.user.dto.UpdateUserRequest;
import com.salah.taskmate.user.dto.UserResponse;
import com.salah.taskmate.user.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //Admin
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User retrieved successfully")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "Users retrieved successfully")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "Users retrieved by status successfully")
    public ResponseEntity<Page<UserResponse>> getUsersByStatus(
            @RequestParam boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserResponse> users = userService.getUsersByStatus(enabled, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User updated successfully")
    public ResponseEntity<UserResponse> updateUserById(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        UserResponse updatedUser = userService.updateUserById(userId, updateUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User deleted successfully")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User deactivated successfully")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User activated successfully")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        userService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User password changed successfully")
    public ResponseEntity<Void> changeUserPasswordById(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changeUserPasswordById(userId, changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @StandardApiResponse(message = "User role updated successfully")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam Role role) {
        UserResponse updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    //Current user
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @StandardApiResponse(message = "Current user profile retrieved successfully")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse profile = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @StandardApiResponse(message = "Current user profile updated successfully")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest updateUserRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse updatedProfile = userService.updateMyProfile(userDetails.getId(), updateUserRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @StandardApiResponse(message = "Current user account deleted successfully")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @StandardApiResponse(message = "Current user password changed successfully")
    public ResponseEntity<Void> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.changeMyPassword(userDetails.getId(), changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/deactivate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @StandardApiResponse(message = "Current user account deactivated successfully")
    public ResponseEntity<Void> deactivateMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deactivateMyAccount(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}