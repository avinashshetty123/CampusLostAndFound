package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class Dto {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class RegisterRequest {
        private String name;
        private String email;
        private String mobile;
        @JsonProperty("student_class") private String studentClass;
        private String department;
        private String password;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private UserDto user;
        private String message;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ChangePasswordRequest {
        @JsonProperty("current_password") private String currentPassword;
        @JsonProperty("new_password") private String newPassword;
    }

    // ── User ──────────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UserDto {
        private String id;
        private String name;
        private String email;
        private String mobile;
        @JsonProperty("student_class") private String studentClass;
        private String department;
        @JsonProperty("avatar_url") private String avatarUrl;
        @JsonProperty("reports_count") private int reportsCount;
        @JsonProperty("resolved_count") private int resolvedCount;
        @JsonProperty("pending_count") private int pendingCount;
        @JsonProperty("notifications_enabled") private boolean notificationsEnabled;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UpdateProfileRequest {
        private String name;
        private String mobile;
        @JsonProperty("student_class") private String studentClass;
        private String department;
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ItemDto {
        private String id;
        private String title;
        private String description;
        private String location;
        private String status;
        private String category;
        @JsonProperty("image_url") private String imageUrl;
        @JsonProperty("contact_info") private String contactInfo;
        @JsonProperty("reported_by") private String reportedBy;
        @JsonProperty("reporter_name") private String reporterName;
        @JsonProperty("reporter_email") private String reporterEmail;
        @JsonProperty("reporter_phone") private String reporterPhone;
        @JsonProperty("reporter_dept") private String reporterDept;
        @JsonProperty("reporter_class") private String reporterClass;
        @JsonProperty("created_at") private String createdAt;
        @JsonProperty("is_resolved") private boolean resolved;
        @JsonProperty("is_saved") private boolean saved;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CreateItemRequest {
        private String title;
        private String description;
        private String location;
        private String status;
        private String category;
        @JsonProperty("contact_info") private String contactInfo;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ItemsResponse {
        private List<ItemDto> items;
        private long total;
        private int page;
        private int per_page;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class SingleItemResponse {
        private ItemDto item;
        private String message;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ClaimItemRequest {
        @JsonProperty("item_id") private String itemId;
        private String message;
    }

    // ── Generic ───────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ApiResponse<T> {
        private T data;
        private String message;
        private boolean success;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class StatsResponse {
        @JsonProperty("total_items") private long totalItems;
        @JsonProperty("lost_items") private long lostItems;
        @JsonProperty("found_items") private long foundItems;
        @JsonProperty("resolved_items") private long resolvedItems;
    }
}
