package com.example.lostandfoundfrontend.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// ─── Result wrapper ───────────────────────────────────────────────────────────

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ─── Token Store ──────────────────────────────────────────────────────────────

object TokenStore {
    private const val PREFS_NAME = "laf_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuth(token: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun isLoggedIn(): Boolean = getToken() != null
    fun clear() = prefs.edit().clear().apply()
}

// ─── Repository ───────────────────────────────────────────────────────────────

class LostFoundRepository {

    private val api = ApiClient.api
    private fun token() = ApiClient.bearerToken(TokenStore.getToken() ?: "")

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        safeCall { api.login(LoginRequest(email, password)) }

    suspend fun register(
        name: String, email: String, mobile: String,
        studentClass: String, department: String, password: String
    ): Result<AuthResponse> =
        safeCall { api.register(RegisterRequest(name, email, mobile, studentClass, department, password)) }

    suspend fun logout(): Result<MessageResponse> =
        safeCall { api.logout(token()) }

    suspend fun forgotPassword(email: String): Result<MessageResponse> =
        safeCall { api.forgotPassword(mapOf("email" to email)) }

    suspend fun changePassword(current: String, new: String): Result<MessageResponse> =
        safeCall { api.changePassword(token(), ChangePasswordRequest(current, new)) }

    // ── Items ─────────────────────────────────────────────────────────────────

    suspend fun getItems(status: String? = null, search: String? = null, page: Int = 1): Result<ItemsResponse> =
        safeCall { api.getItems(token(), status, search, page) }

    suspend fun getStats(): Result<StatsResponse> =
        safeCall { api.getStats() }

    suspend fun getItemById(itemId: String): Result<SingleItemResponse> =
        safeCall { api.getItemById(token(), itemId) }

    suspend fun createItem(
        title: String, description: String, location: String,
        status: String, category: String, contactInfo: String
    ): Result<SingleItemResponse> =
        safeCall { api.createItem(token(), CreateItemRequest(title, description, location, status, category, contactInfo)) }

    suspend fun uploadItemImage(itemId: String, imageFile: File): Result<SingleItemResponse> = safeCall {
        val part = MultipartBody.Part.createFormData(
            "image", imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        api.uploadItemImage(token(), itemId, part)
    }

    suspend fun claimItem(itemId: String, message: String): Result<MessageResponse> =
        safeCall { api.claimItem(token(), itemId, ClaimItemRequest(itemId, message)) }

    suspend fun deleteItem(itemId: String): Result<MessageResponse> =
        safeCall { api.deleteItem(token(), itemId) }

    // ── User ──────────────────────────────────────────────────────────────────

    suspend fun getProfile(): Result<ApiResponse<UserDto>> =
        safeCall { api.getProfile(token()) }

    suspend fun updateProfile(
        name: String, mobile: String, studentClass: String, department: String
    ): Result<ApiResponse<UserDto>> =
        safeCall { api.updateProfile(token(), UpdateProfileRequest(name, mobile, studentClass, department)) }

    suspend fun uploadAvatar(imageFile: File): Result<ApiResponse<UserDto>> = safeCall {
        val part = MultipartBody.Part.createFormData(
            "avatar", imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        api.uploadAvatar(token(), part)
    }

    suspend fun updateNotifications(enabled: Boolean): Result<ApiResponse<UserDto>> =
        safeCall { api.updateNotifications(token(), mapOf("notifications_enabled" to enabled)) }

    suspend fun getMyItems(): Result<ItemsResponse> =
        safeCall { api.getMyItems(token()) }

    suspend fun getSavedItems(): Result<ItemsResponse> =
        safeCall { api.getSavedItems(token()) }

    suspend fun saveItem(itemId: String): Result<MessageResponse> =
        safeCall { api.saveItem(token(), itemId) }

    suspend fun unsaveItem(itemId: String): Result<MessageResponse> =
        safeCall { api.unsaveItem(token(), itemId) }

    // ── Safe call helper ──────────────────────────────────────────────────────

    private val gson = Gson()

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("Empty response", response.code())
            } else {
                Result.Error(errorMessage(response), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            Result.Error("The server is taking too long to respond. It may be waking up — please try again.")
        } catch (e: UnknownHostException) {
            Result.Error("Can't reach the server. Check your internet connection.")
        } catch (e: ConnectException) {
            Result.Error("Can't reach the server. Check your internet connection.")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    /** Prefer the backend's JSON `message`, fall back to a friendly text per status code. */
    private fun errorMessage(response: retrofit2.Response<*>): String {
        val serverMessage = try {
            response.errorBody()?.string()?.let { gson.fromJson(it, MessageResponse::class.java)?.message }
        } catch (e: Exception) {
            null
        }
        if (!serverMessage.isNullOrBlank()) return serverMessage
        return when (response.code()) {
            401 -> "Session expired. Please log in again."
            403 -> "You are not allowed to do that"
            404 -> "Not found"
            413 -> "Image must be smaller than 10MB"
            in 500..599 -> "Server error. Please try again shortly."
            else -> "Request failed (${response.code()})"
        }
    }
}
