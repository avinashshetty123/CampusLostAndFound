package com.example.lostandfoundfrontend.data

import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("student_class") val studentClass: String,
    @SerializedName("department") val department: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto,
    @SerializedName("message") val message: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// ─── User ─────────────────────────────────────────────────────────────────────

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("student_class") val studentClass: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("reports_count") val reportsCount: Int = 0,
    @SerializedName("resolved_count") val resolvedCount: Int = 0,
    @SerializedName("pending_count") val pendingCount: Int = 0,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean = true
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("student_class") val studentClass: String,
    @SerializedName("department") val department: String
)

// ─── Items ────────────────────────────────────────────────────────────────────

data class ItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("contact_info") val contactInfo: String? = null,
    @SerializedName("reported_by") val reportedBy: String,
    @SerializedName("reporter_name") val reporterName: String,
    @SerializedName("reporter_email") val reporterEmail: String? = null,
    @SerializedName("reporter_phone") val reporterPhone: String? = null,
    @SerializedName("reporter_dept") val reporterDept: String? = null,
    @SerializedName("reporter_class") val reporterClass: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("is_resolved") val isResolved: Boolean = false,
    @SerializedName("is_saved") val isSaved: Boolean = false
)

data class CreateItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: String,
    @SerializedName("contact_info") val contactInfo: String
)

data class ItemsResponse(
    @SerializedName("items") val items: List<ItemDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int
)

data class SingleItemResponse(
    @SerializedName("item") val item: ItemDto,
    @SerializedName("message") val message: String? = null
)

data class ClaimItemRequest(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("message") val message: String
)

data class StatsResponse(
    @SerializedName("total_items") val totalItems: Long,
    @SerializedName("lost_items") val lostItems: Long,
    @SerializedName("found_items") val foundItems: Long,
    @SerializedName("resolved_items") val resolvedItems: Long
)

// ─── Generic ──────────────────────────────────────────────────────────────────

data class ApiResponse<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean
)

data class MessageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean
)
