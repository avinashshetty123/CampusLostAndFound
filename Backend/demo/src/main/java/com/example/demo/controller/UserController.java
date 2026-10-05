package com.example.demo.controller;

import com.example.demo.dto.Dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ItemService itemService;
    private final CloudinaryService cloudinaryService;

    /** GET /api/user/profile */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ApiResponse<>(authService.toDto(user), "Profile fetched", true));
    }

    /** PUT /api/user/profile — update name, mobile, class, department (null fields are left unchanged) */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest req) {
        if (req.getName() != null) {
            if (req.getName().isBlank()) throw ApiException.badRequest("Name cannot be empty");
            user.setName(req.getName().trim());
        }
        if (req.getMobile() != null) user.setMobile(blankToNull(req.getMobile()));
        if (req.getStudentClass() != null) user.setStudentClass(blankToNull(req.getStudentClass()));
        if (req.getDepartment() != null) user.setDepartment(blankToNull(req.getDepartment()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(authService.toDto(user), "Profile updated", true));
    }

    /** POST /api/user/avatar — upload profile picture to Cloudinary */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<UserDto>> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestPart("avatar") MultipartFile file) {
        user.setAvatarUrl(cloudinaryService.uploadImage(file));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(authService.toDto(user), "Avatar updated", true));
    }

    /** PUT /api/user/notifications — toggle notification preference */
    @PutMapping("/notifications")
    public ResponseEntity<ApiResponse<UserDto>> updateNotifications(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("notifications_enabled");
        if (enabled != null) {
            user.setNotificationsEnabled(enabled);
            userRepository.save(user);
        }
        return ResponseEntity.ok(new ApiResponse<>(authService.toDto(user), "Notifications updated", true));
    }

    /** GET /api/user/my-items — items reported by the logged-in user */
    @GetMapping("/my-items")
    public ResponseEntity<ItemsResponse> getMyItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemService.getMyItems(user));
    }

    /** GET /api/user/saved-items — bookmarked items */
    @GetMapping("/saved-items")
    public ResponseEntity<ItemsResponse> getSavedItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemService.getSavedItems(user));
    }

    /** POST /api/user/saved-items/{itemId} — save/bookmark an item */
    @PostMapping("/saved-items/{itemId}")
    public ResponseEntity<MessageResponse> saveItem(
            @AuthenticationPrincipal User user,
            @PathVariable String itemId) {
        return ResponseEntity.ok(itemService.saveItem(itemId, user));
    }

    /** DELETE /api/user/saved-items/{itemId} — remove bookmark */
    @DeleteMapping("/saved-items/{itemId}")
    public ResponseEntity<MessageResponse> unsaveItem(
            @AuthenticationPrincipal User user,
            @PathVariable String itemId) {
        return ResponseEntity.ok(itemService.unsaveItem(itemId, user));
    }

    private static String blankToNull(String s) {
        return s.isBlank() ? null : s.trim();
    }
}
