package com.example.lostandfoundfrontend.data

import com.example.lostandfoundfrontend.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Retrofit requires a trailing slash on the base URL
    private val BASE_URL = BuildConfig.API_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    // A 401 on an authenticated request means the token expired → force re-login
    private val sessionInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && request.header("Authorization") != null) {
            SessionManager.onUnauthorized()
        }
        response
    }

    // Generous timeouts: a sleeping Render free instance can take ~50s to wake up
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(sessionInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    /** Helper — formats the Bearer token header value */
    fun bearerToken(token: String) = "Bearer $token"
}

/** Signals the UI that the stored token is no longer valid. */
object SessionManager {
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    fun onUnauthorized() {
        if (TokenStore.isLoggedIn()) {
            TokenStore.clear()
            _expired.value = true
        }
    }

    fun consume() { _expired.value = false }
}
