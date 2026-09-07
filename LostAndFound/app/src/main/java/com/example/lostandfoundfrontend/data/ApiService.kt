package com.example.lostandfoundfrontend.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<MessageResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<MessageResponse>

    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>

    // ─── Items ────────────────────────────────────────────────────────────────

    @GET("items")
    suspend fun getItems(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ItemsResponse>

    @GET("items/stats")
    suspend fun getStats(): Response<StatsResponse>

    @GET("items/{id}")
    suspend fun getItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<SingleItemResponse>

    @POST("items")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Body request: CreateItemRequest
    ): Response<SingleItemResponse>

    @Multipart
    @POST("items/{id}/image")
    suspend fun uploadItemImage(
        @Header("Authorization") token: String,
        @Path("id") itemId: String,
        @Part image: MultipartBody.Part
    ): Response<SingleItemResponse>

    @POST("items/{id}/claim")
    suspend fun claimItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: String,
        @Body request: ClaimItemRequest
    ): Response<MessageResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<MessageResponse>

    // ─── User / Profile ───────────────────────────────────────────────────────

    @GET("user/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserDto>>

    @PUT("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    @Multipart
    @POST("user/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<UserDto>>

    @PUT("user/notifications")
    suspend fun updateNotifications(
        @Header("Authorization") token: String,
        @Body body: Map<String, Boolean>
    ): Response<ApiResponse<UserDto>>

    @GET("user/my-items")
    suspend fun getMyItems(
        @Header("Authorization") token: String
    ): Response<ItemsResponse>

    @GET("user/saved-items")
    suspend fun getSavedItems(
        @Header("Authorization") token: String
    ): Response<ItemsResponse>

    @POST("user/saved-items/{itemId}")
    suspend fun saveItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String
    ): Response<MessageResponse>

    @DELETE("user/saved-items/{itemId}")
    suspend fun unsaveItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String
    ): Response<MessageResponse>
}
