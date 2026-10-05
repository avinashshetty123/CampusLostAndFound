package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class Dto {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required") private String email;
        @NotBlank(message = "Password is required") private String password;

        // Trim before validation: phone keyboards often add a trailing space
        public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Name is required") @Size(max = 80, message = "Name is too long")
        private String name;
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address")
        private String email;
        private String mobile;
        @JsonProperty("student_class") private String studentClass;
        private String department;
        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        private String password;

        // Trim before validation: phone keyboards often add a trailing space
        public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
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
        @NotBlank(message = "Current password is required")
        @JsonProperty("current_password") private String currentPassword;
        @NotBlank(message = "New password is required") @Size(min = 6, max = 100, message = "New password must be at least 6 characters")
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
        @JsonProperty("resolved_at") private String resolvedAt;
        @JsonProperty("claimed_by_name") private String claimedByName;
        @JsonProperty("claim_message") private String claimMessage;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CreateItemRequest {
        @NotBlank(message = "Item name is required") @Size(max = 100, message = "Item name is too long")
        private String title;
        @Size(max = 1000, message = "Description is too long")
        private String description;
        @Size(max = 150, message = "Location is too long")
        private String location;
        @NotBlank(message = "Status is required") @Pattern(regexp = "LOST|FOUND", message = "Status must be LOST or FOUND")
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
        @Size(max = 500, message = "Message is too long")
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
