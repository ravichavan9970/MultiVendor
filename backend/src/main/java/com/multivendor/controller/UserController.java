package com.multivendor.controller;

import com.multivendor.dto.UserDtos.*;
import com.multivendor.security.UserPrincipal;
import com.multivendor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(currentUser.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal UserPrincipal currentUser,
                                                             @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(currentUser.getId(), request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal UserPrincipal currentUser,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMyAccount(@AuthenticationPrincipal UserPrincipal currentUser) {
        userService.deleteUserAccount(currentUser.getId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserById(@PathVariable Long id) {
        userService.deleteUserAccount(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User account deleted successfully");
        return ResponseEntity.ok(response);
    }
}
