package com.example.myapplication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Your computer's IP address
    const val BASE_URL = "http://192.168.12.25:8000/api/"

    // Logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token?.takeIf { it.isNotBlank() }
    }

    // Base client without authentication
    private val baseClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit instance for public endpoints (no auth)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(baseClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Public API service (no auth needed)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Single authenticated client/service using interceptor token injection.
    private val authClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                authToken?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authenticatedApiService: ApiService by lazy {
        authRetrofit.create(ApiService::class.java)
    }

    fun getAuthApiService(token: String): ApiService {
        setAuthToken(token)
        return authenticatedApiService
    }
}